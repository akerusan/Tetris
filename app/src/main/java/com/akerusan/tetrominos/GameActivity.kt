package com.akerusan.tetrominos

import android.app.Dialog
import android.content.Context
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
import com.akerusan.tetrominos.common.OnSwipeTouchListener
import com.akerusan.tetrominos.piece.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.alert_dialog.*
import kotlin.math.roundToInt


open class GameActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener, View.OnTouchListener{

    private val START_TIME_IN_MILLIS: Long = 20000
    private var mTimeLeftInMillis = START_TIME_IN_MILLIS
    private var mCountDownTimer: CountDownTimer? = null
    private var launchCountDown : CountDownTimer? = null
    private var timeLeft = 3000L
    private var countDownIntervall = 1000L
    private var mTimerRunning: Boolean = false

    private val template = R.layout.template

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
        context = applicationContext
        adaptToScreen()

        you = intent.getBooleanExtra("addYouBlock", false)
        em = intent.getBooleanExtra("addEmBlock", false)

        swipe = intent.getBooleanExtra("addswipe", false)
        if (swipe){
            mRrootLayout = activity_game
            game_grid.setOnTouchListener(this)
        }

        grid = intent.getBooleanExtra("addgrid", false)
        if (grid){
            game_grid.background = resources.getDrawable(R.drawable.game_background_grid, null)
        }

        highScore = intent.getStringExtra("highscore")
        high_score.text = highScore

        start.setOnClickListener(this)

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
                    countdown.text = "PLAY !"
                } else {
                    countdown.animate().translationX(1f).duration = 0
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

    override fun onPause() {
        super.onPause()
        pause.performClick()
    }

    private fun adaptToScreen(){
        // get device dimensions
        val dm = DisplayMetrics()

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(dm)

        val widthInDP = (dm.widthPixels / dm.density).roundToInt()
        val heightInDP = (dm.heightPixels / dm.density).roundToInt()

        if (widthInDP > 390){
            setContentView(R.layout.activity_game)
        } else {
            setContentView(R.layout.activity_game_small)
        }
        when {
            heightInDP > 800 -> {
                title_big_screen.visibility = View.VISIBLE
            }
            heightInDP > 730 -> {
                title_medium_screen.visibility = View.VISIBLE
            }
            heightInDP > 670 -> {
                title_small_screen.visibility = View.VISIBLE
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        when (v) {
            left -> {
                farLeft = false
                while (!farLeft){
                    moveLeft()
                }
            }
            right -> {
                farRight = false
                while (!farRight){
                    moveRight()
                }
            }
            down -> {
                bottom = false
                while (!bottom){
                    moveDown()
                }
            }
        }
        return true
    }

    override fun onClick(v: View?) {
        if (v == start) {

            // initialization
            initializeButtons()
            initializeGrid()
            score = 0
            playing = true

            start.visibility = View.GONE
            gameOver.visibility = View.GONE

            mediaPlayer!!.start()
            startGame()
        }

        if (v == pause){
            if (playing){
                pauseGame()
                mediaPlayer!!.pause()
                pause.visibility = View.GONE
                resume.visibility = View.VISIBLE
                buttonClickability(false)
            }
        }

        if (v == resume){
            resumeGame()
            if(volumeOn.visibility == View.GONE){
                mediaPlayer!!.start()
            }
            pause.visibility = View.VISIBLE
            resume.visibility = View.GONE
            buttonClickability(true)
        }

        if (v == rotate) {
            val rotate = mainBlock.rotation(pieceList)
            if (rotate){
                if (mainBlock.rotation == 3) {
                    mainBlock.rotation = 0
                } else {
                    mainBlock.rotation += 1
                }
                val mAdapter =
                    CubeAdapter(this, pieceList, template)
                gridView!!.adapter = mAdapter
            }
            else {
                return
            }
        }
        if (v == right) {
            moveRight()
        }
        if (v == left) {
            moveLeft()
        }
        if (v == down){
            if (mainBlock.detectBottom(pieceList)){
                val check = mainBlock.checkDown(pieceList)
                if (check) {
                    mainBlock.removeBlock(pieceList)
                    mainBlock.moveDown(pieceList)

                    val mAdapter = CubeAdapter(
                        this,
                        pieceList, template
                    )
                    gridView!!.adapter = mAdapter
                }
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
            val mAdapter = CubeAdapter(this, pieceList, template)
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
            8 -> PieceU(pieceList, 14)
            9 -> PieceM(pieceList, 14)
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

        if (next == 2 || next == 4){
            nextView = nextBlock_4
            nextBlock_4.visibility = View.VISIBLE
            nextBlock_5.visibility = View.GONE
        } else {
            nextView = nextBlock_5
            nextBlock_4.visibility = View.GONE
            nextBlock_5.visibility = View.VISIBLE
        }
        val mNextBlockAdaptater = CubeAdapter(this, nextPiece, template)
        nextView!!.adapter = mNextBlockAdaptater
    }

    private fun moveRight(){
        val check = mainBlock.checkRight(pieceList)
        if (check) {
            mainBlock.removeBlock(pieceList)
            mainBlock.moveRight(pieceList)

            val mAdapter = CubeAdapter(this, pieceList, template)
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
                CubeAdapter(this, pieceList, template)
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
                    CubeAdapter(this, pieceList, template)
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
                    val mAdapter = CubeAdapter(
                        this,
                        pieceList,
                        template
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
        // checking for 4 rows
        val point: Int = if (fullRows == 4){
            800
        } else {
            100 * fullRows
        }
        if (point > 0){
            val scaleUpAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.scale_up_animation)
            points.text = point.toString()
            addpoints.visibility = View.VISIBLE
            addpoints.animate().withStartAction { addpoints.startAnimation(scaleUpAnimation) }.alpha(1f).withEndAction{addpoints.visibility = View.GONE}
        }
        score += point
        totalScore.text = score.toString()
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
        val mAdapter = CubeAdapter(this, pieceList, template)
        gridView!!.adapter = mAdapter
    }

    private fun initializeButtons(){
        left.setOnClickListener(this)
        left.setOnLongClickListener(this)
        right.setOnClickListener(this)
        right.setOnLongClickListener(this)
        down.setOnClickListener(this)
        down.setOnLongClickListener(this)
        rotate.setOnClickListener(this)
        pause.setOnClickListener(this)
        resume.setOnClickListener(this)
        volumeOff.setOnClickListener(this)
        volumeOn.setOnClickListener(this)
    }

    private fun buttonClickability(bool: Boolean){
        left.isEnabled = bool
        right.isEnabled = bool
        down.isEnabled = bool
        rotate.isEnabled = bool
        volumeOff.isEnabled = bool
        volumeOn.isEnabled = bool
    }

    override fun onBackPressed() {
        pause.performClick()
        val dialog = Dialog(this@GameActivity)
        dialog.setContentView(R.layout.alert_dialog)
        dialog.show()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.dialog_cancel.setOnClickListener {
            resume.performClick()
            dialog.dismiss()
        }
        dialog.dialog_ok.setOnClickListener {
            super.onBackPressed()
            dialog.dismiss()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
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

    private fun checkHighScore(score: Int){
        if (score > highScore!!.toInt()){
            highScore = score.toString()
            high_score.text = highScore

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
                    rotate.performClick()
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

    // LEGACY CODE
    private fun integrateSwipe(){
        game_grid.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onClick(x: Int, y: Int) {
                val halfGridWidth = (game_grid.width / 2)
                if (x < halfGridWidth){
                    moveLeft()
                } else {
                    moveRight()
                }
            }
            override fun onSwipeLeft() {
                farLeft = false
                while (!farLeft){
                    moveLeft()
                }
            }
            override fun onSwipeRight() {
                farRight = false
                while (!farRight){
                    moveRight()
                }
            }
            override fun onSwipeDown() {
                bottom = false
                while (!bottom){
                    moveDown()
                }
            }
            override fun onSwipeUp() {
                rotate.performClick()
            }
        })
    }
}


