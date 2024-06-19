package de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl

import scala.language.postfixOps

trait IDao [T, U] {

    def save(obj: T): T
    def findAll(): Seq[T]
    def findById(id: U): T
    def update(id: U , obj: T): T
    def delete(id: U): Boolean
}

