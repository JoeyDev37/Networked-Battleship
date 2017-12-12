package com.example.joeyweidman.networkedbattleship

import android.content.Context
import android.graphics.Point
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.io.FileInputStream


/**
 * Created by Joey Weidman
 */
object NetworkedBattleship {
    //TODO: Add counter for remaining ships

    var gameState: GameState = GameState.STARTING //json
    var currentPlayer: Int = 1 //json

    var player1: Player = Player() //json
    var player2: Player = Player() //json

    lateinit var topGridP1: Array<Array<Triple<Status, Ship, Boolean>>> //json
    lateinit var bottomGridP1: Array<Array<Triple<Status, Ship, Boolean>>> //json
    lateinit var topGridP2: Array<Array<Triple<Status, Ship, Boolean>>> //json
    lateinit var bottomGridP2: Array<Array<Triple<Status, Ship, Boolean>>> //json

    lateinit var currentGridToPlaceShips: Array<Array<Triple<Status, Ship, Boolean>>>

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
            val newTriple = Triple(Status.SHIP, shipToPlace, false)
            currentGridToPlaceShips[i.x][i.y] = newTriple
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
            val topGridP1: Array<Array<Triple<Status, Ship, Boolean>>>,
            val topGridP2: Array<Array<Triple<Status, Ship, Boolean>>>,
            val bottomGridP1: Array<Array<Triple<Status, Ship, Boolean>>>,
            val bottomGridP2: Array<Array<Triple<Status, Ship, Boolean>>>
    )

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

    fun LoadGame(file: File) {
        val gson = Gson()
        val inputAsString = FileInputStream(file).bufferedReader().use { it.readText() }
        val savedGame = gson.fromJson(inputAsString, SaveGameState::class.java)

        gameState = savedGame.gameState
        currentPlayer = savedGame.currentPlayer
        player1 = savedGame.player1
        player2 = savedGame.player2
        topGridP1 = savedGame.topGridP1
        topGridP2 = savedGame.topGridP2
        bottomGridP1 = savedGame.bottomGridP1
        bottomGridP2 = savedGame.bottomGridP2
    }
}