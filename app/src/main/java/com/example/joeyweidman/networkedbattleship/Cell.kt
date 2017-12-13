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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_game_screen.*

/**
 * Created by Joey Weidman
 */
class Cell : View {
    constructor(context: Context?, x: Int, y: Int) : super(context) {
        this.x = x
        this.y = y
    }
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    var x: Int = 0
    var y: Int = 0

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
}