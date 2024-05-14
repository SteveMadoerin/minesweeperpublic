package de.htwg.sa.minesweeper.persistence.database


import scala.language.postfixOps

import scala.concurrent.{Await, Future}

trait IDaoNew [T, U] {

    def save(obj: T): T

    def findAll(): Seq[T]

    def findById(id: U): T

    def update(id: U , obj: T): T

    def delete(id: U): Boolean
}

