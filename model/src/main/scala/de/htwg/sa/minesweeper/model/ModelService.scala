package de.htwg.sa.minesweeper.model

import de.htwg.sa.minesweeper.model.gameComponent.{ModelApi, ModelApiTest}
import de.htwg.sa.minesweeper.model.gameComponent.config.Default.given

import scala.io.StdIn.readLine

object ModelService {
    def main(args: Array[String]): Unit = {
        //ModelApi().start() // on Port 9082
        ModelApiTest().start
        println("Press RETURN to stop...")
        readLine()
        
    }
}