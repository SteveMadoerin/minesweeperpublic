package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl


import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec


class FieldSpec extends AnyWordSpec 
{
    "A minesweeper Field" when 
    {
        "is empty" should 
        {
            val fieldOne = new Field(1, Symbols.Empty)
            val field2 = new Field(2, Symbols.Empty)
            val field3 = new Field(3, Symbols.Empty)

            val endl = sys.props("line.separator")
            "have a bar as String of form '+---+---+---+'" in 
            {
                field3.bar() should be("+---+---+---+" + endl)
            }
            "also have a bar as String of form '+---+---+---+'" in 
            {
                field2.bar() should be("+---+---+---+" + endl)
            }
            "still have a bar as String of form '+---+---+---+'" in 
            {
                fieldOne.bar() should be("+---+---+---+" + endl)
            }
            "have a scalable bar" in 
            {
                fieldOne.bar(1,1) should be("+-+" + endl)
                field2.bar(2,1) should be("+--+" + endl)
                field2.bar(1,2) should be("+-+-+" + endl)
                field3.bar(3,3) should be("+---+---+---+" + endl)
            }
            "have cells as String of form '|   |   |   |'" in 
            {
                field3.cells(0) should be ("|   |   |   |" + endl)
            }
            "have scalable cells" in 
            {
                fieldOne.cells(0, 1) should be("| |" + endl)
                field2.cells(0, 1) should be("| | |" + endl)
                field3.cells(2,3) should be("|   |   |   |" + endl)
                fieldOne.cells(0,3) should be("|   |" + endl)
            }
            "have a mesh in the form" + 
            "+-+" + 
            "| |" + 
            "+-+" in 
            {
                fieldOne.mesh(1) should be("+-+" + endl + "| |" + endl + "+-+" + endl)
                field3.mesh(1) should be("+-+-+-+" + endl + "| | | |" + endl + "+-+-+-+" + endl + "| | | |" + endl + "+-+-+-+" + endl + "| | | |" + endl + "+-+-+-+" + endl )
                field2.mesh(1) should be("+-+-+" + endl + "| | |" + endl + "+-+-+" + endl + "| | |" + endl + "+-+-+" + endl)
            }
            "filled with Empty" should 
            {
                val field = new Field(3, Symbols.Empty)
                "be empty initially with Field(Symbols.Empty)" in 
                {
                     field.toString() should be("+---+---+---+" + endl + "|   |   |   |" + endl + "+---+---+---+" + endl + "|   |   |   |" + endl + "+---+---+---+" + endl + "|   |   |   |" + endl + "+---+---+---+" +endl)
                }
            }
            "field open" should {
                var testSpiel = new Game(Status.Playing)
                testSpiel.bombs = 9
                testSpiel.side = 3

                val sicht = new Matrix(4, Symbols.Covered)
                val unsicht = new Matrix(4, Symbols.Eight)
                val testField = new Field(sicht, unsicht)



                "return a new field" in{
                    val resultField = testField.open(1, 1, testSpiel)
                    resultField.getVisible(1,1) should be (Symbols.Eight)

                }
            }

            "field getInvisible" should{

                val sicht69 = new Matrix(4, Symbols.Covered)
                val unsicht69 = new Matrix(4, Symbols.Eight)
                val testField69 = new Field(sicht69, unsicht69)
                
                "get cell of invisible matrix" in{
                    val testResult = testField69.getInvisible(1, 1) 
                    testResult should be (Symbols.Eight)

                }
            }

            "field getVisible" should{

                val sicht70 = new Matrix(4, Symbols.Covered)
                val unsicht70 = new Matrix(4, Symbols.Eight)
                val testField70 = new Field(sicht70, unsicht70)
                
                "get cell of visible matrix" in{
                    val testResult = testField70.getVisible(1, 1) 
                    testResult should be (Symbols.Covered)

                }
            }

            "field gameOverfield" should{
                val sicht71 = new Matrix(4, Symbols.Covered)
                val unsicht71 = new Matrix(4, Symbols.Eight)
                val testField71 = new Field(sicht71, unsicht71)

                "return a new field" in{
                    val resultField = testField71.gameOverField
                    resultField.getVisible(1,1) should be (Symbols.Eight)

                }
            }

            "def openNewXXX" should{
                val sicht72 = new Matrix(4, Symbols.Covered)
                val unsicht72 = new Matrix(4, Symbols.Eight)
                val testField72 = new Field(sicht72, unsicht72)

                "return a new field" in{
                    val resultField = testField72.openNewXXX(1, 1, testField72)
                    resultField.getVisible(1,1) should be (Symbols.Eight)

                }
            }

            " test recusiveMadness" should {
                val sicht74 = new Matrix(5, Symbols.Covered)
                val unsicht74 = new Matrix(5, Symbols.Eight)
                val unsichtWithZero = unsicht74.replaceCell(2, 2, Symbols.Zero)
                val unsichtWithZero2 = unsichtWithZero.replaceCell(1, 1, Symbols.Zero)
                val unsichtWithZero3 = unsichtWithZero2.replaceCell(3, 3, Symbols.Zero)
                val unsichtWithZero4 = unsichtWithZero3.replaceCell(2, 3, Symbols.Zero)
                val unsichtWithZero5 = unsichtWithZero4.replaceCell(3, 2, Symbols.Zero)
                val unsichtWithZero6 = unsichtWithZero5.replaceCell(1, 2, Symbols.Zero)
                val unsichtWithZero7 = unsichtWithZero6.replaceCell(2, 1, Symbols.Zero)
                val unsichtWithZero8 = unsichtWithZero7.replaceCell(1, 3, Symbols.Zero)
                val unsichtWithZero9 = unsichtWithZero8.replaceCell(3, 1, Symbols.Zero)
                val testField74 = new Field(sicht74, unsichtWithZero9)

                "return a new field" in{
                    val resultField = testField74.recursiveMadness(2, 2, testField74)
                    resultField.getVisible(2,2) should be (Symbols.Zero)
                    resultField.getVisible(1,1) should be (Symbols.Zero)
                    resultField.getVisible(0,0) should be (Symbols.Eight)

                }
            }

            "func getFieldSize" should {
                val sicht75 = new Matrix(4, Symbols.Covered)
                val unsicht75 = new Matrix(4, Symbols.Eight)
                val testField75 = new Field(sicht75, unsicht75)

                "return a new field" in{
                    val resultField = testField75.getFieldSize
                    resultField should be (4)

                }

            }

            "def getField" should {
                val sicht76 = new Matrix(4, Symbols.Covered)
                val unsicht76 = new Matrix(4, Symbols.Eight)
                val testField76 = new Field(sicht76, unsicht76)

                "return the same field" in{
                    val resultField = testField76.getField
                    resultField should be (testField76)
                }
            }


        }
    }
}