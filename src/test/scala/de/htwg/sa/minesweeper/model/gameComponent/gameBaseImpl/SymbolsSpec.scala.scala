package de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl

import org.scalatest.matchers.should.Matchers._
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.Symbols
import org.scalatest.wordspec.AnyWordSpec

class SymbolsSpec extends AnyWordSpec {

  "A Symbol" when {
    "toString is called" should {
      "return a string representation for Covered" in {
        Symbols.Covered.toString should be ("~")
      }
      "return a string representation for F" in {
        Symbols.F.toString shouldBe "F"
      }
      "return a string representation for Bomb" in {
        Symbols.Bomb.toString shouldBe "*"
      }
      "return a string representation for Empty" in {
        Symbols.Empty.toString shouldBe " "
      }
      "return a string representation for Zero" in {
        Symbols.Zero.toString shouldBe "0"
      }
      "return a string representation for One" in {
        Symbols.One.toString shouldBe "1"
      }
      "return a string representation for Two" in {
        Symbols.Two.toString shouldBe "2"
      }
      "return a string representation for Three" in {
        Symbols.Three.toString shouldBe "3"
      }
      "return a string representation for Four" in {
        Symbols.Four.toString shouldBe "4"
      }
      "return a string representation for Five" in {
        Symbols.Five.toString shouldBe "5"
      }
      "return a string representation for Six" in {
        Symbols.Six.toString shouldBe "6"
      }
      "return a string representation for Seven" in {
        Symbols.Seven.toString shouldBe "7"
      }
      "return a string representation for Eight" in {
        Symbols.Eight.toString shouldBe "8"
      }
      "return a string representation for E" in {
        Symbols.E.toString shouldBe "E"
      }
    }
  }
}

