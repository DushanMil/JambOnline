package com.example.jambonline

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.jambonline.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.playOfflineBtn.setOnClickListener { createOfflineGame() }
        binding.createOnlineGameBtn.setOnClickListener { createOnlineGame() }
        binding.joinOnlineGameBtn.setOnClickListener { joinOnlineGame() }
    }

    private fun joinOnlineGame() {
        val gameId = binding.gameIdInput.text.toString()
        if (gameId.isEmpty()) {
            binding.gameIdInput.error = "Please enter a game id!"
            return
        }

        // myId to be found in data from game
        GameData.myScore = 0

        Firebase.firestore.collection("games").document(gameId).get().addOnSuccessListener {
            val model = it?.toObject(GameModel::class.java)
            if (model == null) {
                binding.gameIdInput.error = "Please enter a valid game id!"
            }
            else if (model.gameStatus == GameStatus.FINISHED || model.gameStatus == GameStatus.INPROGRESS) {
                binding.gameIdInput.error = "Game already in progress!"
            }
            else {
                model.gameStatus = GameStatus.JOINED
                GameData.myId = model.numOfPlayers
                model.numOfPlayers++
                model.playerScores.add(0)
                GameData.saveGameModel(model)
                startGame()
            }
        }
    }

    private fun createOnlineGame() {
        GameData.myId = 0
        GameData.myScore = 0
        GameData.saveGameModel(
            GameModel(
                gameStatus = GameStatus.CREATED,
                gameId = (Random.nextInt(8999) + 1000).toString(),
                currentPlayer = 0,
                numOfPlayers = 1,
                playerScores = mutableListOf(0)
            )
        )
        startGame()
    }

    private fun createOfflineGame() {
        GameData.saveGameModel(
            GameModel(
                gameStatus = GameStatus.JOINED,
                currentPlayer = 0,
                numOfPlayers = 1,
                playerScores = mutableListOf(0)
            )
        )
        startGame()
    }

    private fun startGame() {
        startActivity(Intent(this, GameActivity::class.java))
    }
}