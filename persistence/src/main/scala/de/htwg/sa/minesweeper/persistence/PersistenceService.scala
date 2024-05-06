package de.htwg.sa.minesweeper.persistence

import de.htwg.sa.minesweeper.persistence.fileIoComponent.PersistenceApi

import akka.actor._
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import java.net.URLEncoder
import akka.http.scaladsl.Http
import scala.concurrent.Future
import akka.http.scaladsl.model.HttpResponse
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.actor._
import akka.actor.TypedActor.dispatcher                                                                                                            
import concurrent.ExecutionContext.Implicits.global   

import de.htwg.sa.minesweeper.persistence.fileIoComponent.config.Default
import de.htwg.sa.minesweeper.persistence.fileIoComponent.config.Default.{given}

object PersistenceService{
    


    @main def main(): Unit = PersistenceApi().start
    
}