package com.example.joeyweidman.networkedbattleship

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import java.text.FieldPosition


class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    val GRID_SIZE = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        //Check if the user is logged in
        if(mAuth.currentUser == null) {
            //user NOT logged in
            finish()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        //Get the display name of the current user
        val currentUser: FirebaseUser? = mAuth.currentUser
        if(currentUser != null) {
            val welcomeText = StringBuilder()
            welcomeText.append("Hello ")
            welcomeText.append(currentUser.displayName)
            welcomeText.append("!")
            main_userNameText.text = welcomeText.toString()
        }

        main_logOutButton.setOnClickListener {
            mAuth.signOut()
            finish()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        main_gameListView.setOnItemClickListener { parent, view, position, id ->
            NetworkedBattleship.LoadGame(filesDir.listFiles()[position])
            startActivity(Intent(this, GameScreenActivity::class.java))
        }

        // Example of a call to a native method
        //sample_text.text = stringFromJNI()

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

        main_gameListView.adapter = MyCustomAdapter(this)
    }

    private class MyCustomAdapter(context: Context): BaseAdapter() {

        private val mContext: Context

        init {
            mContext = context
        }

        //Responsible for rendering each row
        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val rowMain = layoutInflater.inflate(R.layout.row_main, viewGroup, false)

            val gameNameText = rowMain.findViewById<TextView>(R.id.gameName_textView)
            gameNameText.text = "Game $position"

            val gameDetailsText = rowMain.findViewById<TextView>(R.id.gameDetails_textView)
            gameDetailsText.text = "Game Details"
            return rowMain
        }

        override fun getItem(position: Int): Any {
            return "TEST STRING"
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        //Responsible for how many rows in the list
        override fun getCount(): Int {
            return mContext.filesDir.listFiles().size
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
