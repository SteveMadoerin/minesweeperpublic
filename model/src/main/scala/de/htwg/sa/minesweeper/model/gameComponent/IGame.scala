package de.htwg.sa.minesweeper.model.gameComponent

import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.Matrix


trait IGame {
        
    def premierMove(x: Int, y: Int, field: IField): IField
    def replaceBomb(x: Int, y: Int, field: IField): IField
   
    def calcFlag(visibleMatrix: Matrix[String]): Int
    def calcMineAndFlag(visibleMatrix: Matrix[String]): Int
    def calcX(symbols: String)(visibleMatrix: Matrix[String]): Int
    def calcAdjacentMines(row: Int, col: Int, side: Int, invisibleMatrix: Matrix[String]): Int
    def calcWonOrLost(visibleMatrix: Matrix[String], mines: Int): Boolean

    def isValid(row: Int, col: Int, side: Int): Boolean

    def initializeAdjacentNumbers(matrix: Matrix[String]): Matrix[String]
    def intitializeBombs(matrix: Matrix[String], bombs: Int): Matrix[String]

    def checkExit(status: String): Boolean
    def helpMessage: String

    def side: Int
    def bombs: Int
    def board: String
    def time: Int

    def prepareBoard(s: Option[String])(game: IGame): (IField, IGame) // currying

    def gameToJson: String
    def jsonToGame(jsonString: String): IGame
}

trait IField{

    def putFlag(x: Int, y: Int): IField
    def removeFlag(x: Int, y: Int): IField
    def open(x: Int, y: Int, spiel: IGame): (IGame, IField)
    def gameOverField: IField
    def reveal: IField

    def put(symbol: String, x: Int, y: Int): IField
    def showVisibleCell(x: Int, y: Int): String
    def showInvisibleCell(x: Int, y: Int): String
    def openNew(x: Int, y: Int, field: IField): IField
    def isValidF(row: Int, col: Int, side: Int): Boolean
    def recursiveOpen(x: Int, y: Int, field: IField): IField
    def matrix: Matrix[String]
    def hidden: Matrix[String]
    def toString: String

    //def fieldToHtml: String
    def fieldToJson(fieldInput: IField): String
    def fieldToJson: String
    def jsonToField(jsonString: String): IField
}




