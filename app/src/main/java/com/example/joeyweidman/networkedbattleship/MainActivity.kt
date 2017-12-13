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
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.text.FieldPosition


class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var rootRef: DatabaseReference
    private lateinit var gamesRef: DatabaseReference
    private lateinit var listOfGames: MutableList<String>

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
        val welcomeText = StringBuilder()
        welcomeText.append("Hello ")
        welcomeText.append(currentUser!!.displayName)
        welcomeText.append("!")
        main_userNameText.text = welcomeText.toString()

        main_logOutButton.setOnClickListener {
            mAuth.signOut()
            finish()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        listOfGames = mutableListOf()

        rootRef = FirebaseDatabase.getInstance().reference
        gamesRef = rootRef.child("games")

        val gameListener = object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                listOfGames.clear()
                if(dataSnapshot!!.exists()) {
                    for(game in dataSnapshot.children) {
                        val string: String = game.key
                        listOfGames.add(string)
                    }
                    val adapter = MyCustomAdapter(this@MainActivity, listOfGames)
                    main_gameListView.adapter = adapter
                }
            }
        }

        gamesRef.addValueEventListener(gameListener)

        main_gameListView.setOnItemClickListener { parent, view, position, id ->
            //NetworkedBattleship.LoadGame(filesDir.listFiles()[position])
            //startActivity(Intent(this, GameScreenActivity::class.java))
            
        }

        // Example of a call to a native method
        //sample_text.text = stringFromJNI()

        //Initialize all grids. Set empty values (because nothing is in them yet)
        NetworkedBattleship.topGridP1 = Array(10, {Array(10, {Triple(Status.EMPTY, Ship.NONE, true)})})
        NetworkedBattleship.bottomGridP1 = Array(10, {Array(10, {Triple(Status.EMPTY, Ship.NONE, false)})})
        NetworkedBattleship.topGridP2 = Array(10, {Array(10, {Triple(Status.EMPTY, Ship.NONE, true)})})
        NetworkedBattleship.bottomGridP2 = Array(10, {Array(10, {Triple(Status.EMPTY, Ship.NONE, false)})})

        /* Starts a new game and puts the user in it */
        main_newGameButton.setOnClickListener {
            NetworkedBattleship.cleanGame() //Initialize the game boards
            NetworkedBattleship.PlaceShipsRandomly() //Place ships randomly for P1 and P2
            val key = NetworkedBattleship.writeNewGame(mAuth.currentUser!!.uid) //Start a new game and add the user with his id
            val intent = Intent(this, GameScreenActivity::class.java)
            intent.putExtra("KEY", key)
            startActivity(intent)
        }

        main_gameListView.setOnItemClickListener { parent, view, position, id ->

        }
    }

    private class MyCustomAdapter(context: Context, list: MutableList<String>): BaseAdapter() {

        private val mContext: Context
        private val list: MutableList<String>

        init {
            mContext = context
            this.list = list
        }

        //Responsible for rendering each row
        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val rowMain = layoutInflater.inflate(R.layout.row_main, viewGroup, false)

            val gameNameText = rowMain.findViewById<TextView>(R.id.gameName_textView)
            gameNameText.text = "Game $position"

            val gameDetailsText = rowMain.findViewById<TextView>(R.id.gameDetails_textView)
            gameDetailsText.text = list[position]
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
            return list.size
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
