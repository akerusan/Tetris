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
import kotlinx.android.synthetic.main.activity_login.*


open class LoginActivity : AppCompatActivity(), View.OnClickListener{

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_screen.setOnTouchListener { _, _ ->
            hideSoftKeyboard(this@LoginActivity)
            false
        }

        FirebaseApp.initializeApp(this)
        mAuth = FirebaseAuth.getInstance()

        signin_btn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val email = signin_id.text.toString()
        val password = signin_password.text.toString()

        if (email.isEmpty()){
            signin_id.error = "Please enter you email"
            signin_id.requestFocus()
        }
        else if (password.isEmpty()){
            signin_password.error = "Please enter your password"
            signin_password.requestFocus()
        }
        else if (email.isNotEmpty() && password.isNotEmpty()){
            mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val returnIntent = Intent()
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    } else {
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_LONG).show()
                    }

                    // ...
                }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}