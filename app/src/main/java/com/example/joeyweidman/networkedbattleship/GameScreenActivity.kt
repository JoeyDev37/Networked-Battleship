package com.example.joeyweidman.networkedbattleship

import android.annotation.SuppressLint
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Debug
import android.provider.ContactsContract
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_game_screen.*

class GameScreenActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var rootRef: DatabaseReference
    private lateinit var gameKeyRef: DatabaseReference
    private lateinit var jsonRef: DatabaseReference

    private var player: Int = -1

    val GRID_SIZE = 10
    lateinit var topGrid: Array<Array<Cell>>
    lateinit var bottomGrid: Array<Array<Cell>>
    lateinit var opponentBottomGrid: Array<Array<Triple<Status, Ship, Boolean>>>
    lateinit var playerTopGrid: Array<Array<Triple<Status, Ship, Boolean>>>

    /* OnCreate will read from the global data to create new game grid
     * every time the GameScreenActivity is started. So every time the player
     * views the game screen, it is updated with the most recent data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_screen)

        mAuth = FirebaseAuth.getInstance()

        val gameKey = intent.getStringExtra("KEY")

        rootRef = FirebaseDatabase.getInstance().reference
        gameKeyRef = rootRef.child("games").child(gameKey)
        jsonRef = gameKeyRef.child("json")

        //Determine which player you are
        if(gameKeyRef.child("idplayer1").key == mAuth.currentUser!!.uid)
            player = 1
        else if(gameKeyRef.child("idplayer2").key == mAuth.currentUser!!.uid)
            player = 2
        else
            player = 0

        val jsonListener = object: ValueEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError?) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                val jsonString = dataSnapshot?.value as String
                NetworkedBattleship.LoadGame(jsonString)
            }
        }

        jsonRef.addValueEventListener(jsonListener)

        //Update the game state text
        gameScreen_gameStatusText.text = NetworkedBattleship.gameState.toString()

        //Update the unsunk ships text
        gameScreen_unsunkShipsP1.text = "Ships Remaining: ${NetworkedBattleship.player1.shipsRemaining.toString()}"
        gameScreen_unsunkShipsP2.text = "Ships Remaining: ${NetworkedBattleship.player2.shipsRemaining.toString()}"

        //Update the arrow to show the current turn
        if(NetworkedBattleship.currentPlayer == 1) {
            gameScreen_leftArrow.visibility = View.VISIBLE
            gameScreen_rightArrow.visibility = View.INVISIBLE
        } else if(NetworkedBattleship.currentPlayer == 2) {
            gameScreen_rightArrow.visibility = View.VISIBLE
            gameScreen_leftArrow.visibility = View.INVISIBLE
        }

        //Initialize the top and bottom grids to empty cells
        topGrid = Array(10, {Array(10, {Cell(this, 0, 0, true)})})
        bottomGrid = Array(10, {Array(10, {Cell(this, 0, 0, false)})})

        @SuppressLint("ClickableViewAccessibility")
        for(y in 0..9) {
            for(x in 0..9) {
                val currentCell: Cell = topGrid[x][y]
                currentCell.setOnTouchListener(object: View.OnTouchListener {
                    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
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

                                if(currentCell.isTouchable) {
                                    if (opponentStatus == Status.EMPTY) { //If the shot was a miss
                                        currentCell.currentStatus = Status.MISS //Might as well update the cell now to display the correct color before we switch activities
                                        val updatedCell = Triple(Status.MISS, currentCell.shipType, false)
                                        playerTopGrid[x][y] = updatedCell //Set the player's top grid to show the miss
                                        opponentBottomGrid[x][y] = updatedCell //And set the opponents bottom grid to show where the player attacked

                                        NetworkedBattleship.changeTurn()



                                        //val intent = Intent(context, GameScreenActivity::class.java)
                                        //context.startActivity(intent)
                                    }
                                    else if (opponentStatus == Status.SHIP) { //Same stuff for a hit
                                        currentCell.currentStatus = Status.HIT
                                        var updatedCell = Triple(Status.HIT, currentCell.shipType, false)
                                        playerTopGrid[x][y] = updatedCell
                                        updatedCell = Triple(Status.HIT, opponentShipType, false)
                                        opponentBottomGrid[x][y] = updatedCell

                                        decrementShipHealth(currentCell.shipType)

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

                                        //val intent = Intent(context, GameScreenActivity::class.java)
                                        //context.startActivity(intent)
                                    }
                                }
                            }
                        }
                        return true
                    }

                })
            }
        }


        /* Read from the global grid data to update and re-draw the cell grid */
        if(NetworkedBattleship.currentPlayer == 1) {
            for(y in 0..GRID_SIZE - 1) {
                for(x in 0..GRID_SIZE - 1) {
                    //var cell = Cell(this, x, y, true)
                    //topGrid[x][y] = cell
                    topGrid[x][y].currentStatus = NetworkedBattleship.topGridP1[x][y].first
                    topGrid[x][y].shipType = NetworkedBattleship.topGridP1[x][y].second
                    topGrid[x][y].isTouchable = NetworkedBattleship.topGridP1[x][y].third

                    gameScreen_topGrid.addView(topGrid[x][y])
                    //addView(x, y)

                    val cell = Cell(this, x, y, false)
                    bottomGrid[x][y] = cell
                    bottomGrid[x][y].currentStatus = NetworkedBattleship.bottomGridP1[x][y].first
                    bottomGrid[x][y].shipType = NetworkedBattleship.bottomGridP1[x][y].second
                    bottomGrid[x][y].isTouchable = NetworkedBattleship.bottomGridP1[x][y].third

                    gameScreen_bottomGrid.addView(bottomGrid[x][y])
                    //addView(x, y)
                }
            }
        } else if (NetworkedBattleship.currentPlayer == 2) {
            for(y in 0..GRID_SIZE - 1) {
                for(x in 0..GRID_SIZE - 1) {
                    //var cell = Cell(this, x, y, true)
                    //topGrid[x][y] = cell
                    topGrid[x][y].currentStatus = NetworkedBattleship.topGridP2[x][y].first
                    topGrid[x][y].shipType = NetworkedBattleship.topGridP2[x][y].second
                    topGrid[x][y].isTouchable = NetworkedBattleship.topGridP2[x][y].third

                    gameScreen_topGrid.addView(topGrid[x][y])
                    //addView(x, y)

                    val cell = Cell(this, x, y, false)
                    bottomGrid[x][y] = cell
                    bottomGrid[x][y].currentStatus = NetworkedBattleship.bottomGridP2[x][y].first
                    bottomGrid[x][y].shipType = NetworkedBattleship.bottomGridP2[x][y].second
                    bottomGrid[x][y].isTouchable = NetworkedBattleship.bottomGridP2[x][y].third

                    gameScreen_bottomGrid.addView(bottomGrid[x][y])
                    //addView(x, y)
                }
            }
        }



        gameScreen_topGrid.viewTreeObserver.addOnGlobalLayoutListener(
                {
                    val MARGIN = 5

                    var layoutWidth = gameScreen_topGrid.width
                    var layoutHeight = gameScreen_topGrid.height
                    val cellWidth = layoutWidth / GRID_SIZE
                    val cellHeight = layoutHeight / GRID_SIZE

                    for (yPos in 0..GRID_SIZE - 1) {
                        for (xPos in 0..GRID_SIZE - 1) {
                            val params = topGrid[xPos][yPos].layoutParams as GridLayout.LayoutParams
                            params.width = cellWidth - 2 * MARGIN
                            params.height = cellHeight - 2 * MARGIN
                            params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN)
                            topGrid[xPos][yPos].layoutParams = params
                        }
                    }
                })

        gameScreen_bottomGrid.viewTreeObserver.addOnGlobalLayoutListener(
                {
                    val MARGIN = 5

                    var layoutWidth = gameScreen_bottomGrid.width
                    var layoutHeight = gameScreen_bottomGrid.height
                    val cellWidth = layoutWidth / GRID_SIZE
                    val cellHeight = layoutHeight / GRID_SIZE

                    for (yPos in 0..GRID_SIZE - 1) {
                        for (xPos in 0..GRID_SIZE - 1) {
                            val params = bottomGrid[xPos][yPos].layoutParams as GridLayout.LayoutParams
                            params.width = cellWidth - 2 * MARGIN
                            params.height = cellHeight - 2 * MARGIN
                            params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN)
                            bottomGrid[xPos][yPos].layoutParams = params
                        }
                    }
                })
    }

    fun addView(x: Int, y: Int) {
        if(topGrid[x][y].parent != null) {
            val parent: ViewGroup = topGrid[x][y].parent as ViewGroup
            parent.removeView(topGrid[x][y])
            gameScreen_topGrid.addView(topGrid[x][y])
        } else {
            gameScreen_topGrid.addView(topGrid[x][y])
        }
    }

    //Decrements the ships health and sinks it if health drops to 0
    fun decrementShipHealth(shipTypeToDecrement: Ship) {
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
