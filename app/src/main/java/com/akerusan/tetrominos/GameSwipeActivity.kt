package com.akerusan.tetrominos

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.akerusan.tetrominos.adapter.CubeAdapter
import com.akerusan.tetrominos.piece.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_game.activity_game
import kotlinx.android.synthetic.main.activity_game.game_grid
import kotlinx.android.synthetic.main.activity_game_swipe.*
import kotlinx.android.synthetic.main.alert_dialog.*


open class GameSwipeActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener{

    private val START_TIME_IN_MILLIS: Long = 20000
    private var mTimeLeftInMillis = START_TIME_IN_MILLIS
    private var mCountDownTimer: CountDownTimer? = null
    private var launchCountDown : CountDownTimer? = null
    private var timeLeft = 3000L
    private var countDownIntervall = 1000L
    private var mTimerRunning: Boolean = false

    private val template = R.layout.template_swipe
    private var columnSize: Int = 0

    private var bottom: Boolean = false
    private var farRight: Boolean = false
    private var farLeft: Boolean = false

    var mediaPlayer: MediaPlayer? = null
    var paused: Boolean = false

    private var score: Int = 0
    private var currentLevel = 0

    private var you: Boolean = false
    private var em: Boolean = false
    private var grid: Boolean = false
    private var swipe: Boolean = false

    private var playing: Boolean = false
    private var fullRows: Int = 0
    private var next: Int = 0

    private var gridView: GridView? = null
    private var nextView: GridView? = null
    private val pieceList = ArrayList<Piece>()
    private val nextPiece = ArrayList<Piece>()
    private lateinit var mainBlock: Piece

    private var highScore: String? = null
    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null

    private lateinit var context: Context

    private var mRrootLayout: ViewGroup? = null
    private var prevX = 0
    private var prevY = 0
    private var xDown = 0
    private var yDown = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_swipe)
        context = applicationContext

        you = intent.getBooleanExtra("addYouBlock", false)
        em = intent.getBooleanExtra("addEmBlock", false)
        swipe = intent.getBooleanExtra("addswipe", false)
        highScore = intent.getStringExtra("highscore")

        swipe_high_score.text = highScore

        if (swipe){
            mRrootLayout = activity_game
            game_grid.setOnTouchListener(this)
        }

        grid = intent.getBooleanExtra("addgrid", false)
        if (grid){
            game_grid.background = resources.getDrawable(R.drawable.game_background_grid, null)
        }

        swipe_start.setOnClickListener(this)
        swipe_pause.setOnClickListener(this)
        swipe_resume.setOnClickListener(this)
        swipe_volumeOn.setOnClickListener(this)
        swipe_volumeOff.setOnClickListener(this)

        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser

        mediaPlayer = MediaPlayer.create(this, R.raw.tetromino)
        mediaPlayer!!.isLooping = true

        var timer = 4
        val scaleUpAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.scale_up_animation)
        launchCountDown = object : CountDownTimer(4000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                timer -= 1
                if (timer == 0){
                    swipe_countdown.text = "PLAY !"
                } else {
                    swipe_countdown.animate().translationX(1f).duration = 0
                    swipe_countdown.text = timer.toString()
                    swipe_countdown.startAnimation(scaleUpAnimation)
                }
            }
            override fun onFinish() {
                swipe_countdown.visibility = View.GONE
                swipe_start.performClick()
            }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        swipe_pause.performClick()
    }

    override fun onClick(v: View?) {
        if (v == swipe_start) {
            score = 0

            val dm = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(dm)

            mediaPlayer!!.start()
            playing = true

            swipe_start.visibility = View.GONE
            swipe_gameOver.visibility = View.GONE

            startGame()
        }

        if (v == swipe_pause){
            if (playing){
                pauseGame()
                mediaPlayer!!.pause()
                swipe_pause.visibility = View.GONE
                swipe_resume.visibility = View.VISIBLE

                swipe_volumeOff.isEnabled = false
                swipe_volumeOn.isEnabled = false
            }
        }

        if (v == swipe_resume){
            resumeGame()
            if(swipe_volumeOn.visibility == View.GONE){
                mediaPlayer!!.start()
            }
            swipe_pause.visibility = View.VISIBLE
            swipe_resume.visibility = View.GONE

            swipe_volumeOff.isEnabled = true
            swipe_volumeOn.isEnabled = true
        }

        if(v == swipe_volumeOff){
            mediaPlayer!!.pause()
            swipe_volumeOff.visibility = View.GONE
            swipe_volumeOn.visibility = View.VISIBLE
        }

        if(v == swipe_volumeOn){
            mediaPlayer!!.start()
            swipe_volumeOff.visibility = View.VISIBLE
            swipe_volumeOn.visibility = View.GONE
        }
    }

    private fun startGame() {
        val height = game_grid.height
        val width = game_grid.width

        if (height/2 > width){
            game_grid.layoutParams.height = (width*2)
            columnSize = game_grid.columnWidth
        } else {
            game_grid.layoutParams.width = (height/2)
            columnSize = ((height/2)/10)
        }
        initializeGrid()
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
                // randomly get a number
                if (!you && !em){
                    next = (1..7).shuffled().first()
                } else if (you && em){
                    next = (1..9).shuffled().first()
                } else if (you){
                    next = (1..8).shuffled().first()
                } else if (em){
                    val test = ((1..7).plus(9))
                    next = test.shuffled().first()
                }
            }

            // create random block
            val currentBlock = selectBlock(next)

            // display the selected block on screen
            val mAdapter = CubeAdapter(this, pieceList, template, columnSize)
            gridView!!.adapter = mAdapter
            // add the block as the main block
            mainBlock = currentBlock

            timer()

            // randomly get a number
            if (!you && !em){
                next = (1..7).shuffled().first()
            } else if (you && em){
                next = (1..9).shuffled().first()
            } else if (you){
                next = (1..8).shuffled().first()
            } else if (em){
                val test = ((1..7).plus(9))
                next = test.shuffled().first()
            }

        } else {
            // game is over
            swipe_start.visibility = View.VISIBLE
            if(paused){
                swipe_start.text = "Resume"
            } else if (!playing){
                swipe_gameOver.visibility = View.VISIBLE
                swipe_gameOver.animate().translationY(0f).duration = 1000
                swipe_start.text = "Retry"
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
            8 -> PieceU(pieceList, 14)
            9 -> PieceM(pieceList, 14)
            else -> Piece()
        }
    }

    private fun moveRight(){
        val check = mainBlock.checkRight(pieceList)
        if (check) {
            mainBlock.removeBlock(pieceList)
            mainBlock.moveRight(pieceList)

            val mAdapter = CubeAdapter(this, pieceList, template, columnSize)
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

            val mAdapter =
                CubeAdapter(this, pieceList, template, columnSize)
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

                val mAdapter =
                    CubeAdapter(this, pieceList, template, columnSize)
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

    private fun rotate(){
        val rotate = mainBlock.rotation(pieceList)

        if (rotate){
            if (mainBlock.rotation == 3) {
                mainBlock.rotation = 0
            } else {
                mainBlock.rotation += 1
            }
            val mAdapter =
                CubeAdapter(this, pieceList, template, columnSize)
            gridView!!.adapter = mAdapter
        }
        else {
            return
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
                    val mAdapter = CubeAdapter(
                        this,
                        pieceList, template, columnSize
                    )
                    gridView!!.adapter = mAdapter

                    fullRows += 1
                }
            }
        }
    }

    private fun checkingRows(){
        fullRows = 0
        // looking for full rows
        for (rowStart in 0 until 200 step 10) {
            val rowEnd = rowStart + 10
            fullRowCheck(rowStart, rowEnd)
        }
        // checking for tetris
        val point: Int = if (fullRows == 4){
            800
        } else {
            100 * fullRows
        }
        score += point
        swipe_totalScore.text = score.toString()
        checkHighScore(score)
    }

    private fun initializeGrid(){
        pieceList.clear()
        val piece = Piece()

        for (x in 0 until 200) {
            pieceList.add(piece)
        }

        mainBlock = Piece()

        gridView = game_grid
        val mAdapter = CubeAdapter(this, pieceList, template, columnSize)
        gridView!!.adapter = mAdapter
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
        swipe_level.text = currentLevel.toString()
    }

    private fun checkHighScore(score: Int){
        if (score > highScore!!.toInt()){
            if (user != null){
                // call db
                addScoreToDB(highScore!!)
            }
        }
    }

    private fun addScoreToDB(hs: String){

        val userId = mAuth!!.currentUser!!.uid

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .update("high_score", hs)
            .addOnSuccessListener {
                // nothing
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error adding document", Toast.LENGTH_LONG).show()
            }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        view.performClick()
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                prevX = x
                prevY = y
                xDown = x
                yDown = y
            }
            MotionEvent.ACTION_UP -> {
                if (xDown == x || yDown == y){
                    rotate()
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
            }
            MotionEvent.ACTION_POINTER_UP -> {
            }
            MotionEvent.ACTION_MOVE -> {
                val colWidth = game_grid.columnWidth
                // moving left
                if (x < prevX){
                    if (x < prevX-colWidth){
                        prevX = x
                        moveLeft()
                    }
                }
                // moving right
                else {
                    if (x > prevX+colWidth){
                        prevX = x
                        moveRight()
                    }
                }
                // moving down
                if (y > prevY+colWidth){
                    prevY = y
                    moveDown()
                }
            }
        }
        mRrootLayout!!.invalidate()
        return true
    }

    override fun onBackPressed() {
        swipe_pause.performClick()
        val dialog = Dialog(this@GameSwipeActivity)
        dialog.setContentView(R.layout.alert_dialog)
        dialog.show()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.dialog_cancel.setOnClickListener {
            swipe_resume.performClick()
            dialog.dismiss()
        }
        dialog.dialog_ok.setOnClickListener {
            super.onBackPressed()
            dialog.dismiss()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
}


