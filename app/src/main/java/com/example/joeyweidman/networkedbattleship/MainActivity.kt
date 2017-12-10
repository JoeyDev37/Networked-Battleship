package com.example.joeyweidman.networkedbattleship

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    val GRID_SIZE = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = stringFromJNI()

        //Initialize all grids. Set empty values (because nothing is in them yet)
        NetworkedBattleship.topGridP1 = Array(10, {Array(10, {Triple(Status.EMPTY, Ship.NONE, true)})})
        NetworkedBattleship.bottomGridP1 = Array(10, {Array(10, {Triple(Status.EMPTY, Ship.NONE, false)})})
        NetworkedBattleship.topGridP2 = Array(10, {Array(10, {Triple(Status.EMPTY, Ship.NONE, true)})})
        NetworkedBattleship.bottomGridP2 = Array(10, {Array(10, {Triple(Status.EMPTY, Ship.NONE, false)})})

        main_newGameButton.setOnClickListener {
            NetworkedBattleship.PlaceShipsRandomly() //Place ships randomly for P1 and P2
            val intent = Intent(this, GameScreenActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
