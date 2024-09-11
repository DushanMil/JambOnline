package com.example.jambonline

data class GameModel (
    var gameId: String = "-1",
    var winner: Int = -1,
    var gameStatus: GameStatus = GameStatus.CREATED,
    var currentPlayer: Int = -1,
    var numOfPlayers: Int = 0,
    var playerScores: MutableList<Int> = mutableListOf()
)

enum class GameStatus {
    CREATED,
    JOINED,
    INPROGRESS,
    FINISHED
}

enum class RollState {
    FIRST_ROLL,
    SECOND_ROLL,
    THIRD_ROLL,
    WAITING,
    FINISHED
}

enum class SelectedDice {
    SELECTED,
    NOT_SELECTED
}