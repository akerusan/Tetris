package com.akerusan.tetromino

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alert_dialog_ok.*

open class MainActivity : AppCompatActivity(), View.OnClickListener{

    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var db: FirebaseFirestore? = null
    private var userName = ""
    private var highScore = "0"

    private var you: Boolean = false
    private var em: Boolean = false
    private var grid: Boolean = false
    private var swipe: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launchGame.setOnClickListener(this)
        login_main.setOnClickListener(this)
        register_main.setOnClickListener(this)
        logout_main.setOnClickListener(this)
        settings.setOnClickListener(this)
        ranking_main.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()

        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser

        if (user == null) {
            login_main.visibility = View.VISIBLE
            register_main.visibility = View.VISIBLE
            ranking_main.visibility = View.GONE
            logout_main.visibility = View.GONE
        } else {
            login_main.visibility = View.GONE
            register_main.visibility = View.GONE
            ranking_main.visibility = View.VISIBLE
            logout_main.visibility = View.VISIBLE

            val userId = user!!.uid

            db = FirebaseFirestore.getInstance()
            db!!.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { result ->
                    userName = result.data!!["username"].toString()
                    highScore = result.data!!["high_score"].toString()
                    profile_name.visibility = View.VISIBLE
                    profile_highscore.visibility = View.VISIBLE
                    profile_name.text = resources.getString(R.string.welcome, userName)
                    profile_highscore.text = resources.getString(R.string.highscore, highScore)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error getting documents.", Toast.LENGTH_LONG).show()
                }
        }

        if (userName.isNotEmpty()) {
            profile_name.visibility = View.VISIBLE
            profile_name.text = resources.getString(R.string.welcome, userName)
        } else {
            profile_name.visibility = View.INVISIBLE
            profile_highscore.visibility = View.INVISIBLE
        }
    }

    override fun onClick(v: View?) {

        when (v) {
            launchGame -> {
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("highscore", highScore)
                intent.putExtra("addYouBlock", you)
                intent.putExtra("addEmBlock", em)
                intent.putExtra("addgrid", grid)
                intent.putExtra("addswipe", swipe)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.putExtra("addYouBlock", you)
                intent.putExtra("addEmBlock", em)
                intent.putExtra("addgrid", grid)
                intent.putExtra("addswipe", swipe)
                startActivityForResult(intent, 0)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            register_main -> {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivityForResult(intent, 2)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            login_main -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivityForResult(intent, 1)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            ranking_main -> {
                val intent = Intent(this, RankingActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            logout_main -> {
                user = null
                userName = ""
                highScore = "0"
                mAuth!!.signOut()
                Toast.makeText(this, "Log Out Successful", Toast.LENGTH_LONG).show()
                this.onResume()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0){
            if (resultCode == Activity.RESULT_OK){
                you = data!!.getBooleanExtra("addYouBlock", false)
                em = data.getBooleanExtra("addEmBlock", false)
                grid = data.getBooleanExtra("addgrid", false)
                swipe = data.getBooleanExtra("addswipe", false)
            }
        } else if (requestCode == 1){
            if (resultCode == Activity.RESULT_OK){
                Toast.makeText(this, "Log In Successful!", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == 2){
            if (resultCode == Activity.RESULT_OK){
                Toast.makeText(this, "SignUp Successful!", Toast.LENGTH_LONG).show()
            }
        }
    }
}


