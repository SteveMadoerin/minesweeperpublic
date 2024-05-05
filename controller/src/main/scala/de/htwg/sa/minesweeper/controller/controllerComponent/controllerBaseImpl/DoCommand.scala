package de.htwg.sa.minesweeper.controller.controllerComponent.controllerBaseImpl

import de.htwg.sa.minesweeper.model.gameComponent._
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl._

import de.htwg.sa.minesweeper.util.{Command, UndoRedoManager, Move}
import de.htwg.sa.minesweeper.controller.controllerComponent.config.Default.{given}
import de.htwg.sa.minesweeper.util.RestUtil
import de.htwg.sa.minesweeper.entity.FieldDTO
import de.htwg.sa.minesweeper.entity.MatrixDTO


class DoCommand(move: Move) extends Command[FieldDTO]:

    var saveFieldUndo: FieldDTO = FieldDTO(MatrixDTO(Vector.tabulate(9, 9) {(row, col) => "E"}), MatrixDTO(Vector.tabulate(9, 9) {(row, col) => "E"}))
    var saveFieldRedo: FieldDTO = FieldDTO(MatrixDTO(Vector.tabulate(9, 9) {(row, col) => "E"}), MatrixDTO(Vector.tabulate(9, 9) {(row, col) => "E"}))

    override def noStep(field: FieldDTO): FieldDTO = field
    override def doStep(field: FieldDTO): FieldDTO = 
        move.value match {
            case "open" => 
                val extractedSymbol = RestUtil.requestShowInvisibleCell(move.y, move.x, field)
                val returnField = RestUtil.requestFieldPut(extractedSymbol, move.y, move.x, field)
                returnField
            case "recursion" => 
                saveFieldUndo = field
                val returnRecField = RestUtil.requestRecursiveOpen(move.x, move.y, field)
                saveFieldRedo = returnRecField
                returnRecField
            
            case "flag" => RestUtil.requestFieldPut("F",move.y, move.x, field)
            case "unflag" => RestUtil.requestFieldPut("~",move.y, move.x, field)
          }
    
    override def undoStep(field: FieldDTO): FieldDTO = 
        move.value match {
            case "unflag" => RestUtil.requestFieldPut("F",move.y, move.x, field)
            case "flag" => RestUtil.requestFieldPut("~",move.y, move.x, field)
            case "open" => RestUtil.requestFieldPut("~",move.y, move.x, field)
            case "recursion" => saveFieldUndo
        }

    override def redoStep(field: FieldDTO): FieldDTO = 
        move.value match {
            case "open" => 
                val extractedSymbol = RestUtil.requestShowInvisibleCell(move.y, move.x, field)
                val returnField = RestUtil.requestFieldPut(extractedSymbol, move.y, move.x, field)
                returnField
            
            case "flag" => RestUtil.requestFieldPut("F",move.y, move.x, field)
            case "unflag" => RestUtil.requestFieldPut("~",move.y, move.x, field)
            case "recursion" => saveFieldRedo
        }
