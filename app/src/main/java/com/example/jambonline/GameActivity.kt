package com.example.jambonline

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.down1.setOnClickListener {
            // example, should be different
            binding.down1.text = "4"
            binding.buttonRoll.isEnabled = true
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

    }


    @SuppressLint("SetTextI18n")
    private fun performPlayerMove() {

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
            }
            RollState.THIRD_ROLL -> {
                diceRoll()

                binding.rollCount.text = "Waiting"
                binding.buttonRoll.isEnabled = false


                rollState = RollState.WAITING
            }
            RollState.WAITING -> {
                binding.rollCount.text = "First roll"

                resetDiceSelection()
                setDiceImages()

                rollState = RollState.FIRST_ROLL
            }
        }
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
        if (rollState == RollState.SECOND_ROLL || rollState == RollState.THIRD_ROLL) {
            diceSelection[dice] = SelectedDice.SELECTED
            diceBindings[dice].setImageResource(selectedImageIds[diceValues[dice] - 1])
        }
        else {
            Toast.makeText(applicationContext, "Can't store a dice value", Toast.LENGTH_SHORT).show()
        }

    }
}