package com.example.jambonline

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set on click listeners for table fields
        setTableOnClickListeners()

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
    }

    @SuppressLint("SetTextI18n")
    private fun processTableClick(clicked: TextView, columnSum: TextView, groupSum: TextView, nextField: TextView?) {
        if (clicked.hint == "i" && rollState != RollState.FIRST_ROLL) {

            // value can be set to the field
            val rollValue = calculateFieldValue(clicked.tag.toString().toInt())

            // set field value
            clicked.text = rollValue.toString()
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
                var value3: Int = 0
                for(i in count.indices) {
                    if (count[i] >= 3) {
                        value3 = i + 1
                    }
                }
                if (value3 == 0) {
                    return 0
                }
                var value2: Int = 0
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
                // TODO remove this
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