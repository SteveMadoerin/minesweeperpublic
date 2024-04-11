package de.htwg.sa.minesweeper

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.sa.minesweeper.Default

class DefaultSpec extends AnyWordSpec {
  
    "it" should {
        "return the same object with multiple call in the same thread" in {
            val singelton1 = Default
            val singelton2 = Default

            singelton1 should be (singelton2)
        }
    }

    "it" should {
        "return the same object with multiple call in different threads" in {
            val singletons = (1 to 4).map(_ => Default)
            val expected = Default
            singletons.foreach(s => s should be (expected))
        }
    }

}