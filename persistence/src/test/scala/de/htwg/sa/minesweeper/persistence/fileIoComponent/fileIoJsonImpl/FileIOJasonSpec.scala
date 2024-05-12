package de.htwg.sa.minesweeper.persistence.fileIoComponent.fileIoJsonImpl

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
/*
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.{Field, Game, Matrix, Playfield}
import de.htwg.sa.minesweeper.persistence.fileIoComponent.fileIoJsonImpl.FileIO
import de.htwg.sa.minesweeper.model.gameComponent.{IGame, IField}
import de.htwg.sa.minesweeper.model.gameComponent.config.Default
import java.io._
import java.nio.file.{Files, Path, Paths}
import play.api.libs.json.{Json, JsArray}

*/
class FileIOJasonSpec extends AnyWordSpec {
  "A FileIO" when {

/*     "save and load Game" should {
      val fileIO1 = new FileIO()
      var game: IGame = Default.prepareGame(10, 10, 10)

      "save the game status, bombs, side and time and reload it" in {
        fileIO1.saveGame(game)
        val loadGameTest = fileIO1.loadGame
        loadGameTest.get.bombs should be (10)
        loadGameTest.get.side should be (10)
        loadGameTest.get.time should be (10)
      }
    } */

/*     "save and load Field" should {
      val fileIO2 = new FileIO()
      var field: IField = new Field(1, "~")
      field = field.put("*", 0, 0)

      "save the field and reload it" in {
        fileIO2.saveField(field)
        val loadFieldTest = fileIO2.loadField.get
        loadFieldTest.matrix.size should be (1)
      }
    }
    
    "saving a player score" should {
      val fileIo9 = new FileIO()
      "append the new score to the JSON file if it exists" in {
        val filePath = "C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\highscoretest.json"
        val tempFile = new File(filePath)

        // Initially write an empty JSON array to the file to simulate existing scores
        val writer = new PrintWriter(tempFile)
        writer.write(Json.prettyPrint(JsArray()))
        writer.close()

        // Call the method to append a new score
        fileIo9.savePlayerScore("Alice", 100, tempFile.getPath)

        // Verify the file now contains the new score
        val source = scala.io.Source.fromFile(tempFile)
        val content = try source.mkString finally source.close()
        val json = Json.parse(content)
        val scoresArray = json.as[JsArray]

        scoresArray.value.length should be(1)
        (scoresArray(0) \ "player").as[String] should be("Alice")
        (scoresArray(0) \ "score").as[Int] should be(100)

        // Clean up the temporary file and directory
        tempFile.delete()
      }

      "create a new JSON file with the score if it does not exist" in {
        val filePath = "C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\highscoretest2.json"
        val tempFilePath = "C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\highscoretest2.json"
        val fileIo10 = new FileIO()
        // Ensure the file does not exist
        new File(tempFilePath).delete()

        // Call the method to create a new file with the score
        fileIo10.savePlayerScore("Bob", 200, tempFilePath)

        // Verify the file was created and contains the score
        val source = scala.io.Source.fromFile(tempFilePath)
        val content = try source.mkString finally source.close()
        val json = Json.parse(content)
        val scoresArray = json.as[JsArray]

        scoresArray.value.length should be(1)
        (scoresArray(0) \ "player").as[String] should be("Bob")
        (scoresArray(0) \ "score").as[Int] should be(200)

        // Clean up the temporary file and directory
        new File(tempFilePath).delete()
      }
    }
    */

  }
}