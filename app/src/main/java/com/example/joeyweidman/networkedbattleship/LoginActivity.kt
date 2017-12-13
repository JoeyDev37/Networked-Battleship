package com.example.joeyweidman.networkedbattleship

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var name: EditText

    private lateinit var signin: Button
    private lateinit var signup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        email = findViewById(R.id.login_emailBox)
        password = findViewById(R.id.login_passwordBox)
        name = findViewById(R.id.login_nameBox)

        signin = findViewById(R.id.login_signInButton)
        signup = findViewById(R.id.login_signUpButton)

        //Check if user is already logged in
        if(mAuth.currentUser != null) {
            //User NOT logged in
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }

        signin.setOnClickListener {
            val getEmail: String = email.text.toString().trim()
            val getPassword: String = password.text.toString().trim()
            callSignin(getEmail, getPassword)
        }

        signup.setOnClickListener {
            val getEmail: String = email.text.toString().trim()
            val getPassword: String = password.text.toString().trim()
            callSignup(getEmail, getPassword)
        }
    }

    private fun callSignup(email: String, password: String) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task: Task<AuthResult> ->
            if (task.isSuccessful) {
                //Registration OK
                setUserDisplayName()
                Log.d("LoginActivity", "createUserWithEmail: success")
                Toast.makeText(this, "Created Account", Toast.LENGTH_SHORT).show()
            } else {
                //Registration error
                Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Set user display name
    private fun setUserDisplayName() {
        val firebaseUser = mAuth.currentUser!!

        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name.text.toString().trim()).build()

        firebaseUser.updateProfile(profileUpdates).addOnCompleteListener { task: Task<Void> ->
            if(task.isSuccessful) {
                Log.d("LoginActivity", "User Profile Updated")
            }
        }
    }

    //Now start sign in process
    private fun callSignin(email: String, password: String) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task: Task<AuthResult> ->
            if(task.isSuccessful) {
                Log.d("LoginActivity", "createUserWithEmail: success")
                Toast.makeText(this, "Signed In", Toast.LENGTH_SHORT).show()
                finish()
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
