import de.htwg.se.minesweeper.Default
object HelloTest{
    def main(args: Array[String]): Unit = {
        printTest(1,2,7,8,9)
    }
    def printTest(multipleInts: Int*): Unit = {
        multipleInts.map(println)
    }
}

//HelloTest.main(Array())

// Traits with Parameter

trait Base(val msg: String)
class A extends Base("Hello")
class B extends Base("Velo")

val a = new A
val b = new B
val teststringA = a.msg
val teststringB = b.msg


// SE-09-GUI

def f1(x: Int): Int = x + 1

val resF1 = f1{5}
val resF2 = f1(5)

// Anonymous Functions or Function Literals

val resA1 = (x: Int) => x + 1

// Higher Order Functions

def f(x: Int) = x + 1

val numbers = Vector(1,2,3,4,5)

numbers.foreach((x: Int) => println(f(x)) )

// other higher order functions:
val test1 = numbers.map((x: Int) => f(x))
val nrTest = numbers
val test2 = numbers.reduce((x: Int, y: Int) => x + y)
val test3 = numbers.filter((x: Int) => x % 2 == 0)
val test4 = numbers.fold(2)((x: Int, y: Int) => x + y)

// Syntactic Sugar

val numbers2 = List(1,2,3,4,5)

numbers.foreach((x: Int) => print(x) )
numbers.foreach(x => print(x))
numbers foreach print

// map

numbers2.map(x => x + 1)
numbers2.map(_ + 1)
numbers2.map(x => f(x))
numbers2.map(f(_))
numbers2.map(f)
numbers map f

// Chaning Higher Order Functions

List(-10, -5, 0, 5, 10) filter (x => x > 0) map (x => x * x) sortWith (_ > _) foreach (println)
List(-10, -5, 0, 5, 10) filter (_ > 0) map (x => x * x) sortWith (_ > _) foreach (println)
List(-10, -5, 0, 5, 10)
    .filter(_ > 0)
    .map(x => x * x)
    .sortWith(_ > _)
    .foreach (println)

List(-10, -5, 0, 5, 10) map (x => x * x) filter (_ > 0) sortWith (_ > _) foreach (println)

// Anonymous Classes

val f1 = Foo {
    println("hello from 'f1' instance")
    "this is the result of the block of code"
}

case class Foo[A, B](f: A => B)

// Closures

// a closed term is a function without free variables
def f5(x: Int) = x + 1

// a open term is a function with free variables
var y = 33
def f99(x: Int) = x + y

// a closure is function value of open term -> closure closes open term at runtime, by capturing bindings of it´s free variables
val resF6 = f99(5)
y = 11
f99(5)

// Currying
def f3(c: Int) = (x: Int) => x + c

f3(1)(5)
f3(10)(5)
f3(10){5}
f3(10){
    val z = 4
    f(z)
}
f3(10)

// Partially Applied Functions
def f4 = f3(10)
f4(5)

// Putting it together

// A Closure, a curried function
def f5(op:(Int, Int) => Int) (x: Int, y: Int) = op(x, y)

def add(x: Int, y: Int) = x + y

f5(add)(5, 9)

// A partially applied function
def f6 = f5(add) _
f6(5, 7)

// A practical Example: merge sort

def msort[T](less: (T, T) => Boolean) (xs: List[T]): List[T] = {
    def merge(xs: List[T], ys: List[T]): List[T] = 
        (xs, ys) match {
        case (Nil, _) => ys
        case (_, Nil) => xs
        case (x :: xs1, y :: ys1) => 
            if (less(x, y)) x :: merge(xs1, ys)
            else y :: merge(xs, ys1)
    }
    val n = xs.length / 2
    if (n == 0) xs
    else {
        val (ys, zs) = xs splitAt n
        merge(msort(less)(ys), msort(less)(zs))
    }
}

def intsort = msort((x: Int, y: Int) => x < y) _
intsort(List(9,2,5,7,3,8))

def reverseintsort = msort((x: Int, y: Int) => x > y) _
reverseintsort(List(9,2,5,7,3,8))

def stringsort = msort((s1: String, s2: String) => s1.length < s2.length) _
stringsort(List("coffee", "tea", "beer", "orangejuice"))

// An even more practical Example:

def sum(list:List[Int]): Int = list match {
    case Nil => 0
    case x :: xs => x + sum(xs)
}

sum(List(1,2,3,4,5))

def product(list:List[Int]): Int = list match {
    case Nil => 1
    case x :: xs => x * product(xs)
}

product(List(1,2,3,4,5))

def sum2(list:List[Int]): Int = list.foldLeft(0)(_ + _)
sum2(List(1,2,3,4,5))
List(1,2,3,4,5).foldLeft(0)(_ + _)


def product2(list:List[Int]): Int = list.foldLeft(1)(_ * _)
product2(List(1,2,3,4,5))
List(1,2,3,4,5).foldLeft(1)(_ * _)


// Partial Functions:
val squareRoot: PartialFunction[Double, Double] = {
    case x if x >= 0 => Math.sqrt(x)
}   

// a Partial Function has the method isDefined
squareRoot.isDefinedAt(2)
squareRoot.isDefinedAt(-2)



/* var testGame1 = Default.prepareGame
val status = testGame1._board
testGame1.handleGameState("Lost")
testGame1.handleGameState("Won")
testGame1._board
testGame1.bombs
testGame1.side
testGame1.setSideAndBombs(11, 11)
testGame1._board
testGame1.bombs
testGame1.side */

trait Pizza {
  val name: String
}

case class SmallPizza(name: String) extends Pizza

val pizza = SmallPizza("small")
val pizzaNameAsString = pizza.name

val pizzaInterface: Pizza = SmallPizza("small")
val pizzaInterfaceNameAsString = pizzaInterface.name

case class Spiel(side : Int, bombs : Int)

val testSpiel1 = Spiel(9, 10)
val testSpiel2 = testSpiel1.copy(side = 11)
// make change the side via setter
// not possible heretestSpiel1.side = 33
testSpiel1
testSpiel2
testSpiel1.side
//val testSpiel4 = testSpiel13

case class Settings(var side: Int, var bombs: Int)

var testSettings1 = Settings(9, 10)
val testSettings2 = testSettings1.bombs = 33

var time = Vector(0)
// update the time
time.updated(0, 1)

val zeit = Vector(0)

zeit.updated(0, 1)

// get the time
zeit(0)
