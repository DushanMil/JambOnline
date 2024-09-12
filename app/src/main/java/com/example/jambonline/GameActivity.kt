package com.example.jambonline

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.jambonline.databinding.ActivityGameBinding
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding

    private var gameModel : GameModel? = null

    // state machine for 1st, 2nd, 3rd roll
    private var rollState: RollState = RollState.FIRST_ROLL

    // array for current dice values
    private var diceValues: IntArray = intArrayOf(6, 5, 4, 3, 2, 6)
    private var diceSelection: Array<SelectedDice> = arrayOf(SelectedDice.NOT_SELECTED, SelectedDice.NOT_SELECTED, SelectedDice.NOT_SELECTED, SelectedDice.NOT_SELECTED, SelectedDice.NOT_SELECTED, SelectedDice.NOT_SELECTED)
    private var imageIds: IntArray = intArrayOf(R.drawable.dice1, R.drawable.dice2, R.drawable.dice3, R.drawable.dice4, R.drawable.dice5, R.drawable.dice6)
    private var selectedImageIds: IntArray = intArrayOf(R.drawable.dice1_selected, R.drawable.dice2_selected, R.drawable.dice3_selected, R.drawable.dice4_selected, R.drawable.dice5_selected, R.drawable.dice6_selected)
    private lateinit var diceBindings : Array<ImageView>

    // global variables for announce
    private var announceState: Boolean = false
    private var announceField: TextView? = null
    private var announceColumnSum: TextView? = null
    private var announceGroupSum: TextView? = null

    // number of moves used to check if the game is finished
    private var numOfMoves: Int = 0
    private val maxGameMoves = 48

    // scoreboard table rows
    private var scoreBoard: MutableList<TableRow> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // when the game is started a snapshot listener is set on the games collection
        // when a change is made in the database it is visible in the local data
        // this is only done in online mode
        // offline there is no snapshotListener
        GameData.fetchGameModel()

        // set on click listeners for table fields
        setTableOnClickListeners()

        // set onclick listener for online game creation
        binding.onlineStartButton.setOnClickListener {
            if (gameModel?.gameId != "-1") {
                startOnlineGame()
            }
        }

        binding.buttonRoll.setOnClickListener {
            performPlayerMove()
        }

        // initialise diceBindings array and set images for dices
        diceBindings = arrayOf(binding.dice1, binding.dice2, binding.dice3, binding.dice4, binding.dice5, binding.dice6)


        // set dice 1-6 onclick listeners for selection dice
        binding.dice1.setOnClickListener {
            selectDice(0)
        }
        binding.dice2.setOnClickListener {
            selectDice(1)
        }
        binding.dice3.setOnClickListener {
            selectDice(2)
        }
        binding.dice4.setOnClickListener {
            selectDice(3)
        }
        binding.dice5.setOnClickListener {
            selectDice(4)
        }
        binding.dice6.setOnClickListener {
            selectDice(5)
        }

        setDiceImages()

        GameData.gameModel.observe(this) {
            gameModel = it
            setUI()
        }
    }

    private fun startOnlineGame() {
        gameModel?.apply {
            GameData.saveGameModel(GameModel(gameId = gameId, gameStatus = GameStatus.INPROGRESS,
                currentPlayer = currentPlayer, numOfPlayers = numOfPlayers,
                playerScores = playerScores, playerFinished = playerFinished))
        }
    }

    private fun setUI() {
        // set UI elements about other users
        gameModel?.apply {
            if (gameId != "-1") {
                binding.gameIdTv.text = gameId
                binding.myIdTv.text = GameData.myId.toString()
                // binding.playingIdTv.text = currentPlayer.toString()


                // set table rows
                if (gameStatus == GameStatus.CREATED || gameStatus == GameStatus.JOINED) {
                    // scoreboard is not initialised
                    // create table rows and add them to table
                    for(i in playerScores.indices) {
                        // using this we skip over the rows that have alredy been added in the table
                        if (i >= scoreBoard.size) {
                            val tableRow = LayoutInflater.from(applicationContext).inflate(R.layout.table_row, null) as TableRow
                            tableRow.findViewById<TextView>(R.id.nameTextView).text = i.toString()
                            tableRow.findViewById<TextView>(R.id.numberTextView).text = playerScores[i].toString()

                            scoreBoard.add(tableRow)

                            binding.scoreBoard.addView(tableRow)
                        }

                    }
                }
                else {
                    // update table data
                    for(i in scoreBoard.indices) {
                        scoreBoard[i].findViewById<TextView>(R.id.numberTextView).text = playerScores[i].toString()
                    }
                }

                // current player's row should be colored
                scoreBoard[currentPlayer].findViewById<TextView>(R.id.numberTextView).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.light_pink))
                scoreBoard[currentPlayer].findViewById<TextView>(R.id.nameTextView).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.light_pink))

                // previous players background color should be reset
                scoreBoard[Math.floorMod(currentPlayer - 1, numOfPlayers)].findViewById<TextView>(R.id.numberTextView).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.white))
                scoreBoard[Math.floorMod(currentPlayer - 1, numOfPlayers)].findViewById<TextView>(R.id.nameTextView).setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.white))

                if (gameStatus == GameStatus.INPROGRESS) {
                    binding.winnerComment.text = "In progress"
                }

                if (gameStatus == GameStatus.FINISHED) {
                    binding.winnerComment.text = if (winner == GameData.myId) {
                        "You won"
                    }
                    else {
                        "Player $winner won"
                    }
                }
            }
            else {
                // game is offline, disable these ui elements
                binding.gameIdLayout.visibility = View.GONE
                binding.myIdLayout.visibility = View.GONE
                binding.scoreBoard.visibility = View.GONE
                binding.winnerLayout.visibility = View.GONE
            }

        }
    }

    private fun setTableOnClickListeners() {

        // down on click listeners
        binding.down1.setOnClickListener {
            processTableClick(binding.down1, binding.downSumDigits, binding.allSumDigits, binding.down2)
        }
        binding.down2.setOnClickListener {
            processTableClick(binding.down2, binding.downSumDigits, binding.allSumDigits, binding.down3)
        }
        binding.down3.setOnClickListener {
            processTableClick(binding.down3, binding.downSumDigits, binding.allSumDigits, binding.down4)
        }
        binding.down4.setOnClickListener {
            processTableClick(binding.down4, binding.downSumDigits, binding.allSumDigits, binding.down5)
        }
        binding.down5.setOnClickListener {
            processTableClick(binding.down5, binding.downSumDigits, binding.allSumDigits, binding.down6)
        }
        binding.down6.setOnClickListener {
            processTableClick(binding.down6, binding.downSumDigits, binding.allSumDigits, binding.downMax)
        }
        binding.downMax.setOnClickListener {
            processTableClick(binding.downMax, binding.downSumMinmax, binding.allSumMinmax, binding.downMin)
        }
        binding.downMin.setOnClickListener {
            processTableClick(binding.downMin, binding.downSumMinmax, binding.allSumMinmax, binding.downStraight)
        }
        binding.downStraight.setOnClickListener {
            processTableClick(binding.downStraight, binding.downSumSpecific, binding.allSumSpecific, binding.downFull)
        }
        binding.downFull.setOnClickListener {
            processTableClick(binding.downFull, binding.downSumSpecific, binding.allSumSpecific, binding.downPoker)
        }
        binding.downPoker.setOnClickListener {
            processTableClick(binding.downPoker, binding.downSumSpecific, binding.allSumSpecific, binding.downJamb)
        }
        binding.downJamb.setOnClickListener {
            processTableClick(binding.downJamb, binding.downSumSpecific, binding.allSumSpecific, null)
        }


        // up on click listeners
        binding.up1.setOnClickListener {
            processTableClick(binding.up1, binding.upSumDigits, binding.allSumDigits, null)
        }
        binding.up2.setOnClickListener {
            processTableClick(binding.up2, binding.upSumDigits, binding.allSumDigits, binding.up1)
        }
        binding.up3.setOnClickListener {
            processTableClick(binding.up3, binding.upSumDigits, binding.allSumDigits, binding.up2)
        }
        binding.up4.setOnClickListener {
            processTableClick(binding.up4, binding.upSumDigits, binding.allSumDigits, binding.up3)
        }
        binding.up5.setOnClickListener {
            processTableClick(binding.up5, binding.upSumDigits, binding.allSumDigits, binding.up4)
        }
        binding.up6.setOnClickListener {
            processTableClick(binding.up6, binding.upSumDigits, binding.allSumDigits, binding.up5)
        }
        binding.upMax.setOnClickListener {
            processTableClick(binding.upMax, binding.upSumMinmax, binding.allSumMinmax, binding.up6)
        }
        binding.upMin.setOnClickListener {
            processTableClick(binding.upMin, binding.upSumMinmax, binding.allSumMinmax, binding.upMax)
        }
        binding.upStraight.setOnClickListener {
            processTableClick(binding.upStraight, binding.upSumSpecific, binding.allSumSpecific, binding.upMin)
        }
        binding.upFull.setOnClickListener {
            processTableClick(binding.upFull, binding.upSumSpecific, binding.allSumSpecific, binding.upStraight)
        }
        binding.upPoker.setOnClickListener {
            processTableClick(binding.upPoker, binding.upSumSpecific, binding.allSumSpecific, binding.upFull)
        }
        binding.upJamb.setOnClickListener {
            processTableClick(binding.upJamb, binding.upSumSpecific, binding.allSumSpecific, binding.upPoker)
        }

        // alternative on click listeners
        binding.alternative1.setOnClickListener {
            processTableClick(binding.alternative1, binding.alternativeSumDigits, binding.allSumDigits, null)
        }
        binding.alternative2.setOnClickListener {
            processTableClick(binding.alternative2, binding.alternativeSumDigits, binding.allSumDigits, null)
        }
        binding.alternative3.setOnClickListener {
            processTableClick(binding.alternative3, binding.alternativeSumDigits, binding.allSumDigits, null)
        }
        binding.alternative4.setOnClickListener {
            processTableClick(binding.alternative4, binding.alternativeSumDigits, binding.allSumDigits, null)
        }
        binding.alternative5.setOnClickListener {
            processTableClick(binding.alternative5, binding.alternativeSumDigits, binding.allSumDigits, null)
        }
        binding.alternative6.setOnClickListener {
            processTableClick(binding.alternative6, binding.alternativeSumDigits, binding.allSumDigits, null)
        }
        binding.alternativeMax.setOnClickListener {
            processTableClick(binding.alternativeMax, binding.alternativeSumMinmax, binding.allSumMinmax, null)
        }
        binding.alternativeMin.setOnClickListener {
            processTableClick(binding.alternativeMin, binding.alternativeSumMinmax, binding.allSumMinmax, null)
        }
        binding.alternativeStraight.setOnClickListener {
            processTableClick(binding.alternativeStraight, binding.alternativeSumSpecific, binding.allSumSpecific, null)
        }
        binding.alternativeFull.setOnClickListener {
            processTableClick(binding.alternativeFull, binding.alternativeSumSpecific, binding.allSumSpecific, null)
        }
        binding.alternativePoker.setOnClickListener {
            processTableClick(binding.alternativePoker, binding.alternativeSumSpecific, binding.allSumSpecific, null)
        }
        binding.alternativeJamb.setOnClickListener {
            processTableClick(binding.alternativeJamb, binding.alternativeSumSpecific, binding.allSumSpecific, null)
        }

        // announce on click listeners
        binding.announce1.setOnClickListener {
            announceFieldClick(binding.announce1, binding.announceSumDigits, binding.allSumDigits)
        }
        binding.announce2.setOnClickListener {
            announceFieldClick(binding.announce2, binding.announceSumDigits, binding.allSumDigits)
        }
        binding.announce3.setOnClickListener {
            announceFieldClick(binding.announce3, binding.announceSumDigits, binding.allSumDigits)
        }
        binding.announce4.setOnClickListener {
            announceFieldClick(binding.announce4, binding.announceSumDigits, binding.allSumDigits)
        }
        binding.announce5.setOnClickListener {
            announceFieldClick(binding.announce5, binding.announceSumDigits, binding.allSumDigits)
        }
        binding.announce6.setOnClickListener {
            announceFieldClick(binding.announce6, binding.announceSumDigits, binding.allSumDigits)
        }
        binding.announceMax.setOnClickListener {
            announceFieldClick(binding.announceMax, binding.announceSumMinmax, binding.allSumMinmax)
        }
        binding.announceMin.setOnClickListener {
            announceFieldClick(binding.announceMin, binding.announceSumMinmax, binding.allSumMinmax)
        }
        binding.announceStraight.setOnClickListener {
            announceFieldClick(binding.announceStraight, binding.announceSumSpecific, binding.allSumSpecific)
        }
        binding.announceFull.setOnClickListener {
            announceFieldClick(binding.announceFull, binding.announceSumSpecific, binding.allSumSpecific)
        }
        binding.announcePoker.setOnClickListener {
            announceFieldClick(binding.announcePoker, binding.announceSumSpecific, binding.allSumSpecific)
        }
        binding.announceJamb.setOnClickListener {
            announceFieldClick(binding.announceJamb, binding.announceSumSpecific, binding.allSumSpecific)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun processTableClick(clicked: TextView, columnSum: TextView, groupSum: TextView, nextField: TextView?) {
        // check if its my turn and if the game is started
        if (!checkOnlineGameConditions()) {
            return
        }

        if (announceState) {
            Toast.makeText(applicationContext, "Can't write after announce!", Toast.LENGTH_SHORT).show()
            return
        }

        if (clicked.hint == "i" && rollState != RollState.FIRST_ROLL) {

            // value can be set to the field
            val rollValue = calculateFieldValue(clicked.tag.toString().toInt())

            // set field value
            clicked.text = rollValue.toString()
            clicked.hint = "x"
            clicked.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
            // add value to total score
            if (clicked.tag.toString().toInt() == 8) {
                // min is a special case because it has to be deducted from the total score
                columnSum.text = (columnSum.text.toString().toInt() - rollValue).toString()
                groupSum.text = (groupSum.text.toString().toInt() - rollValue).toString()
                binding.gameScore.text = (binding.gameScore.text.toString().toInt() - rollValue).toString()
            }
            else {
                // increment column sum
                columnSum.text = (columnSum.text.toString().toInt() + rollValue).toString()
                // increment group sum
                groupSum.text = (groupSum.text.toString().toInt() + rollValue).toString()
                // increment total score
                binding.gameScore.text = (binding.gameScore.text.toString().toInt() + rollValue).toString()
            }


            // next field down or up hint should be i
            if (nextField != null) {
                nextField.hint = "i"
                nextField.text = ""
                nextField.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
            }

            // rollState should be first roll
            rollState = RollState.FIRST_ROLL
            // enable roll button
            binding.buttonRoll.isEnabled = true
            // reset dice selection and images
            resetDiceSelection()
            setDiceImages()
            binding.rollCount.text = "First roll"

            // add number of moves
            numOfMoves++
            // Log.i("tag", "Broj poteza: $numOfMoves")
            if (numOfMoves == maxGameMoves) {
                // game is finished
                rollState = RollState.FINISHED
                binding.rollCount.text = "Game over"
                binding.buttonRoll.isEnabled = false
            }

            if (gameModel?.gameId == "-1") return;

            // my move is finished, announce that to other users
            gameModel?.apply {
                currentPlayer = (currentPlayer + 1) % numOfPlayers
                playerScores[GameData.myId] = binding.gameScore.text.toString().toInt()

                if (rollState == RollState.FINISHED) {
                    playerFinished[GameData.myId] = RollState.FINISHED

                    // check if all other players have finished the game
                    var finished = true
                    for(player in playerFinished) {
                        if (player == RollState.NOT_FINISHED) {
                            finished = false
                        }
                    }

                    if (finished) {
                        // winner = playerScores.max()
                        for(i in playerScores.indices) {
                            if (playerScores[i] == playerScores.max()) winner = i
                        }
                        gameStatus = GameStatus.FINISHED
                    }
                }

                GameData.saveGameModel(this)
            }

        }
        else {
            if (rollState == RollState.FIRST_ROLL) {
                Toast.makeText(applicationContext, "You have to roll the dice first!", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(applicationContext, "Field value already set!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun announceFieldClick(clicked: TextView, columnSum: TextView, groupSum: TextView) {
        if (!checkOnlineGameConditions()) {
            return
        }

        if (clicked.hint == "n" && rollState == RollState.SECOND_ROLL) {
            clicked.hint = "i"
            announceState = true
            announceField = clicked
            announceColumnSum = columnSum
            announceGroupSum = groupSum
        }
        else {
            Toast.makeText(applicationContext, "Can't announce!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateFieldValue(tag: Int): Int {
        var rollValue = 0
        when(tag) {
            7, 8 -> {
                // max, min
                for (dice in diceValues) {
                    rollValue += dice
                }
            }
            9 -> {
                // straight
                rollValue = if (diceValues.contains(6) && diceValues.contains(5) && diceValues.contains(4)
                    && diceValues.contains(3) && diceValues.contains(2)) {
                    20
                } else if (diceValues.contains(5) && diceValues.contains(4)
                    && diceValues.contains(3) && diceValues.contains(2) && diceValues.contains(1)) {
                    15
                } else {
                    0
                }
            }
            10 -> {
                // full 3x, 2x
                // counting sort
                val count: IntArray = intArrayOf(0, 0, 0, 0, 0, 0)
                for(dice in diceValues) {
                    count[dice - 1]++
                }
                // find the index of 3x
                // find the index of 2x different from 3x
                var value3 = 0
                for(i in count.indices) {
                    if (count[i] >= 3) {
                        value3 = i + 1
                    }
                }
                if (value3 == 0) {
                    return 0
                }
                var value2 = 0
                for(i in count.indices) {
                    if (count[i] >= 2 && i != value3 - 1) {
                        value2 = i + 1
                    }
                }
                if (value2 == 0) {
                    return 0
                }

                // both values were found
                return 3 * value3 + 2 * value2
            }
            11 -> {
                // poker 4x
                // counting sort
                val count: IntArray = intArrayOf(0, 0, 0, 0, 0, 0)
                for(dice in diceValues) {
                    count[dice - 1]++
                    if (count[dice - 1] >= 4) {
                        rollValue = 4 * dice
                        break
                    }
                }
            }
            12 -> {
                // jamb 5x
                val count: IntArray = intArrayOf(0, 0, 0, 0, 0, 0)
                for(dice in diceValues) {
                    count[dice - 1]++
                    if (count[dice - 1] >= 5) {
                        rollValue = 5 * dice
                        break
                    }
                }
            }
            else -> {
                // digits 1 - 6
                for (dice in diceValues) {
                    if (dice == tag) rollValue += dice
                }
            }
        }


        return rollValue
    }


    @SuppressLint("SetTextI18n")
    private fun performPlayerMove() {
        if (!checkOnlineGameConditions()) {
            return
        }

        when(rollState) {
            RollState.FIRST_ROLL -> {
                diceRoll()
                binding.rollCount.text = "Second roll"
                rollState = RollState.SECOND_ROLL
            }
            RollState.SECOND_ROLL -> {
                diceRoll()
                binding.rollCount.text = "Third roll"
                rollState = RollState.THIRD_ROLL
                // set previously selected dice to prev_move selected
                // these dice cannot be redelected in the 3rd move
                for(i in 0 .. 5) {
                    if (diceSelection[i] == SelectedDice.SELECTED) {
                        diceSelection[i] = SelectedDice.PREV_MOVE
                    }
                }
            }
            RollState.THIRD_ROLL -> {
                diceRoll()

                if (!announceState) {
                    binding.rollCount.text = "Waiting"
                    binding.buttonRoll.isEnabled = false
                    rollState = RollState.WAITING
                }
                else {
                    announceState = false
                    announceField?.let { announceColumnSum?.let { it1 ->
                        announceGroupSum?.let { it2 ->
                            processTableClick(it,
                                it1, it2, null)
                        }
                    } }
                }

            }
            RollState.WAITING, RollState.NOT_FINISHED, RollState.FINISHED -> {
                // nothing to do
            }
        }
    }

    private fun checkOnlineGameConditions(): Boolean {
        if (gameModel?.gameId != "-1") {
            if (gameModel?.gameStatus != GameStatus.INPROGRESS) {
                if (gameModel?.gameStatus == GameStatus.FINISHED) {
                    Toast.makeText(applicationContext, "Game is finished!", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(applicationContext, "Game is not started!", Toast.LENGTH_SHORT).show()
                }
                return false
            }
            if (gameModel?.currentPlayer != GameData.myId) {
                Toast.makeText(applicationContext, "Not your turn!", Toast.LENGTH_SHORT).show()
                return false
            }

        }
        return true
    }

    private fun resetDiceSelection() {
        for (i in 0 .. 5) {
            diceSelection[i] = SelectedDice.NOT_SELECTED
        }
    }

    private fun diceRoll() {
        // generate 6 random integers and set dice values
        for (i in 0 .. 5) {
            if (diceSelection[i] == SelectedDice.NOT_SELECTED) {
                val random : Int = Random.nextInt(6) + 1 // 1, 2, 3, 4, 5, 6
                diceValues[i] = random
            }

        }

        // set images for the current roll
        setDiceImages()
    }

    private fun setDiceImages() {
        for (i in 0 .. 5) {
            if (diceSelection[i] == SelectedDice.NOT_SELECTED) {
                diceBindings[i].setImageResource(imageIds[diceValues[i] - 1])

            }
            else {
                diceBindings[i].setImageResource(selectedImageIds[diceValues[i] - 1])
            }
        }
    }

    private fun selectDice(dice: Int) {
        // select dice to be saved in the next roll
        // dice cannot be stored before first or after 3rd roll
        if (rollState == RollState.SECOND_ROLL) {
            if (diceSelection[dice] == SelectedDice.NOT_SELECTED) {
                diceSelection[dice] = SelectedDice.SELECTED
                diceBindings[dice].setImageResource(selectedImageIds[diceValues[dice] - 1])
            }
            else {
                // restore dice selection
                diceSelection[dice] = SelectedDice.NOT_SELECTED
                diceBindings[dice].setImageResource(imageIds[diceValues[dice] - 1])
            }

        }
        else if (rollState == RollState.THIRD_ROLL) {
            if (diceSelection[dice] == SelectedDice.NOT_SELECTED) {
                diceSelection[dice] = SelectedDice.SELECTED
                diceBindings[dice].setImageResource(selectedImageIds[diceValues[dice] - 1])
            }
            else if (diceSelection[dice] == SelectedDice.SELECTED) {
                // restore dice selection
                diceSelection[dice] = SelectedDice.NOT_SELECTED
                diceBindings[dice].setImageResource(imageIds[diceValues[dice] - 1])
            }
            else {
                // dice was selected in the prev move
                // it cant be deselected now
                Toast.makeText(applicationContext, "Dice was selected in previous move!", Toast.LENGTH_SHORT).show()
            }
        }
        else {
            Toast.makeText(applicationContext, "Can't store a dice value", Toast.LENGTH_SHORT).show()
        }

    }
}