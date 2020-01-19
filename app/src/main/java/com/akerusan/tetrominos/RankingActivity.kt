package com.akerusan.tetrominos

import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.akerusan.tetrominos.adapter.RankingAdapter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_ranking.*

open class RankingActivity : AppCompatActivity() {

    private var db: FirebaseFirestore? = null
    private val rankList = ArrayList<Pair<Int, String>>()
    private var listView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        db = FirebaseFirestore.getInstance()
        db!!.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result){
                    val username = document.data["username"].toString()
                    val highScore = document.data["high_score"].toString()
                    rankList.add(Pair(highScore.toInt(), username))
                }
                val sortedRank = rankList.sortedWith(compareByDescending{ it.first })

                // display the ranking table
                listView = ranking_listview
                val mAdapter = RankingAdapter(this, sortedRank)
                listView!!.adapter = mAdapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error getting documents.", Toast.LENGTH_LONG).show()
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}