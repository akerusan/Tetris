package com.akerusan.tetromino

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*

open class SettingsActivity : AppCompatActivity(), View.OnClickListener {

    private var you: Boolean? = null
    private var em: Boolean? = null
    private var grid: Boolean? = null
    private var swipe: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        you = intent.getBooleanExtra("addYouBlock", false)
        em = intent.getBooleanExtra("addEmBlock", false)
        grid = intent.getBooleanExtra("addgrid", false)
        swipe = intent.getBooleanExtra("addswipe", false)

        add_grid_block.isChecked = grid!!
        add_swipe_mode.isChecked = swipe!!

        toAddBlocks.setOnClickListener(this)
        apply_settings_btn.setOnClickListener(this)

        add_grid_block.setOnCheckedChangeListener { _, isChecked ->
            grid = isChecked
        }
        add_swipe_mode.setOnCheckedChangeListener { _, isChecked ->
            swipe = isChecked
        }
    }

    override fun onClick(v: View?) {
        if (v == apply_settings_btn){
            val returnIntent = Intent()
            returnIntent.putExtra("addYouBlock", you)
            returnIntent.putExtra("addEmBlock", em)
            returnIntent.putExtra("addgrid", grid)
            returnIntent.putExtra("addswipe", swipe)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        else if (v == toAddBlocks){
            val intent = Intent(this, AddBlocksActivity::class.java)
            intent.putExtra("addYouBlock", you)
            intent.putExtra("addEmBlock", em)
            startActivityForResult(intent, 0)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0){
            if (resultCode == Activity.RESULT_OK){
                you = data!!.getBooleanExtra("addYouBlock", false)
                em = data.getBooleanExtra("addEmBlock", false)
            }
        }
    }

    override fun onBackPressed() {
        apply_settings_btn.performClick()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}