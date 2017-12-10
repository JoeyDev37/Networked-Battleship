package com.example.joeyweidman.networkedbattleship

import android.graphics.Point
import java.util.*

/**
 * Created by Joey Weidman
 */
object NetworkedBattleship {
    var currentPlayer: Int = 1 //json

    var player1: Player = Player()
    var player2: Player = Player()

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
}