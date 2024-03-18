package de.htwg.se.minesweeper

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class MinesweeperSpec extends AnyWordSpec{

    "The Minesweeper game" should {

        "do nothing " in {
            Minesweeper.main(Array[String]("s"))
        }

    }
}