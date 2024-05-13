import de.htwg.sa.minesweeper.persistence.entity.Game
import slick.lifted.TableQuery

import scala.concurrent.Await

trait IDAO:
    def saveGame(bombs: Int, size: Int, time: Int, board: String): Unit
    def saveField(field: String): Unit
    def savePlayerScore(playerName: String, score: Int): Unit

    def loadGame(): Game
    def loadField(): String


trait IDao2[T, U] {
    def save(t: T): T

    def findAll(): List[T]

    def findById(id: U): T

    def update(t: T): T

    def delete(id: U): Boolean
}

/*class GameDao extends IDao2[Game, Long] {

    override def save(t: Game): Game = ???

    override def findAll(): List[Game] = ???

    override def findById(id: Long): Game = ???

    override def update(t: Game): Game = ???

    override def delete(id: Long): Boolean = ???
}*/

/*import slick.jdbc.PostgresProfile.api._
import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Success, Failure}
import org.slf4j.{Logger, LoggerFactory}
import de.htwg.sa.minesweeper.persistence.entity.Game
import de.htwg.sa.minesweeper.persistence.database.{GameTable, Slick}

val database = Slick().database*/

/*import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
val gameTable = TableQuery(GameTable(_))

val setup: DBIOAction[Unit, NoStream, Effect.Schema] = DBIO.seq(
    gameTable.schema.createIfNotExists
)
database.run(setup)

val tmp = database.run(gameTable.result)*/
/*class GameDao(implicit ec: ExecutionContext) extends IDao2[Game, Long] {
    val logger: Logger = LoggerFactory.getLogger(classOf[GameDao])
    val database = Slick().database
    private val games = TableQuery[GameTable]

    override def save(game: Game): Future[Game] = {
        if (game == null) {
            logger.error("Cannot save null Game object.")
            Future.failed(new IllegalArgumentException("Cannot save null Game object."))
        } else {
            val action = (games returning games.map(_.id) into ((game, id) => game.withId(id))) += game
            database.run(action.asTry).transform {
                case Success(result) => Success(result)
                case Failure(e) =>
                    logger.error(s"Error occurred while saving Game: ${e.getMessage}")
                    Failure(e)
            }
        }
    }

    override def findAll(): Future[Seq[Game]] = {
        database.run(games.result.asTry).transform {
            case Success(result) => Success(result)
            case Failure(e) =>
                logger.error(s"Error occurred while finding all Games: ${e.getMessage}")
                Failure(e)
        }
    }

    override def findById(id: Long): Future[Option[Game]] = {
        database.run(games.filter(_.id === id).result.headOption.asTry).transform {
            case Success(result) => Success(result)
            case Failure(e) =>
                logger.error(s"Error occurred while finding Game by id: ${e.getMessage}")
                Failure(e)
        }
    }

    override def update(game: Game): Future[Game] = {
        val action = games.filter(_.id === game.id).update(game)
        database.run(action.asTry).transform {
            case Success(_) => Success(game)
            case Failure(e) =>
                logger.error(s"Error occurred while updating Game: ${e.getMessage}")
                Failure(e)
        }
    }

    override def delete(id: Long): Future[Boolean] = {
        val action = games.filter(_.id === id).delete
        database.run(action.asTry).transform {
            case Success(count) => Success(count > 0)
            case Failure(e) =>
                logger.error(s"Error occurred while deleting Game: ${e.getMessage}")
                Failure(e)
        }
    }
}

object GameService {
    def saveGame(bombs: Int, size: Int, time: Int, board: String): Unit = {
        val game = new Game(bombs, size, time, board)
        val gameDao = new GameDao
        gameDao.save(game)
    }

    def loadGame(): Game = {
        val gameDao = new GameDao
        gameDao.findAll().head
    }
}*/
/*import concurrent.duration.DurationInt
val gameTable = TableQuery(GameTable(_))
val db = Slick().database
val query = gameTable
val games: Future[Seq[Game]] = db.run(query.result).map(_.map {
    case (id, field1, field2, field3, field4) => GameTable(id, field1, field2, field3, field4)
})
val games2: Seq[Game] = Await.result(games, 1.second)

val game: Future[Option[Game]] = db.run(GameTable.query.filter(_.id === 1).result.headOption)
val game2: Option[Game] = Await.result(game, 1.second)

val game3: Future[Game] = db.run(GameTable.query.filter(_.id === 1).result.head)
val game4: Game = Await.result(game3, 1.second)*/

/*class GameTable(tag: Tag) extends Table[(Option[Int], Int, Int, Int, String)](tag, "Game") {
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def bombs = column[Int]("bombs")
    def size = column[Int]("size")
    def time = column[Int]("time")
    def board = column[String]("board")

    def * = (id, bombs, size, time, board)

}*/

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

case class Person(id: Option[Int], name: String, age: Int)

class People(tag: Tag) extends Table[Person](tag, "people") {
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name: Rep[String] = column[String]("name")
    def age: Rep[Int] = column[Int]("age")

    def * : ProvenShape[Person] = (id.?, name, age) <> ((Person.apply _).tupled, Person.unapply)
}

val people = TableQuery[People]

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait PersonDAO {
    def findById(id: Int): Future[Option[Person]]
    def findAll(): Future[Seq[Person]]
    def insert(person: Person): Future[Int]
    def update(id: Int, person: Person): Future[Int]
    def delete(id: Int): Future[Int]
}

class SlickPersonDAO(db: Database) extends PersonDAO {
    val people = TableQuery[People]

    override def findById(id: Int): Future[Option[Person]] = {
        db.run(people.filter(_.id === id).result.headOption)
    }

    override def findAll(): Future[Seq[Person]] = {
        db.run(people.result)
    }

    override def insert(person: Person): Future[Int] = {
        db.run((people returning people.map(_.id)) += person)
    }

    override def update(id: Int, person: Person): Future[Int] = {
        db.run(people.filter(_.id === id).update(person))
    }

    override def delete(id: Int): Future[Int] = {
        db.run(people.filter(_.id === id).delete)
    }
}

/*val db = Database.forConfig("mydbconfig") // The config would be in your application.conf
val personDAO = new SlickPersonDAO(db)*/

// Example usage
/*val futurePerson = personDAO.findById(42) // Fetch a person with ID 42
val allPeople = personDAO.findAll()       // Fetch all people
val newPersonId = personDAO.insert(Person(None, "Alice", 30)) // Insert a new person
val updatedRows = personDAO.update(42, Person(Some(42), "Alice", 31)) // Update person with ID 42
val deletedRows = personDAO.delete(42) // Delete person with ID 42*/



// GENERIC DAO INTERFACE
trait GenericDaoExample[T, U] {
    def findById(id: U): Future[Option[T]]
    def findAll(): Future[Seq[T]]
    def insert(obj: T): Future[Int]
    def update(id: U, obj: T): Future[Int]
    def delete(id: U): Future[Int]
}

// SLICK POSTGRES
/*class SlickPersonDaoExample(db: Database) extends GenericDaoExample[Person, Long] {
    // ... (implementation details for Slick with PostgreSQL)

    override def findById(id: Long): Future[Option[Person]] = ???

    override def findAll(): Future[Seq[Person]] = ???

    override def insert(obj: Person): Future[Int] = ???

    override def update(id: Long, obj: Person): Future[Long] = ???

    override def delete(id: Int): Future[Int] = ???
}*/

// MONGODB
/*class MongoPersonDAOExample extends GenericDaoExample[Person, Long] {
    // ... (implementation details for MongoDB)

    override def findById(id: Long): Future[Option[Person]] = ???

    override def findAll(): Future[Seq[Person]] = ???

    override def insert(obj: Person): Future[Int] = ???

    override def update(id: Long, obj: Person): Future[Int] = ???

    override def delete(id: Int): Future[Int] = ???
}*/

// DI
/*object ServiceComponent {
    // This could be loaded from configuration
    val usePostgres = true

    val personDAO: GenericDaoExample[Person, Long] = if (usePostgres) {
        new SlickPersonDaoExample(Database.forConfig("postgresDB"))
    } else {
        new MongoPersonDAOExample()
    }
}
*/





