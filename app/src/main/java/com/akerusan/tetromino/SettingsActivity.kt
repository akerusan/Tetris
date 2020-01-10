package com.akerusan.tetromino

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*

open class SettingsActivity : AppCompatActivity(), View.OnClickListener {

    private var you: Boolean? = null
    private var grid: Boolean? = null
    private var swipe: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        you = intent.getBooleanExtra("addblock", false)
        grid = intent.getBooleanExtra("addgrid", false)
        swipe = intent.getBooleanExtra("addswipe", false)

        add_you_block.isChecked = you!!
        add_grid_block.isChecked = grid!!
        add_swipe_mode.isChecked = swipe!!

        apply_settings_btn.setOnClickListener(this)

        add_you_block.setOnCheckedChangeListener { _, isChecked ->
            you = isChecked
        }
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
            returnIntent.putExtra("addblock", you)
            returnIntent.putExtra("addgrid", grid)
            returnIntent.putExtra("addswipe", swipe)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}