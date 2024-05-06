package de.htwg.sa.minesweeper.persistence.entity




trait IGame {
    
    def side: Int
    def bombs: Int
    def board: String
    def time: Int

    def gameToJson: String
    def jsonToGame(jsonString: String): IGame
}

trait IField{

    def matrix: Matrix[String]
    def hidden: Matrix[String]
    def toString: String

    def showInvisibleCell(x: Int, y: Int): String
    def showVisibleCell(x: Int, y: Int): String

    //def fieldToHtml: String
    def fieldToJson(fieldInput: IField): String
    def fieldToJson: String
    def jsonToField(jsonString: String): IField
}




