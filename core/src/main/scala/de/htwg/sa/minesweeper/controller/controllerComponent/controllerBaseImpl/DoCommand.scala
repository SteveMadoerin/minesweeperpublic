package controller.controllerComponent.controllerBaseImpl

import model.gameComponent._
import model.gameComponent.gameBaseImpl._

import de.htwg.sa.minesweeper.util.{Command, UndoRedoManager, Move}
//import Default.{given}
import model.gameComponent.gameBaseImpl.Module.{given}


class DoCommand(move: Move) extends Command[IField]:

    var saveFieldUndo: IField = initField
    var saveFieldRedo: IField = initField

    def initField(using lefield: IField) = lefield
    override def noStep(field: IField): IField = field
    override def doStep(field: IField): IField = 
        move.value match {
            case "open" => 
                val extractedSymbol = field.showInvisibleCell(move.y, move.x)
                val returnField = field.put(extractedSymbol, move.y, move.x)
                returnField
            case "recursion" => 
                saveFieldUndo = field
                val returnRecField = field.recursiveOpen(move.x,move.y,field)
                saveFieldRedo = returnRecField
                returnRecField
            
            case "flag" => field.put(Symbols.F, move.y, move.x)
            case "unflag" => field.put(Symbols.Covered, move.y, move.x)
          }
    
    override def undoStep(field: IField): IField = 
        move.value match {
            case "unflag" => field.put(Symbols.F, move.y, move.x)
            case "flag" => field.put(Symbols.Covered, move.y, move.x)
            case "open" => field.put(Symbols.Covered, move.y, move.x)
            case "recursion" => saveFieldUndo
        }

    override def redoStep(field: IField): IField = 
        move.value match {
            case "open" => 
                val extractedSymbol = field.showInvisibleCell(move.y, move.x)
                val returnField = field.put(extractedSymbol, move.y, move.x)
                returnField
            
            case "flag" => field.put(Symbols.F, move.y, move.x)
            case "unflag" => field.put(Symbols.Covered, move.y, move.x)
            case "recursion" => saveFieldRedo
        }
