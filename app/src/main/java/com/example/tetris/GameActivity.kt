package com.example.tetris

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.GridView
import com.example.tetris.piece.*
import kotlinx.android.synthetic.main.activity_game.*
import kotlin.collections.ArrayList

open class GameActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener{

    private val START_TIME_IN_MILLIS: Long = 20000
    private var mTimeLeftInMillis = START_TIME_IN_MILLIS
    private var mCountDownTimer: CountDownTimer? = null
    private var launchCountDown : CountDownTimer? = null
    private var timeLeft = 3000L
    private var countDownIntervall = 1000L
    private var mTimerRunning: Boolean = false

    private var width: Int = 0

    private var bottom: Boolean = false
    private var farRight: Boolean = false
    private var farLeft: Boolean = false

    var mediaPlayer: MediaPlayer? = null
    var paused: Boolean = false

    private var score: Int = 0
    private var currentLevel = 0

    private var playing: Boolean = false
    private var fullRows: Int = 0
    private var next: Int = 0

    private var gridView: GridView? = null
    private var nextView: GridView? = null
    private val pieceList = ArrayList<Piece>()
    private val nextPiece = ArrayList<Piece>()
    private lateinit var mainBlock: Piece

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Hide the status bar.
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        actionBar?.hide()

        start.setOnClickListener(this)

        mediaPlayer = MediaPlayer.create(this, R.raw.tetris)
        mediaPlayer!!.isLooping = true

        val scaleUpAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.scale_up_animation)

        var timer = 4
        launchCountDown = object : CountDownTimer(4000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                timer -= 1
                if (timer == 0){
                    countdown.text = "PLAY !"
                } else {
//                    countdown.animate().translationX(1f).duration = 0
                    countdown.text = timer.toString()
                    countdown.startAnimation(scaleUpAnimation)
                }
            }
            override fun onFinish() {
                countdown.visibility = View.GONE
                start.performClick()
            }
        }.start()


    }

    override fun onLongClick(v: View?): Boolean {

        if (v == left){
            farLeft = false
            while (!farLeft){
                moveLeft()
            }
        }
        else if (v == right) {
            farRight = false
            while (!farRight){
                moveRight()
            }
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        pause.performClick()
    }

//    override fun onResume() {
//        super.onResume()
//
//        if(mediaPlayer!!.isPlaying){
//            mediaPlayer!!.start()
//        }
//        if (playing){
//            resumeGame()
//        }
//
//    }

    override fun onClick(v: View?) {

        if (v == start) {

            enableButtons()

            mediaPlayer!!.start()

            initializeGrid()
            playing = true

            width = gridView!!.width
            start.visibility = View.GONE
            gameOver.visibility = View.GONE

            startGame()
        }

        if (v == pause){
            if (playing){
                pauseGame()
                mediaPlayer!!.pause()
                pause.visibility = View.GONE
                resume.visibility = View.VISIBLE
            }
        }

        if (v == resume){
            resumeGame()
            if(volumeOn.visibility == View.GONE){
                mediaPlayer!!.start()
            }
            pause.visibility = View.VISIBLE
            resume.visibility = View.GONE
        }

        if (v == rotate) {

            if (mainBlock.rotation == 3) {
                mainBlock.rotation = 0
            } else {
                mainBlock.rotation += 1
            }

            mainBlock.rotation(pieceList)

            val mAdapter = CubeAdapter(this, pieceList)
            gridView!!.adapter = mAdapter

        }

        if (v == right) {
            moveRight()
        }

        if (v == left) {
            moveLeft()
        }

        if (v == down){
            // initialization
            bottom = false
            // sending block to bottom
            while (!bottom){
                moveDown()
            }
        }

        if(v == volumeOff){
            mediaPlayer!!.pause()
            volumeOff.visibility = View.GONE
            volumeOn.visibility = View.VISIBLE
        }
        if(v == volumeOn){
            mediaPlayer!!.start()
            volumeOff.visibility = View.VISIBLE
            volumeOn.visibility = View.GONE
        }
    }


    private fun startGame() {
        sendNewBlock()
    }

    private fun pauseGame(){
            playing = false
            paused = true
            pauseTimer()
    }
    private fun resumeGame(){
        playing = true
        paused = false
        resumeTimer()
    }

    private fun sendNewBlock() {

        if (playing){

            checkingRows()
            checkingScore()

            if (next == 0){
                // randomly get a number from 1 to 7
                next = (1..7).shuffled().first()
            }

            // create random block
            val currentBlock = selectBlock(next)

            // display the selected block on screen
            val mAdapter = CubeAdapter(this, pieceList)
            gridView!!.adapter = mAdapter
            // add the block as the main block
            mainBlock = currentBlock

            timer()

            // randomly get a number from 1 to 7
            next = (1..7).shuffled().first()
            // display the next block on screen
            displayNext(next)

        } else {
            // game is over
            start.visibility = View.VISIBLE
            if(paused){
                start.text = "Resume"
            } else if (!playing){
                gameOver.visibility = View.VISIBLE
                gameOver.animate().translationY(0f).duration = 1000
                start.text = "Retry"
            }
        }
    }

    private fun selectBlock(next: Int) : Piece {

        return when (next) {
            1 -> PieceT(pieceList, 4)
            2 -> PieceO(pieceList, 4)
            3 -> PieceL(pieceList, 4)
            4 -> PieceI(pieceList, 4)
            5 -> PieceJ(pieceList, 4)
            6 -> PieceZ(pieceList, 4)
            7 -> PieceS(pieceList, 4)
            else -> Piece()
        }
    }

    private fun displayNext(next: Int){

        nextPiece.clear()

        val piece = Piece()
        for (x in 0 until 20) {
            nextPiece.add(piece)
        }

        NextPiece(nextPiece, next)

        nextView = nextBlock
        val mNextBlockAdaptater = CubeAdapter(this, nextPiece)
        nextView!!.adapter = mNextBlockAdaptater
    }

    private fun moveRight(){
        val check = mainBlock.checkRight(pieceList)
        if (check) {
            mainBlock.removeBlock(pieceList)
            mainBlock.moveRight(pieceList)

            val mAdapter = CubeAdapter(this, pieceList)
            gridView!!.adapter = mAdapter
        } else {
            farRight = true
        }
    }

    private fun moveLeft(){
        val check = mainBlock.checkLeft(pieceList)
        if (check) {
            mainBlock.removeBlock(pieceList)
            mainBlock.moveLeft(pieceList)

            val mAdapter = CubeAdapter(this, pieceList)
            gridView!!.adapter = mAdapter
        } else {
            farLeft = true
        }
    }

    private fun moveDown() {

        if (mainBlock.detectBottom(pieceList)) {

            val check = mainBlock.checkDown(pieceList)

            if (check) {
                mainBlock.removeBlock(pieceList)
                mainBlock.moveDown(pieceList)

                val mAdapter = CubeAdapter(this, pieceList)
                gridView!!.adapter = mAdapter
            } else {
                bottom = true
                // checking if gameover
                if (pieceList[4].axe == mainBlock.axe){
                    playing = false
                    mediaPlayer!!.stop()
                }
                cancelTimer()
                sendNewBlock()
            }
        } else {
            bottom = true
            // checking if gameover
            if (pieceList[4].axe == mainBlock.axe){
                playing = false
            }
            cancelTimer()
            sendNewBlock()
        }
    }

    private fun timer() {
        mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, countDownIntervall) {
            override fun onTick(millisUntilFinished: Long) {
                mTimeLeftInMillis = millisUntilFinished
                moveDown()
            }

            override fun onFinish() {
                mTimerRunning = false
            }
        }.start()

        mTimerRunning = true
    }

    private fun pauseTimer(){
        mCountDownTimer!!.cancel()
        mTimerRunning = false
    }
    private fun resumeTimer(){
        mCountDownTimer!!.start()
        mTimerRunning = true
    }

    private fun cancelTimer() {
        mTimeLeftInMillis = START_TIME_IN_MILLIS
        mCountDownTimer!!.onFinish()
        mCountDownTimer!!.cancel()
    }

    private fun fullRowCheck(rowStart: Int, rowEnd: Int) {

        for (i in rowStart until rowEnd) {
            if (pieceList[i].block == "") {
                break
            } else {
                if (i == rowStart + 9) {
                    for (j in rowStart until rowEnd) {
                        pieceList[j] = Piece()
                    }
                    for (z in rowStart downTo 0) {
                        if (pieceList[z].block != "") {

                            pieceList[z + 10].axe = pieceList[z].axe + 10
                            pieceList[z + 10].block = pieceList[z].block

                            pieceList[z].axe = 0
                            pieceList[z].block = ""
                        }
                    }
                    val mAdapter = CubeAdapter(this, pieceList)
                    gridView!!.adapter = mAdapter

                    fullRows += 1
                }
            }
        }
    }

    private fun checkingRows(){
        var point = 0
        fullRows = 0
        // looking for full rows
        for (rowStart in 0 until 200 step 10) {
            val rowEnd = rowStart + 10
            fullRowCheck(rowStart, rowEnd)
        }
        // checking for tetris
        point = if (fullRows == 4){
            800
        } else {
            100 * fullRows
        }
        score += point
        totalScore.text = score.toString()
    }

    private fun initializeGrid(){

        pieceList.clear()
        val piece = Piece()

        for (x in 0 until 200) {
            pieceList.add(piece)
        }

        mainBlock = Piece()

        gridView = game_grid
        val mAdapter = CubeAdapter(this, pieceList)
        gridView!!.adapter = mAdapter
    }

    private fun enableButtons(){
        left.setOnClickListener(this)
        left.setOnLongClickListener(this)
        right.setOnClickListener(this)
        right.setOnLongClickListener(this)
        rotate.setOnClickListener(this)
        down.setOnClickListener(this)
        pause.setOnClickListener(this)
        resume.setOnClickListener(this)
        volumeOff.setOnClickListener(this)
        volumeOn.setOnClickListener(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun checkingScore(){

        when (currentLevel) {
            0 -> {
                if (score > 1000){
                    nextLevel()
                }
            }
            1 -> {
                if (score > 2000){
                    nextLevel()
                }
            }
            2 -> {
                if (score > 3000){
                    nextLevel()
                }
            }
            3 -> {
                if (score > 4000){
                    nextLevel()
                }
            }
            4 -> {
                if (score > 5000){
                    nextLevel()
                }
            }
            5 -> {
                if (score > 6000){
                    nextLevel()
                }
            }
            6 -> {
                if (score > 7000){
                    nextLevel()
                }
            }
            7 -> {
                if (score > 8000){
                    nextLevel()
                }
            }
            8 -> {
                if (score > 9000){
                    nextLevel()
                }
            }
        }
    }

    private fun nextLevel(){
        currentLevel += 1
        countDownIntervall -= 100L
        level.text = currentLevel.toString()
    }
}


