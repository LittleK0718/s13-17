package com.example.s13_17

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class RaceResult(
    var winner: String,
    var rabbit: Int,
    var turtle: Int,
    var timestamps: String
)

class MainActivity : AppCompatActivity() {

    private lateinit var spnMatches: Spinner
    private var best0f = 1
    private var rabbitwin = 0
    private var turtlewin = 0

    private lateinit var adapter: RaceResultAdapter
    private val raceResults = mutableListOf<RaceResult>()
    private lateinit var rvHistory: RecyclerView

    // 建立兩個數值，用於計算兔子與烏龜的進度
    private var progressRabbit = 0
    private var progressTurtle = 0

    //速度調整變數
    private lateinit var sbrabbitspeed: SeekBar
    private lateinit var sbturtlespeed: SeekBar
    private lateinit var tvrabbitspeed: TextView
    private lateinit var tvturulespeed: TextView
    private var turtlespeed = 1
    private var rabbitspeed = 3
    private var rabbitSleepProb = 0.66


    // 建立變數以利後續綁定元件
    private lateinit var btnStart: Button
    private lateinit var sbRabbit: SeekBar
    private lateinit var sbTurtle: SeekBar

    //動畫
    private lateinit var ivRabbit: ImageView
    private lateinit var ivTurtle: ImageView
    private var rabbitAnimator: ObjectAnimator? = null
    private var turtleAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //spinner
        spnMatches = findViewById(R.id.spnMatches)
        setupspn()

        //seekbar
        sbrabbitspeed = findViewById(R.id.sbRabbitSpeed)
        sbturtlespeed = findViewById(R.id.sbTurtleSpeed)
        tvrabbitspeed = findViewById(R.id.tvRabbitSpeedValue)
        tvturulespeed = findViewById(R.id.tvTurtleSpeedValue)
        setupSpeedControls()

        //動畫
        ivRabbit = findViewById(R.id.ivRabbit)
        ivTurtle = findViewById(R.id.ivTurtle)


        //
        rvHistory = findViewById(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)
        adapter = RaceResultAdapter(raceResults)
        rvHistory.adapter = adapter

        // 將變數與XML元件綁定
        btnStart = findViewById(R.id.btnStart)
        sbRabbit = findViewById(R.id.sbRabbit)
        sbTurtle = findViewById(R.id.sbTurtle)
        // 對開始按鈕設定監聽器
        btnStart.setOnClickListener {
            setupRaceSeekBar()
            startRace()
        }
    }

    private fun startRace() {
        // 進行賽跑後按鈕不可被操作
        btnStart.isEnabled = false
        // 初始化兔子的賽跑進度
        progressRabbit = 0
        // 初始化烏龜的賽跑進度
        progressTurtle = 0
        // 初始化兔子的SeekBar進度
        sbRabbit.progress = 0
        // 初始化烏龜的SeekBar進度
        sbTurtle.progress = 0

        //重製emoji位置
        resetanimalPosition()
        //開始按鈕動畫
        animaReStartButton()

        Handler(Looper.getMainLooper()).postDelayed({
            //兔子起跑
            runRabbit()
            // 烏龜起跑
            runTurtle()
        }, 1000)
    }

    private fun resetanimalPosition() {
        ivRabbit.translationX = 0f
        ivTurtle.translationX = 0f
        ivRabbit.scaleX = 1f
        ivRabbit.scaleY = 1f
        ivTurtle.scaleX = 1f
        ivTurtle.scaleY = 1f
    }

    private fun animaReStartButton() {
        val scaleX = ObjectAnimator.ofFloat(btnStart, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(btnStart, "scaleY", 1f, 1.2f, 1f)
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.duration = 300
        animatorSet.interpolator = BounceInterpolator()
        animatorSet.start()

    }

    private fun animateRabbit(progress: Int) {
        val maxWidth = resources.displayMetrics.widthPixels - ivRabbit.width
        val targetX = (progress / 100f) * maxWidth

        rabbitAnimator?.cancel()
        rabbitAnimator =
            ObjectAnimator.ofFloat(ivRabbit, "translationX", ivRabbit.translationX, targetX)
        rabbitAnimator?.duration = 200
        rabbitAnimator?.interpolator = AccelerateDecelerateInterpolator()

        val jumpY = ObjectAnimator.ofFloat(ivRabbit, "translationY", 0f, -20f, 0f)
        jumpY.duration = 200
        jumpY.interpolator = AccelerateDecelerateInterpolator()

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(rabbitAnimator, jumpY)
        animatorSet.start()

        if (Math.random() < rabbitSleepProb) {
            Handler(Looper.getMainLooper()).postDelayed({
                animateSleeping()
            }, 300)

        }
    }

    private fun animateSleeping() {
        val sleepAnimator = ObjectAnimator.ofFloat(ivRabbit, "rotation", 0f, -15f, 0f)
        sleepAnimator.duration = 600
        sleepAnimator.repeatCount = 2
        sleepAnimator.start()
    }

    private fun animateTurtle(progress: Int) {
        val maxWidth = resources.displayMetrics.widthPixels - ivTurtle.width
        val targetX = (progress / 100f) * maxWidth

        turtleAnimator?.cancel()
        turtleAnimator = ObjectAnimator.ofFloat(ivTurtle, "translationX", ivTurtle.translationX, targetX)
        turtleAnimator?.duration = 300
        turtleAnimator?.interpolator = AccelerateDecelerateInterpolator()
        turtleAnimator?.start()

        val wiggle=ObjectAnimator.ofFloat(ivTurtle, "rotation", -2f,2f,-2f)
        wiggle.duration=400
        wiggle.start()
    }


    private fun setupspn() {
        val matchOption = arrayOf("1戰1勝", "3戰2勝", "5戰3勝", "7戰4勝")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, matchOption)
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spnMatches.adapter = adapter
        spnMatches.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                best0f = when (position) {
                    0 -> 1
                    1 -> 3
                    2 -> 5
                    3 -> 7
                    else -> 1
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                best0f = 1
            }
        }

    }

    private fun resetWinCount() {
        rabbitwin = 0
        turtlewin = 0
        showToast("以調整至${best0f}")
    }


    private fun setupSpeedControls() {
        //兔子SeekBar顏色
        sbrabbitspeed.progressDrawable.setColorFilter(
            Color.RED,
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        sbrabbitspeed.thumb.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN)
        //烏龜SeekBar顏色
        sbturtlespeed.progressDrawable.setColorFilter(
            Color.GREEN,
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        sbturtlespeed.thumb.setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN)
        //speed
        sbrabbitspeed.max = 9//最大值為10(0~9+1)
        sbrabbitspeed.progress = 2//初始值3
        sbturtlespeed.max = 9//最大值為10(0~9+1)
        sbturtlespeed.progress = 0////初始值1

        rabbitspeed = 3
        turtlespeed = 1
        tvrabbitspeed.text = "$rabbitspeed"
        tvturulespeed.text = "$turtlespeed"


        //Seekbar for rabbit
        sbrabbitspeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                rabbitspeed = p1 + 1
                tvrabbitspeed.text = "$rabbitspeed"
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        sbturtlespeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                turtlespeed = p1 + 1
                tvturulespeed.text = "$turtlespeed"
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
    }

    private fun setupRaceSeekBar() {
        sbRabbit.progressDrawable.setColorFilter(
            Color.RED,
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        sbRabbit.thumb.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN)

        sbTurtle.progressDrawable.setColorFilter(
            Color.GREEN,
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        sbTurtle.thumb.setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN)


    }


    // 建立 showToast 方法顯示Toast訊息
    private fun showToast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    // 建立 Handler 變數接收訊息
    private val handler = Handler(Looper.getMainLooper()) { msg ->
        // 判斷編號，並更新兔子的進度
        if (msg.what == 1) {
            // 更新兔子的進度
            sbRabbit.progress = progressRabbit
            //執行兔子移動
            animateRabbit(progressRabbit)
            // 若兔子抵達，則顯示兔子勝利
            if (progressRabbit >= 100 && progressTurtle < 100) {
                val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
                dateFormat.timeZone = java.util.TimeZone.getTimeZone("Asia/Taipei")
                val currentTime = dateFormat.format(Date())
                recordResult("兔子", progressRabbit, progressTurtle, currentTime)
                btnStart.isEnabled = true
                showToast("兔子獲勝")
                //animateWinner("兔子")
            }

        } else if (msg.what == 2) {
            // 更新烏龜的進度
            sbTurtle.progress = progressTurtle
            //執行兔子移動
            animateTurtle(progressTurtle)
            // 若烏龜抵達，則顯示烏龜勝利
            if (progressTurtle >= 100 && progressRabbit < 100) {
                val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
                dateFormat.timeZone = java.util.TimeZone.getTimeZone("Asia/Taipei")
                val currentTime = dateFormat.format(Date())
                recordResult("烏龜", progressRabbit, progressTurtle, currentTime)
                btnStart.isEnabled = true
                showToast("烏龜獲勝")
                //animateWinner("烏龜")
            }
        }
        true
    }

    // 用 Thread 模擬兔子移動
    private fun runRabbit() {
        Thread {
            // 兔子有三分之二的機率會偷懶
            val sleepProbability = arrayOf(true, true, false)
            while (progressRabbit < 100 && progressTurtle < 100) {
                try {
                    Thread.sleep(100) // 延遲0.1秒更新賽況
                    if (sleepProbability.random())
                        Thread.sleep(300) // 兔子偷懶0.3秒
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                progressRabbit += rabbitspeed // 每次跑三步

                val msg = Message() // 建立Message物件
                msg.what = 1  // 加入編號
                handler.sendMessage(msg) // 傳送兔子的賽況訊息
            }
        }.start() // 啟動 Thread
    }

    // 用 Thread 模擬烏龜移動
    private fun runTurtle() {
        Thread {
            while (progressTurtle < 100 && progressRabbit < 100) {
                try {
                    Thread.sleep(100) // 延遲0.1秒更新賽況
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                progressTurtle += turtlespeed // 每次跑一步

                val msg = Message() // 建立Message物件
                msg.what = 2 // 加入編號
                handler.sendMessage(msg) // 傳送烏龜的賽況訊息
            }
        }.start() // 啟動 Thread
    }


    private fun animateWinner(winner: String) {
        val winnerView = if (winner == "兔子") ivRabbit else
        ivTurtle

        val scaleX = ObjectAnimator.ofFloat(winnerView, "scaleX", 1f, 1.5f, 1.2f)
        val scaleY = ObjectAnimator.ofFloat(winnerView, "scaleY", 1f, 1.5f, 1.2f)
        val rotation = ObjectAnimator.ofFloat(winnerView, "rotation", 0f, 360f)

        val victorySet = AnimatorSet()
        victorySet.playTogether(scaleX, scaleY, rotation)
        victorySet.duration = 1000
        victorySet.interpolator = BounceInterpolator()
        victorySet.start()

        val blinkAnimation = ObjectAnimator.ofFloat(winnerView, "alpha", 1f, 0.3f, 1f)
        blinkAnimation.duration = 300
        blinkAnimation.repeatCount = 5
        blinkAnimation.startDelay = 1000  // 延遲1秒開始
        blinkAnimation.start()
    }



    private fun recordResult(
        winner: String,
        rabbittimes: Int,
        turtletimes: Int,
        timestamp: String
    ) {
        //val sdf = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss", java.util.Locale.getDefault())
        //val timestamp = sdf.format(java.util.Date())

        val result = RaceResult(
            winner = winner,
            rabbit = rabbittimes,
            turtle = turtletimes,
            timestamps = timestamp
        )

        raceResults.add(0, result) // 加在最上面
        adapter.notifyItemInserted(0)
        rvHistory.scrollToPosition(0)
        updateWinCount(winner)
    }

    private fun clearRaceHistory() {
        raceResults.clear()
        adapter.notifyDataSetChanged()
    }


    private fun updateWinCount(winner: String) {
        when (winner) {
            "兔子" -> rabbitwin++
            "烏龜" -> turtlewin++
        }

        var winThreadholds = (best0f / 2) + 1
        showToast("目前比分 - 兔子:$rabbitwin, 烏龜:$turtlewin")

        if (rabbitwin >= winThreadholds || turtlewin >= winThreadholds) {
            val intent = Intent(this, MainActivity2::class.java).apply {
                putExtra("RABBIT_WINS", rabbitwin)
                putExtra("TURTLE_WINS", turtlewin)
                putExtra("BEST_OF", best0f)
                putExtra("FINAL_WINNER", if (rabbitwin > turtlewin) "兔子" else "烏龜")
            }
            startActivity(intent)
            resetWinCount()
            clearRaceHistory()
        }


    }
}
