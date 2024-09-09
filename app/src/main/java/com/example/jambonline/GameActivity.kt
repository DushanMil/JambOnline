package com.example.jambonline

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.jambonline.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {

    lateinit var binding: ActivityGameBinding

    private var gameModel : GameModel? = null

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
            rollDice()
        }

        binding.dice1.setImageResource(R.drawable.dice1)
        binding.dice2.setImageResource(R.drawable.dice2)
        binding.dice3.setImageResource(R.drawable.dice3)
        binding.dice4.setImageResource(R.drawable.dice4)
        binding.dice5.setImageResource(R.drawable.dice5)
        binding.dice6.setImageResource(R.drawable.dice6)
    }

    private fun rollDice() {
        // generate six random numbers

    }
}