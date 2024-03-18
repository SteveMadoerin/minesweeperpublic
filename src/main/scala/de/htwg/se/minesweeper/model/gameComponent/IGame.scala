package de.htwg.se.minesweeper.model.gameComponent

import de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl.{Symbols, Status}


trait IGame {
    
    def helpMessage: Unit
    def getSide: Int
    def getBombs: Int
    def getStatus: Status
    
    def premierMove(x: Int, y: Int, field: IField): IField
    def openNew(x: Int, y: Int, field: IField): IField
    def replaceBomb(x: Int, y: Int, field: IField): IField
   
    def calcFlag(visibleMatrix: IMatrix[Symbols]): Int
    def addCoveredAndFlag(visibleMatrix: IMatrix[Symbols]): Int
    def calcMineAndFlag(visibleMatrix: IMatrix[Symbols]): Int
    def calcAdjacentMines(row: Int, col: Int, side: Int, invisibleMatrix: IMatrix[Symbols]): Int
    def calcWonOrLost(visibleMatrix: IMatrix[Symbols], mines: Int): Boolean

    def isValid(row: Int, col: Int, side: Int): Boolean

    def initializeAdjacentNumbers(matrix: IMatrix[Symbols]): IMatrix[Symbols]
    def intitializeBombs(matrix: IMatrix[Symbols], bombs: Int): IMatrix[Symbols]

    def handleGameState(state: String): Unit
    def checkExit: Boolean

    def getGame: IGame
    def setBombs(bombs: Int): Unit
    def setSide(side: Int): Unit
    def setState(newState: Status): Unit
    def setSideAndBombs(side: Int, bombs: Int): Unit

    def getTime: Int
    def setTime(newTime: Int): Unit

    // NEW
    def prepareBoard(s: Option[String], realGame: IGame): IField
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
    def get(x: Int, y: Int): Symbols
    def getVisible(x: Int, y: Int): Symbols
    def getInvisible(x: Int, y: Int): Symbols
    def openNewXXX(x: Int, y: Int, field: IField): IField
    def isValidF(row: Int, col: Int, side: Int): Boolean
    def recursiveMadness(x: Int, y: Int, field: IField): IField
    def getFieldSize: Int
    def getMatrix: IMatrix[Symbols]
    def getHidden: IMatrix[Symbols]
    def getVisibleMatrix: IMatrix[Symbols]
    def getInvisibleMatrix: IMatrix[Symbols]
    def getField: IField
    def setInvisibleMatrix(matrix: IMatrix[Symbols]): Unit
    def setVisibleMatrix(matrix: IMatrix[Symbols]): Unit

    def toString: String
}

trait IMatrix[T] {
    def getSize: Int
    def cell(row: Int, col: Int): T
    def row(row: Int): Vector[T]
    def replaceCell(row: Int, col: Int, cell: T): IMatrix[T]
    def fill(filling: T): IMatrix[T]
    def getRealMatrix: IMatrix[T]
    def toString: String
}



