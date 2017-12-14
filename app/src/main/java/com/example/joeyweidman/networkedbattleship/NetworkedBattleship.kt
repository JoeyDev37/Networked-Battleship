package com.example.joeyweidman.networkedbattleship

import android.content.Context
import android.graphics.Point
import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.io.FileInputStream


/**
 * Created by Joey Weidman
 */
object NetworkedBattleship {

    //Get a reference to the root of the json tree
    private var mRootRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var mGamesRef: DatabaseReference = mRootRef.child("games")

    var gameState: GameState = GameState.STARTING //json
    var currentPlayer: Int = 1 //json

    var player1: Player = Player() //json
    var player2: Player = Player() //json

    var topGridP1: Array<Array<Pair<Status, Ship>>> //json
    var bottomGridP1: Array<Array<Pair<Status, Ship>>> //json
    var topGridP2: Array<Array<Pair<Status, Ship>>> //json
    var bottomGridP2: Array<Array<Pair<Status, Ship>>> //json

    init {
        //Initialize all grids. Set empty values (because nothing is in them yet)
        topGridP1 = Array(10, {Array(10, {Pair(Status.EMPTY, Ship.NONE)})})
        bottomGridP1 = Array(10, {Array(10, {Pair(Status.EMPTY, Ship.NONE)})})
        topGridP2 = Array(10, {Array(10, {Pair(Status.EMPTY, Ship.NONE)})})
        bottomGridP2 = Array(10, {Array(10, {Pair(Status.EMPTY, Ship.NONE)})})
    }

    lateinit var currentGridToPlaceShips: Array<Array<Pair<Status, Ship>>>

    fun PlaceShipsRandomly() {
        currentGridToPlaceShips = bottomGridP1
        for(ship in Ship.values()) {
            placeShip(ship)
        }

        currentGridToPlaceShips = bottomGridP2
        for(ship in Ship.values()) {
            placeShip(ship)
        }
    }

    private fun placeShip(shipToPlace: Ship) {
        if(shipToPlace == Ship.NONE)
            return

        var potentialPlacement: Array<Point>
        start@while(true) {
            var random: Random = Random()
            val randomX: Int = random.nextInt(10)
            random = Random()
            val randomY: Int = random.nextInt(10)
            val currentPoint: Point = Point(randomX, randomY)
            val startingPoint: Point = Point(currentPoint)
            potentialPlacement = Array(shipToPlace.size, {Point()})
            potentialPlacement[0] = startingPoint
            val randomDirection: Direction = Direction.randomDirection()

            when(randomDirection) {
                Direction.NORTH -> {
                    for(i in 1..shipToPlace.size - 1) {
                        if(currentPoint.y - 1 < 0 || currentGridToPlaceShips[currentPoint.x][currentPoint.y - 1].first != Status.EMPTY) {
                            continue@start
                        } else {
                            currentPoint.y--
                            val point: Point = Point(currentPoint)
                            potentialPlacement[i] = point
                            if(i == shipToPlace.size - 1)
                                break@start
                        }
                    }
                }
                Direction.SOUTH -> {
                    for(i in 1..shipToPlace.size - 1) {
                        if(currentPoint.y + 1 > 9 || currentGridToPlaceShips[currentPoint.x][currentPoint.y + 1].first != Status.EMPTY) {
                            continue@start
                        } else {
                            currentPoint.y++
                            val point: Point = Point(currentPoint)
                            potentialPlacement[i] = point
                            if(i == shipToPlace.size - 1)
                                break@start
                        }
                    }
                }
                Direction.EAST -> {
                    for(i in 1..shipToPlace.size - 1) {
                        if(currentPoint.x + 1 > 9 || currentGridToPlaceShips[currentPoint.x + 1][currentPoint.y].first != Status.EMPTY) {
                            continue@start
                        } else {
                            currentPoint.x++
                            val point: Point = Point(currentPoint)
                            potentialPlacement[i] = point
                            if(i == shipToPlace.size - 1)
                                break@start
                        }
                    }
                }
                Direction.WEST -> {
                    for(i in 1..shipToPlace.size - 1) {
                        if(currentPoint.x - 1 < 0 || currentGridToPlaceShips[currentPoint.x - 1][currentPoint.y].first != Status.EMPTY) {
                            continue@start
                        } else {
                            currentPoint.x--
                            val point: Point = Point(currentPoint)
                            potentialPlacement[i] = point
                            if(i == shipToPlace.size - 1)
                                break@start
                        }
                    }
                }
            }
        }
        for(i in potentialPlacement) {
            val newPair = Pair(Status.SHIP, shipToPlace)
            currentGridToPlaceShips[i.x][i.y] = newPair
        }
    }

    enum class Direction {
        NORTH, SOUTH, EAST, WEST;

        companion object {
            fun randomDirection(): Direction {
                val random: Random = Random()
                return values()[random.nextInt(values().size)]
            }
        }
    }

    fun changeTurn() {
        if(currentPlayer == 1)
            currentPlayer = 2
        else if (currentPlayer == 2)
            currentPlayer = 1
    }

    data class SaveGameState(
            val gameState: GameState,
            val currentPlayer: Int,
            val player1: Player,
            val player2: Player,
            val topGridP1: Array<Array<Pair<Status, Ship>>>,
            val topGridP2: Array<Array<Pair<Status, Ship>>>,
            val bottomGridP1: Array<Array<Pair<Status, Ship>>>,
            val bottomGridP2: Array<Array<Pair<Status, Ship>>>
    )

    data class FirebaseEntry(val json: String, val gameID: String, val player1: String, val player2: String, val gameState: GameState)

    //Temporary save game function before I save things to firebase
    fun SaveGame(mContext: Context) {
        val savedGame = SaveGameState(
                this.gameState, currentPlayer, player1, player2, topGridP1, topGridP2, bottomGridP1, bottomGridP2
        )
        val gson = Gson()
        val jsonString: String = gson.toJson(savedGame)

        val fileStream: FileOutputStream? = mContext.openFileOutput("test", Context.MODE_PRIVATE)
        fileStream?.write(jsonString.toByteArray())
        fileStream?.close()
    }

    fun LoadGame(jsonString: String) {
        //Log.e("NetworkedBattleship", "LoadGame Called")
        val gson = Gson()
        //val inputAsString = FileInputStream(file).bufferedReader().use { it.readText() }
        val savedGame = gson.fromJson(jsonString, SaveGameState::class.java)

        gameState = savedGame.gameState
        currentPlayer = savedGame.currentPlayer
        player1 = savedGame.player1
        player2 = savedGame.player2
        topGridP1 = savedGame.topGridP1
        topGridP2 = savedGame.topGridP2
        bottomGridP1 = savedGame.bottomGridP1
        bottomGridP2 = savedGame.bottomGridP2
    }

    fun WriteNewGame(userID: String, name: String?, gameState: GameState): String {
        val savedGame = SaveGameState(
                this.gameState, currentPlayer, player1, player2, topGridP1, topGridP2, bottomGridP1, bottomGridP2
        )
        val gson = Gson()
        val jsonString: String = gson.toJson(savedGame)

        val key = mRootRef.child("games").push().key
        val firebaseEntry = FirebaseEntry(jsonString, key, "empty", "empty", gameState)
        mRootRef.child("games").child(key).setValue(firebaseEntry)
        mRootRef.child("games").child(key).child("player1").child("ID").setValue(userID)
        mRootRef.child("games").child(key).child("player1").child("name").setValue(name)
        return key
    }

    fun UpdateGame(gameKey: String) {
        val savedGame = SaveGameState(
                this.gameState, currentPlayer, player1, player2, topGridP1, topGridP2, bottomGridP1, bottomGridP2
        )
        val gson = Gson()
        val jsonString: String = gson.toJson(savedGame)

        mRootRef.child("games").child(gameKey).child("json").setValue(jsonString)
    }

    fun CleanGame() {
        gameState = GameState.STARTING
        currentPlayer = 1
        player1 = Player()
        player2 = Player()
        topGridP1 = Array(10, {Array(10, {Pair(Status.EMPTY, Ship.NONE)})})
        topGridP2 = Array(10, {Array(10, {Pair(Status.EMPTY, Ship.NONE)})})
        bottomGridP1 = Array(10, {Array(10, {Pair(Status.EMPTY, Ship.NONE)})})
        bottomGridP2 = Array(10, {Array(10, {Pair(Status.EMPTY, Ship.NONE)})})
    }
}