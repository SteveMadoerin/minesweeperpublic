package de.htwg.se.minesweeper.model.gameComponent

import de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl.{Symbols,Matrix}


trait IGame {
        
    def premierMove(x: Int, y: Int, field: IField): IField
    def openNew(x: Int, y: Int, field: IField): IField
    def replaceBomb(x: Int, y: Int, field: IField): IField
   
    def calcFlag(visibleMatrix: Matrix[Symbols]): Int
    def addCoveredAndFlag(visibleMatrix: Matrix[Symbols]): Int
    def calcMineAndFlag(visibleMatrix: Matrix[Symbols]): Int
    def calcAdjacentMines(row: Int, col: Int, side: Int, invisibleMatrix: Matrix[Symbols]): Int
    def calcWonOrLost(visibleMatrix: Matrix[Symbols], mines: Int): Boolean

    def isValid(row: Int, col: Int, side: Int): Boolean

    def initializeAdjacentNumbers(matrix: Matrix[Symbols]): Matrix[Symbols]
    def intitializeBombs(matrix: Matrix[Symbols], bombs: Int): Matrix[Symbols]

    //def handleGameState(state: String): Unit // hopefully it works 
    def checkExit(status: String): Boolean
    //def checkExit: Boolean

    //def setState(newBoard: String): Unit

    def helpMessage: Unit
    //def updateTime(newTime: Int): Unit

    def side: Int
    def bombs: Int
    def board: String
    def hyperField: IField
    def time: Int

    def prepareBoard(s: Option[String], game: IGame): (IField, IGame)
    def createField: IField

}

trait IField{

    def putFlag(x: Int, y: Int): IField
    def removeFlag(x: Int, y: Int): IField
    //def open(x: Int, y: Int, spiel: IGame): IField
    def open(x: Int, y: Int, spiel: IGame): (IGame, IField)
    def gameOverField: IField
    def reveal: IField

    def put(symbol: Symbols, x: Int, y: Int): IField
    def showVisibleCell(x: Int, y: Int): Symbols
    def showInvisibleCell(x: Int, y: Int): Symbols
    def openNewXXX(x: Int, y: Int, field: IField): IField
    def isValidF(row: Int, col: Int, side: Int): Boolean
    def recursiveMadness(x: Int, y: Int, field: IField): IField
    def matrix: Matrix[Symbols]
    def hidden: Matrix[Symbols]
    def toString: String
}




