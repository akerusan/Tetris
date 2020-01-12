package com.akerusan.tetromino

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.akerusan.tetromino.common.hideSoftKeyboard
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*

open class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_screen.setOnTouchListener { _, _ ->
            hideSoftKeyboard(this@RegisterActivity)
            false
        }

        FirebaseApp.initializeApp(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        signup_btn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val username = signup_username.text.toString()
        val email = signup_id.text.toString()
        val password = signup_password.text.toString()

        if (username.isEmpty()){
            signup_username.error = "Please enter you username"
            signup_username.requestFocus()
        }
        else if (email.isEmpty()){
            signup_id.error = "Please enter your email"
            signup_id.requestFocus()
        }
        else if (password.isEmpty()){
            signup_password.error = "Please enter your password"
            signup_password.requestFocus()
        }
        else if (email.isNotEmpty() && password.isNotEmpty()){
            mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // creating users info in Db
                        createUserDB(username, email)
                        // going back to home
                        val returnIntent = Intent()
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, "SignUp Unsuccessful, Please Try Again", Toast.LENGTH_LONG).show()
                    }
                }
        }
        else {
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createUserDB(username: String, email: String){

        val userId = mAuth!!.currentUser!!.uid

        // Create a new user with a first and last name
        val user = hashMapOf(
            "username" to username,
            "email" to email,
            "high_score" to 0
        )

        // Add a new document with a generated ID
        db!!.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "DocumentSnapshot added", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error adding document", Toast.LENGTH_LONG).show()
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}