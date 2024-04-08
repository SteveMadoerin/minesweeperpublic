package de.htwg.se.minesweeper.util

case class Move(value: String, x: Int, y: Int):

    def validate: Move = value match
        case "open" => copy("open", x, y)
        case "flag" => copy("flag", x, y)
        case "unflag" => copy("unflag", x, y)
        case _ => copy("invalid", x, y)