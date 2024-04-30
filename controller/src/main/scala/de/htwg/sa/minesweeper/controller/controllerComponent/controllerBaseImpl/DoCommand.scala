package de.htwg.sa.minesweeper.controller.controllerComponent.controllerBaseImpl

import de.htwg.sa.minesweeper.model.gameComponent._
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl._

import de.htwg.sa.minesweeper.util.{Command, UndoRedoManager, Move}
import de.htwg.sa.minesweeper.controller.controllerComponent.config.Default.{given}
import de.htwg.sa.minesweeper.util.RestUtil


class DoCommand(move: Move) extends Command[IField]:

    var saveFieldUndo: IField = Field(Matrix(Vector.tabulate(9, 9) {(row, col) => "E"}), Matrix(Vector.tabulate(9, 9) {(row, col) => "E"}))
    var saveFieldRedo: IField = Field(Matrix(Vector.tabulate(9, 9) {(row, col) => "E"}), Matrix(Vector.tabulate(9, 9) {(row, col) => "E"}))

    //def initField(using lefield: IField) = lefield
    override def noStep(field: IField): IField = field
    override def doStep(field: IField): IField = 
        move.value match {
            case "open" => 
                //val extractedSymbol = field.showInvisibleCell(move.y, move.x)
                val extractedSymbol = RestUtil.requestShowInvisibleCell(move.y, move.x, field)
/*                 val returnField = field.put(extractedSymbol, move.y, move.x) */
                val returnField = RestUtil.requestFieldPut(extractedSymbol, move.y, move.x, field)
                returnField
            case "recursion" => 
                saveFieldUndo = field
                //val returnRecField = field.recursiveOpen(move.x,move.y,field)
                val returnRecField = RestUtil.requestRecursiveOpen(move.x, move.y, field)
                saveFieldRedo = returnRecField
                returnRecField
            
            case "flag" => /*field.put("F", move.y, move.x)*/ RestUtil.requestFieldPut("F",move.y, move.x, field)
            case "unflag" => /*field.put("~", move.y, move.x)*/ RestUtil.requestFieldPut("~",move.y, move.x, field)
          }
    
    override def undoStep(field: IField): IField = 
        move.value match {
            case "unflag" => /* field.put("F", move.y, move.x) */ RestUtil.requestFieldPut("F",move.y, move.x, field)
            case "flag" => /* field.put("~", move.y, move.x) */ RestUtil.requestFieldPut("~",move.y, move.x, field)
            case "open" => /* field.put("~", move.y, move.x) */ RestUtil.requestFieldPut("~",move.y, move.x, field)
            case "recursion" => saveFieldUndo
        }

    override def redoStep(field: IField): IField = 
        move.value match {
            case "open" => 
                val extractedSymbol = /* field.showInvisibleCell(move.y, move.x) */RestUtil.requestShowInvisibleCell(move.y, move.x, field)
                val returnField = /* field.put(extractedSymbol, move.y, move.x) */ RestUtil.requestFieldPut(extractedSymbol, move.y, move.x, field)
                returnField
            
            case "flag" => /* field.put("F", move.y, move.x) */ RestUtil.requestFieldPut("F",move.y, move.x, field)
            case "unflag" => /* field.put("~", move.y, move.x) */ RestUtil.requestFieldPut("~",move.y, move.x, field)
            case "recursion" => saveFieldRedo
        }
