package com.example.jambonline

data class GameModel (
    var gameId: String = "-1",
    var winner: String = "",
    var gameStatus: GameStatus = GameStatus.CREATED
    // will probably need a list for the score of all players, and a number of players
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