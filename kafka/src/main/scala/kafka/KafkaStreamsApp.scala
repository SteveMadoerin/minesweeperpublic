package kafka

import io.circe.*
import io.circe.generic.auto.*
import io.circe.jawn.decode
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.kstream.{GlobalKTable, JoinWindows, TimeWindows, Windowed}
import org.apache.kafka.streams.scala.ImplicitConversions.*
import org.apache.kafka.streams.scala.*
import org.apache.kafka.streams.scala.kstream.{KGroupedStream, KStream, KTable}
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes.*
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.apache.kafka.streams.scala.ImplicitConversions.*

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.Properties

object KafkaStreamsApp {

    object Domain{
        type UserId = String
        type Profile = String
        type Product = String
        type OrderId = String
        type Status = String

        case class Order(orderId: OrderId, userId: UserId, products: List[Product], amount: Double)
        case class Discount(profile: Profile, amount: Double)
        case class Payment(orderId: OrderId, status: Status)
    }

    object Topics{
        final val OrdersByUserTopic = "orders-by-user"
        final val DiscountProfilesByUserTopic = "discount-profiles-by-user"
        final val DiscountsTopic = "discounts"
        final val OrdersTopic = "orders"
        final val PaymentsTopic = "payments"
        final val PayedOrdersTopic = "paid-orders"
    }

    // source  = emits elements
    // flows = transforms elements along the way (e.g. map)
    // sink = "ingests" elements
    import Domain._
    import Topics._
    implicit def serde[A >: Null: Decoder : Encoder]: Serde[A] = {
        val serializer = (a: A) => a.asJson.noSpaces.getBytes
        val deserializer = (bytes: Array[Byte]) => {
            val string = new String(bytes)
            decode[A](string).toOption
        }
        Serdes.fromFn[A](serializer, deserializer)
    }

    // start main here:
    def main(args: Array[String]): Unit = {

    // topologiy
    val builder = new StreamsBuilder()

    // KStream

    val usersOrdersStreams: KStream[UserId, Order] = builder.stream[UserId, Order](OrdersByUserTopic)

    // KTable . is distributed
    // --config "cleanup.policy=compact"
    val userProfilesTable: KTable[UserId, Profile] = builder.table[UserId, Profile](DiscountProfilesByUserTopic)

    // GlobalKTable - copied to all the nodes
    // should store few values - otherwise it will crash the nodes of the kafka cluster
    // cleanup.policy=compact
    val discountProfilesGlobalTable: GlobalKTable[Profile, Discount] = builder.globalTable[Profile, Discount](DiscountsTopic)

    // KStream transformation: filter, map, mapValues, flatMap, flatMapValues
    val expensiveOrders: KStream[UserId, Order] = usersOrdersStreams.filter { (userId, order) =>
        order.amount > 1000
    }

    val listOfProducts: KStream[UserId, List[Product]] = usersOrdersStreams.mapValues{ order =>
        order.products
    }

    val productsStream: KStream[UserId, Product] = usersOrdersStreams.flatMapValues(_.products)

    // join
    val ordersWithUserProfiles: KStream[UserId, (Order, Profile)] = usersOrdersStreams.join(userProfilesTable){ (order, profile) =>
        (order, profile)
    }

    val discountedOrdersStream: KStream[UserId, Order] = ordersWithUserProfiles.join(discountProfilesGlobalTable)(
        { case (userId, (order, profile)) => profile},
        { case ((order, profile), discount) => order.copy(amount = order.amount - discount.amount)}
    )

    // pick another identifier
    val ordersStream = discountedOrdersStream.selectKey((userId, order) => order.orderId)
    val paymentsStream = builder.stream[OrderId, Payment](PaymentsTopic)

    val joinWindow = JoinWindows.of(Duration.of(5, ChronoUnit.MINUTES))

    val joinOrdersPayments = (order: Order, payment: Payment) => if (payment.status == "PAID") Option(order) else Option.empty[Order]
    val ordersPaid: KStream[OrderId, Order] = ordersStream.join(paymentsStream)(joinOrdersPayments, joinWindow).flatMapValues(maybeOrder => maybeOrder.toIterable)

    // sink
    ordersPaid.to(PayedOrdersTopic) // topic produced -> can now be subscribed from others

    val topology: Topology = builder.build

    // setup for starting kafka application:

    val props = new Properties
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "orders-application")
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.stringSerde.getClass)

    //println(topology.describe())
    val application = new KafkaStreams(topology, props)
    application.start()





    //def main(args: Array[String]): Unit = {
/*        List (
            "orders-by-user",
            "discount-profiles-by-user",
            "discounts",
            "orders",
            "payments",
            "paid-orders"
        ).foreach { topic =>
            println(s"kafka-topics --bootstrap-server localhost:9092 --topic ${topic} --create")
        }*/
    }
}
