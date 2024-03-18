object HelloTest{
    def main(args: Array[String]): Unit = {
        printTest(1,2,7,8,9)
    }
    def printTest(multipleInts: Int*): Unit = {
        multipleInts.map(println)
    }
}

//HelloTest.main(Array())