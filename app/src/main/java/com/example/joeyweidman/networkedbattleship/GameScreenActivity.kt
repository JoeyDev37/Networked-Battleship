package com.example.joeyweidman.networkedbattleship

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.GridLayout
import kotlinx.android.synthetic.main.activity_game_screen.*

class GameScreenActivity : AppCompatActivity() {
    val GRID_SIZE = 10
    lateinit var topGrid: Array<Array<Cell>>
    lateinit var bottomGrid: Array<Array<Cell>>

    /* OnCreate will read from the global data to create new game grid
     * every time the GameScreenActivity is started. So every time the player
     * views the game screen, it is updated with the most recent data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_screen)

        NetworkedBattleship.gameState

        gameScreen_saveButton.setOnClickListener {
            NetworkedBattleship.SaveGame(this)
        }

        if(NetworkedBattleship.currentPlayer == 1) {
            gameScreen_currentPlayerText.text = "P1 TURN"
        } else if(NetworkedBattleship.currentPlayer == 2) {
            gameScreen_currentPlayerText.text = "P2 TURN"
        }

        gameScreen_gameStatusText.text = NetworkedBattleship.gameState.toString()

        topGrid = Array(10, {Array(10, {Cell(this, 0, 0, true)})})
        bottomGrid = Array(10, {Array(10, {Cell(this, 0, 0, false)})})

        /* Read from the global grid data to update and re-draw the cell grid */
        if(NetworkedBattleship.currentPlayer == 1) {
            for(y in 0..GRID_SIZE - 1) {
                for(x in 0..GRID_SIZE - 1) {
                    var cell = Cell(this, x, y, true)
                    topGrid[x][y] = cell
                    topGrid[x][y].currentStatus = NetworkedBattleship.topGridP1[x][y].first
                    topGrid[x][y].shipType = NetworkedBattleship.topGridP1[x][y].second
                    topGrid[x][y].isTouchable = NetworkedBattleship.topGridP1[x][y].third

                    gameScreen_topGrid.addView(topGrid[x][y])
                    //addView(x, y)

                    cell = Cell(this, x, y, false)
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
                    var cell = Cell(this, x, y, true)
                    topGrid[x][y] = cell
                    topGrid[x][y].currentStatus = NetworkedBattleship.topGridP2[x][y].first
                    topGrid[x][y].shipType = NetworkedBattleship.topGridP2[x][y].second
                    topGrid[x][y].isTouchable = NetworkedBattleship.topGridP2[x][y].third

                    gameScreen_topGrid.addView(topGrid[x][y])
                    //addView(x, y)

                    cell = Cell(this, x, y, false)
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
}
