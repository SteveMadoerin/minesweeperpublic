package de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl

class PackGame[T](val games: List[T]) {
  def map(f:T => T) = games.map(game => f(game))
}
