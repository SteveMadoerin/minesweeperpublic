package de.htwg.sa.minesweeper

import de.htwg.sa.minesweeper.ui.TUI
import de.htwg.sa.minesweeper.ui.gui.GUI
import de.htwg.sa.minesweeper.ui.config.Default.{given}
import de.htwg.sa.minesweeper.model.gameComponent.ModelApi
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
import de.htwg.sa.minesweeper.ui.WebGuiApi

object Minesweeper {
    
    def main(args: Array[String]): Unit = {
        System.exit(666)

        /*
        if (args.length >= 1){
            return
        } else {
            
            
            ModelApi() // on Port 8082
            PersistenceApi() // on Port 8083
            ControllerApi() // on Port 8081
            WebGuiApi() // on Port 8080

            GUI().run // on Port 8087
            TUI().run // on Port 8088
        }
        */
        
    }
}
