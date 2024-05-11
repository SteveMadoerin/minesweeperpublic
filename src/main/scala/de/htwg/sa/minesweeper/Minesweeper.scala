package de.htwg.sa.minesweeper

import de.htwg.sa.minesweeper.ui.TUI
import de.htwg.sa.minesweeper.ui.gui.GUI
//import de.htwg.sa.minesweeper.ui.config.Default.{given}
import de.htwg.sa.minesweeper.Default.{given}
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
import concurrent.ExecutionContext.Implicits.global   
//import de.htwg.sa.minesweeper.ui.WebGuiApi

object Minesweeper {
    
    def main(args: Array[String]): Unit = {

        if (args.length >= 1){
            return
        } else {
            
            
            ModelApi() // on Port 9082
            PersistenceApi() // on Port 9083
            ControllerApi() // on Port 9081
            //WebGuiApi() // on Port 9080

            GUI().run // on Port 9087
            TUI().run // on Port 9088
        }
        
    }
}
