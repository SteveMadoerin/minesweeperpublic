package de.htwg.se.minesweeper.util

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class ObserverSpec extends AnyWordSpec{

    "The Observable" should{
        val observable = new Observable {}

        "have no subscribers" in {
            assert(observable.subscribers.isEmpty)
        }

        "allow an Observer to be added" in {
            val observer = new Observer{
                override def update(e: NewEvent): Boolean = false
            }
            observable.add(observer)
            assert(observable.subscribers.contains(observer))
        }

        "allow an Observer to be removed" in {
            val observer = new Observer{
                override def update(e: NewEvent): Boolean = false
            }
            observable.add(observer)
            observable.remove(observer)
            assert(!observable.subscribers.contains(observer))
        }

        "notify its observers when an update occurs" in {
            var notified = false
            val observer = new Observer{
                override def update(e: NewEvent) = 
                {
                    notified = true
                    true
                }
            }
            observable.add(observer)
            observable.notifyObservers(NewEvent.Next)
            assert(notified)
        }



    }




}