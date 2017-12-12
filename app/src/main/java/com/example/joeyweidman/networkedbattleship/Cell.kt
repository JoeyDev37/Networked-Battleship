package com.example.joeyweidman.networkedbattleship

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.net.Network
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_game_screen.*

/**
 * Created by Joey Weidman
 */
class Cell : View {
    constructor(context: Context?, x: Int, y: Int, isTouchable: Boolean) : super(context) {
        this.x = x
        this.y = y
        this.isTouchable = isTouchable
    }
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    var x: Int = 0
    var y: Int = 0
    var isTouchable = false

    var currentStatus: Status
        set(newStatus) {
            field = newStatus
            invalidate()
        }
    var currentColor: Int
    var shipType: Ship

    init {
        currentStatus = Status.EMPTY
        currentColor = Color.CYAN
        shipType = Ship.NONE
    }

    lateinit var opponentBottomGrid: Array<Array<Triple<Status, Ship, Boolean>>>
    lateinit var playerTopGrid: Array<Array<Triple<Status, Ship, Boolean>>>

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas !is Canvas)
            return

        when(currentStatus) {
            (Status.SHIP) -> {
                currentColor = Color.GRAY
            }
            (Status.EMPTY) -> {
                currentColor = Color.CYAN
            }
            (Status.HIT) -> {
                currentColor = Color.GREEN
            }
            (Status.MISS) -> {
                currentColor = Color.RED
            }
            Status.SUNK -> {
                currentColor = Color.BLACK
            }
        }

        canvas.drawColor(currentColor)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //return super.onTouchEvent(event)
        if(event !is MotionEvent)
            return false

        //Don't allow any more touch actions if a player has won
        if(NetworkedBattleship.gameState == GameState.P1_VICTORY || NetworkedBattleship.gameState == GameState.P2_VICTORY)
            return true

        when(event.action) {
            MotionEvent.ACTION_DOWN -> {

                //Determine which grids we are working with:
                if(NetworkedBattleship.currentPlayer == 1) {
                    playerTopGrid = NetworkedBattleship.topGridP1
                    opponentBottomGrid = NetworkedBattleship.bottomGridP2
                } else if (NetworkedBattleship.currentPlayer == 2) {
                    playerTopGrid = NetworkedBattleship.topGridP2
                    opponentBottomGrid = NetworkedBattleship.bottomGridP1
                }

                Log.e("Cell", "Ship type HIT is: " + opponentBottomGrid[x][y].second.toString())

                //Variables to help with readability
                val opponentStatus = opponentBottomGrid[x][y].first
                val opponentShipType = opponentBottomGrid[x][y].second

                if(isTouchable) {
                    if (opponentStatus == Status.EMPTY) { //If the shot was a miss
                        currentStatus = Status.MISS //Might as well update the cell now to display the correct color before we switch activities
                        val updatedCell = Triple(Status.MISS, shipType, false)
                        playerTopGrid[x][y] = updatedCell //Set the player's top grid to show the miss
                        opponentBottomGrid[x][y] = updatedCell //And set the opponents bottom grid to show where the player attacked

                        NetworkedBattleship.changeTurn()

                        val intent = Intent(context, GameScreenActivity::class.java)
                        context.startActivity(intent)
                    }
                    else if (opponentStatus == Status.SHIP) { //Same stuff for a hit
                        currentStatus = Status.HIT
                        var updatedCell = Triple(Status.HIT, shipType, false)
                        playerTopGrid[x][y] = updatedCell
                        updatedCell = Triple(Status.HIT, opponentShipType, false)
                        opponentBottomGrid[x][y] = updatedCell

                        decrementShipHealth()

                        if(checkForVictory()) {
                            if(NetworkedBattleship.currentPlayer == 1) {
                                //P1 Victory
                                Log.e("Cell", "P1 VICTORY REACHED")
                                NetworkedBattleship.gameState = GameState.P1_VICTORY
                            } else if (NetworkedBattleship.currentPlayer == 2) {
                                //P2 Victory
                                Log.e("Cell", "P2 VICTORY REACHED")
                                NetworkedBattleship.gameState = GameState.P2_VICTORY
                            }
                        }

                        if(NetworkedBattleship.currentPlayer == 2) {
                            Log.e("Cell", "P1 Destroyer Health: ${NetworkedBattleship.player1.destroyerHealth}")
                            Log.e("Cell", "P1 Cruiser Health: ${NetworkedBattleship.player1.cruiserHealth}")
                            Log.e("Cell", "P1 Submarine Health: ${NetworkedBattleship.player1.submarineHealth}")
                            Log.e("Cell", "P1 Battleship Health: ${NetworkedBattleship.player1.battleshipHealth}")
                            Log.e("Cell", "P1 Carrier Health: ${NetworkedBattleship.player1.carrierHealth}")
                        } else if(NetworkedBattleship.currentPlayer == 1) {
                            Log.e("Cell", "P2 Destroyer Health: ${NetworkedBattleship.player2.destroyerHealth}")
                            Log.e("Cell", "P2 Cruiser Health: ${NetworkedBattleship.player2.cruiserHealth}")
                            Log.e("Cell", "P2 Submarine Health: ${NetworkedBattleship.player2.submarineHealth}")
                            Log.e("Cell", "P2 Battleship Health: ${NetworkedBattleship.player2.battleshipHealth}")
                            Log.e("Cell", "P2 Carrier Health: ${NetworkedBattleship.player2.carrierHealth}")
                        }

                        NetworkedBattleship.changeTurn()

                        val intent = Intent(context, GameScreenActivity::class.java)
                        context.startActivity(intent)
                    }
                }
            }
        }
        return true
    }

    //Decrements the ships health and sinks it if health drops to 0
    fun decrementShipHealth() {
        val shipTypeToDecrement = opponentBottomGrid[x][y].second
        if(NetworkedBattleship.currentPlayer == 2) {
            when(shipTypeToDecrement) {
                Ship.DESTROYER -> {
                    NetworkedBattleship.player1.destroyerHealth--
                    if(NetworkedBattleship.player1.destroyerHealth == 0) {
                        sinkShip(shipTypeToDecrement)
                        NetworkedBattleship.player1.destroyerHealth = -1
                        NetworkedBattleship.player1.shipsRemaining--
                    }
                }
                Ship.CRUISER -> {
                    NetworkedBattleship.player1.cruiserHealth--
                    if(NetworkedBattleship.player1.cruiserHealth == 0) {
                        sinkShip(shipTypeToDecrement)
                        NetworkedBattleship.player1.cruiserHealth = -1
                        NetworkedBattleship.player1.shipsRemaining--
                    }
                }
                Ship.SUBMARINE -> {
                    NetworkedBattleship.player1.submarineHealth--
                    if(NetworkedBattleship.player1.submarineHealth == 0) {
                        sinkShip(shipTypeToDecrement)
                        NetworkedBattleship.player1.submarineHealth = -1
                        NetworkedBattleship.player1.shipsRemaining--
                    }
                }
                Ship.BATTLESHIP -> {
                    NetworkedBattleship.player1.battleshipHealth--
                    if(NetworkedBattleship.player1.battleshipHealth == 0) {
                        sinkShip(shipTypeToDecrement)
                        NetworkedBattleship.player1.battleshipHealth = -1
                        NetworkedBattleship.player1.shipsRemaining--
                    }
                }
                Ship.CARRIER -> {
                    NetworkedBattleship.player1.carrierHealth--
                    if(NetworkedBattleship.player1.carrierHealth == 0) {
                        sinkShip(shipTypeToDecrement)
                        NetworkedBattleship.player1.carrierHealth = -1
                        NetworkedBattleship.player1.shipsRemaining--
                    }
                }
            }
        } else if (NetworkedBattleship.currentPlayer == 1) {
            when(shipTypeToDecrement) {
                Ship.DESTROYER -> {
                    NetworkedBattleship.player2.destroyerHealth--
                    if(NetworkedBattleship.player2.destroyerHealth == 0) {
                        sinkShip(shipTypeToDecrement)
                        NetworkedBattleship.player2.destroyerHealth = -1
                        NetworkedBattleship.player2.shipsRemaining--
                    }
                }
                Ship.CRUISER -> {
                    NetworkedBattleship.player2.cruiserHealth--
                    if(NetworkedBattleship.player2.cruiserHealth == 0) {
                        sinkShip(shipTypeToDecrement)
                        NetworkedBattleship.player2.cruiserHealth = -1
                        NetworkedBattleship.player2.shipsRemaining--
                    }
                }
                Ship.SUBMARINE -> {
                    NetworkedBattleship.player2.submarineHealth--
                    if(NetworkedBattleship.player2.submarineHealth == 0) {
                        sinkShip(shipTypeToDecrement)
                        NetworkedBattleship.player2.submarineHealth = -1
                        NetworkedBattleship.player2.shipsRemaining--
                    }
                }
                Ship.BATTLESHIP -> {
                    NetworkedBattleship.player2.battleshipHealth--
                    if(NetworkedBattleship.player2.battleshipHealth == 0) {
                        sinkShip(shipTypeToDecrement)
                        NetworkedBattleship.player2.battleshipHealth = -1
                        NetworkedBattleship.player2.shipsRemaining--
                    }
                }
                Ship.CARRIER -> {
                    NetworkedBattleship.player2.carrierHealth--
                    if(NetworkedBattleship.player2.carrierHealth == 0) {
                        sinkShip(shipTypeToDecrement)
                        NetworkedBattleship.player2.carrierHealth = -1
                        NetworkedBattleship.player2.shipsRemaining--
                    }
                }
            }
        }
    }

    /* Loops through the whole grid to find the cells with the shiptype that matches the one that got sunk
     * and sets its status to SUNK
     */
    fun sinkShip(shipTypeToSink: Ship) {
        for(yPos in 0..9) {
            for(xPos in 0..9) {
                if(opponentBottomGrid[xPos][yPos].second == shipTypeToSink){
                    val updatedCell = Triple(Status.SUNK, opponentBottomGrid[xPos][yPos].second, false)
                    playerTopGrid[xPos][yPos] = updatedCell
                    opponentBottomGrid[xPos][yPos] = updatedCell
                }
            }
        }
    }

    /* Goes through the health of every ship to see if they are all sunk */
    fun checkForVictory() : Boolean {
        if(NetworkedBattleship.currentPlayer == 1) {
            return (NetworkedBattleship.player2.destroyerHealth == -1 && NetworkedBattleship.player2.cruiserHealth == -1 && NetworkedBattleship.player2.submarineHealth == -1 &&
                    NetworkedBattleship.player2.battleshipHealth == -1 && NetworkedBattleship.player2.carrierHealth == -1)
        } else if (NetworkedBattleship.currentPlayer == 2) {
            return (NetworkedBattleship.player1.destroyerHealth == -1 && NetworkedBattleship.player1.cruiserHealth == -1 && NetworkedBattleship.player1.submarineHealth == -1 &&
                    NetworkedBattleship.player1.battleshipHealth == -1 && NetworkedBattleship.player1.carrierHealth == -1)
        }
        return false
    }
}