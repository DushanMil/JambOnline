package com.example.jambonline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

object GameData {
    private var _gameModel: MutableLiveData<GameModel> = MutableLiveData()

    var gameModel: LiveData<GameModel> = _gameModel

    var myId: Int = 0
    var myScore: Int = 0

    fun saveGameModel(model: GameModel) {
        _gameModel.postValue(model)

        if (model.gameId != "-1") {
            Firebase.firestore.collection("games").document(model.gameId).set(model)
        }
    }

    fun fetchGameModel() {
        gameModel.value?.apply {
            if (gameId != "-1") {
                // fetch document from firestore db
                // postavi se snapshotListener i svaka promena je vidljiva u nasim podacima
                Firebase.firestore.collection("games")
                    .document(gameId).addSnapshotListener { value, error ->
                        val model = value?.toObject(GameModel::class.java)
                        _gameModel.postValue(model)
                    }
            }
        }
    }
}