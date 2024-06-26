package de.htwg.sa.minesweeper.util

import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class MoveSpec extends AnyWordSpec{
    "The Move" should {
        "have a String, and x,y position" in{
            val testMove = Move("open", 2, 4)
            testMove.value should be("open")
            testMove.x should be(2)
            testMove.y should be(4)

        }
    }
}