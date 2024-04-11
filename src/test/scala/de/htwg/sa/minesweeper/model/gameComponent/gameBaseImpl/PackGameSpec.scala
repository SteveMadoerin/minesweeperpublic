package de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.PackGame

class PackGameSpec extends AnyWordSpec with Matchers {
  "A PackGame" should {
    "correctly apply a function to each element using map" when {
      "the list is of integers" in {
        val packGame = new PackGame[Int](List(1, 2, 3))
        val mappedGames = packGame.map(_ * 2)
        mappedGames shouldBe List(2, 4, 6)
      }

      "the list is of strings" in {
        val packGame = new PackGame[String](List("a", "b", "c"))
        val mappedGames = packGame.map(_ + "1")
        mappedGames shouldBe List("a1", "b1", "c1")
      }

      "the list is empty" in {
        val packGame = new PackGame[Int](List.empty)
        val mappedGames = packGame.map(_ * 2)
        mappedGames shouldBe empty
      }
    }
  }
}
