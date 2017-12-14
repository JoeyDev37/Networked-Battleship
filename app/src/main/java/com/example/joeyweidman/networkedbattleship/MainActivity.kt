package com.example.joeyweidman.networkedbattleship

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.text.FieldPosition
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.provider.ContactsContract
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.google.android.gms.internal.lv




class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var rootRef: DatabaseReference
    private lateinit var gamesRef: DatabaseReference
    private lateinit var listOfGames: MutableList<Pair<String, Triple<String, String, String>>>
    //private lateinit var listOfGames: MutableList<String>

    private var canJoinGame: Boolean = false

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

        main_gameListView.setOnItemClickListener { parent, view, position, id ->

            val gameIdRef = gamesRef.child(listOfGames[position].first)
            val gameIdListenter = object:ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    if(dataSnapshot!!.child("player1").child("ID").value == mAuth.currentUser!!.uid) {
                        //P1 joins their own game
                        val intent = Intent(this@MainActivity, GameScreenActivity::class.java)
                        NetworkedBattleship.LoadGame(dataSnapshot.child("json").value as String)
                        intent.putExtra("KEY", listOfGames[position].first)
                        intent.putExtra("PLAYER", 1)
                        startActivity(intent)
                    } else if(dataSnapshot!!.child("player2").child("ID").value == mAuth.currentUser!!.uid) {
                        //P2 joins their own game
                        val intent = Intent(this@MainActivity, GameScreenActivity::class.java)
                        NetworkedBattleship.LoadGame(dataSnapshot.child("json").value as String)
                        intent.putExtra("KEY", listOfGames[position].first)
                        intent.putExtra("PLAYER", 2)
                        startActivity(intent)
                    } else if(dataSnapshot!!.child("player2").child("ID").value == null) {
                        //P2 joins in the open slot
                        gameIdRef.child("player2").child("ID").setValue(mAuth.currentUser!!.uid)
                        gameIdRef.child("player2").child("name").setValue(currentUser!!.displayName)
                        gameIdRef.child("gameState").setValue(GameState.IN_PROGRESS)
                        val intent = Intent(this@MainActivity, GameScreenActivity::class.java)
                        NetworkedBattleship.LoadGame(dataSnapshot.child("json").value as String)
                        intent.putExtra("KEY", listOfGames[position].first)
                        intent.putExtra("PLAYER", 2)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@MainActivity, GameScreenActivity::class.java)
                        NetworkedBattleship.LoadGame(dataSnapshot.child("json").value as String)
                        intent.putExtra("KEY", listOfGames[position].first)
                        intent.putExtra("PLAYER", 0)
                        startActivity(intent)
                    }
                }

            }
            gameIdRef.addListenerForSingleValueEvent(gameIdListenter)
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
                        val nameP1 = game.child("player1").child("name").value.toString()
                        val nameP2 = game.child("player2").child("name").value.toString()
                        val gameStatus = game.child("gameState").value.toString()
                        val key: String = game.key
                        val triple = Triple(nameP1, nameP2, gameStatus)
                        val pair = Pair(key, triple)

                        //Don't show completed games that you weren't part of
                        val IDPlayer1 = game.child("player1").child("ID").value.toString()
                        val IDPlayer2 = game.child("player2").child("ID").value.toString()
                        if( game.child("gameState").value.toString() == GameState.P1_VICTORY.toString() || game.child("gameState").value.toString() == GameState.P2_VICTORY.toString()) {
                            if(mAuth.currentUser!!.uid != IDPlayer1 && mAuth.currentUser!!.uid != IDPlayer2) {
                                continue
                            }
                        }
                        listOfGames.add(pair)
                    }
                    val adapter = MyCustomAdapter(this@MainActivity, listOfGames, rootRef)
                    main_gameListView.adapter = adapter
                }
            }
        }

        gamesRef.addValueEventListener(gameListener)

        /* Starts a new game and puts the user in it */
        main_newGameButton.setOnClickListener {
            NetworkedBattleship.CleanGame() //Initialize the game boards
            NetworkedBattleship.PlaceShipsRandomly() //Place ships randomly for P1 and P2
            val key = NetworkedBattleship.WriteNewGame(mAuth.currentUser!!.uid, currentUser.displayName, GameState.STARTING) //Start a new game and add the user with his id and name
            NetworkedBattleship.UpdateGame(key)
            val intent = Intent(this, GameScreenActivity::class.java)
            intent.putExtra("KEY", key)
            intent.putExtra("PLAYER", 1)
            startActivity(intent)
        }
    }

    private class MyCustomAdapter(context: Context, list: MutableList<Pair<String, Triple<String, String, String>>>, rootRef: DatabaseReference): BaseAdapter() {

        private val mContext: Context
        private val list: MutableList<Pair<String, Triple<String, String, String>>>
        private val rootRef: DatabaseReference

        init {
            mContext = context
            this.list = list
            this.rootRef = rootRef
        }

        //Responsible for rendering each row
        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val rowMain = layoutInflater.inflate(R.layout.row_main, viewGroup, false)

            val gameNameText = rowMain.findViewById<TextView>(R.id.gameName_textView)
            var nameP1 = list[position].second.first
            var nameP2 = list[position].second.second
            if(nameP2 == "null")
                nameP2 = ""
            gameNameText.text = "$nameP1 VS $nameP2"

            val deleteButton = rowMain.findViewById<Button>(R.id.deleteButton)

            deleteButton.setOnClickListener {
                rootRef.child("games").child(list[position].first).removeValue()
            }

            //Show the delete button if the game is completed
            if(list[position].second.third == GameState.P1_VICTORY.toString() || list[position].second.third == GameState.P2_VICTORY.toString()) {
                deleteButton.visibility = View.VISIBLE
            }

            val gameDetailsText = rowMain.findViewById<TextView>(R.id.gameDetails_textView)
            gameDetailsText.text = list[position].second.third
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
