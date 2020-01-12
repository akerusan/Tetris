package com.akerusan.tetromino

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.akerusan.tetromino.adapter.CubeAdapter
import com.akerusan.tetromino.piece.*
import kotlinx.android.synthetic.main.activity_add_blocks.*
import kotlinx.android.synthetic.main.activity_settings.*

open class AddBlocksActivity : AppCompatActivity(), View.OnClickListener {

    private val pieceListU = ArrayList<Piece>()
    private val pieceListM = ArrayList<Piece>()
    private var you: Boolean? = null
    private var em: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_blocks)

        you = intent.getBooleanExtra("addYouBlock", false)
        em = intent.getBooleanExtra("addEmBlock", false)
        switch_add_block_1.isChecked = you!!
        switch_add_block_2.isChecked = em!!

        addPieceU()
        addPieceM()

        switch_add_block_1.setOnCheckedChangeListener { _, switchOn ->
            you = switchOn
        }
        switch_add_block_2.setOnCheckedChangeListener { _, switchOn ->
            em = switchOn
        }

        apply_addBlock_btn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v == apply_addBlock_btn){
            val returnIntent = Intent()
            returnIntent.putExtra("addYouBlock", you)
            returnIntent.putExtra("addEmBlock", em)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun addPieceU(){
        val piece = Piece()
        for (x in 0 until 20) {
            pieceListU.add(piece)
        }
        NextPiece(pieceListU, 8)

        val addBlockAdaptater1 = CubeAdapter(this, pieceListU)
        add_block_1!!.adapter = addBlockAdaptater1
    }

    private fun addPieceM(){
        val piece = Piece()
        for (x in 0 until 20) {
            pieceListM.add(piece)
        }
        NextPiece(pieceListM, 9)

        val addBlockAdaptater2 = CubeAdapter(this, pieceListM)
        add_block_2!!.adapter = addBlockAdaptater2
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}