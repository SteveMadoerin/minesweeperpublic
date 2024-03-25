package de.htwg.se.minesweeper.model.gameComponent

import de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl.{Symbols,Matrix}


trait IGame {
    
    def helpMessage: Unit
    def getSide: Int
    def getBombs: Int
    def _board: String
    
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

    def handleGameState(state: String): Unit
    def checkExit: Boolean

    def setState(newBoard: String): Unit
    def setSideAndBombs(side: Int, bombs: Int): Unit

    def getTime: Int
    def setTime(newTime: Int): Unit

    // NEW
    def prepareBoard(s: Option[String], realGame: IGame): IField
    //def prepareBoard(s: Option[String]): IField
    def createField: IField
    def getField: IField
    // NEW

}

trait IField{

    def putFlag(x: Int, y: Int): IField
    def removeFlag(x: Int, y: Int): IField
    def open(x: Int, y: Int, spiel: IGame): IField
    def gameOverField: IField
    def reveal: IField

    def put(symbol: Symbols, x: Int, y: Int): IField
    def showVisibleCell(x: Int, y: Int): Symbols
    def showInvisibleCell(x: Int, y: Int): Symbols
    def openNewXXX(x: Int, y: Int, field: IField): IField
    def isValidF(row: Int, col: Int, side: Int): Boolean
    def recursiveMadness(x: Int, y: Int, field: IField): IField
    def _matrix: Matrix[Symbols]
    def _hidden: Matrix[Symbols]

    def toString: String
}




