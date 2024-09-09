package com.example.jambonline

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
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
    private var imageIds: IntArray = intArrayOf(R.drawable.dice1, R.drawable.dice2, R.drawable.dice3, R.drawable.dice4, R.drawable.dice5, R.drawable.dice6)
    private lateinit var diceBindings : Array<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.down1.setOnClickListener {
            // example, should be different
            binding.down1.text = "4"
        }

        binding.buttonRoll.setOnClickListener {
            performPlayerMove()
        }

        // initialise diceBindings array and set images for dices
        diceBindings = arrayOf(binding.dice1, binding.dice2, binding.dice3, binding.dice4, binding.dice5, binding.dice6)

        setDiceImages()

        /*
        binding.dice1.setImageResource(imageIds[diceValues[0] - 1])
        binding.dice2.setImageResource(imageIds[diceValues[1] - 1])
        binding.dice3.setImageResource(imageIds[diceValues[2] - 1])
        binding.dice4.setImageResource(imageIds[diceValues[3] - 1])
        binding.dice5.setImageResource(imageIds[diceValues[4] - 1])
        binding.dice6.setImageResource(imageIds[diceValues[5] - 1])
        */

    }


    @SuppressLint("SetTextI18n")
    private fun performPlayerMove() {
        // generate six random numbers

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
                binding.rollCount.text = "First roll"
                rollState = RollState.FIRST_ROLL
            }
            RollState.WAITING -> {

            }
        }
    }

    private fun diceRoll() {
        // generate 6 random integers and set dice values
        for (i in 0 .. 5) {
            val random : Int = Random.nextInt(6) + 1 // 1, 2, 3, 4, 5, 6
            diceValues[i] = random
        }

        // set images for the current roll
        setDiceImages()
    }

    private fun setDiceImages() {
        for (i in 0 .. 5) {
            diceBindings[i].setImageResource(imageIds[diceValues[i] - 1])
        }
    }
}