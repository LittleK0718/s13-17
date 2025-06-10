package com.example.s13_17

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class MainActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // 取得傳過來的資料
        val rabbitWins = intent.getIntExtra("RABBIT_WINS", 0)
        val turtleWins = intent.getIntExtra("TURTLE_WINS", 0)
        val bestof = intent.getIntExtra("BEST_OF", 1)
        val finalWinner = intent.getStringExtra("FINAL_WINNER") ?: "平局"

        // 綁定畫面元件
        val tvMatchResult = findViewById<TextView>(R.id.howtimes)
        val tvFinalScore = findViewById<TextView>(R.id.textView3)
        val ivWinnerImage = findViewById<ImageView>(R.id.imageView4)
        val btnBackToRace = findViewById<Button>(R.id.button)

        // 顯示比賽結果文字
        tvMatchResult.text = "$bestof 場制比賽結果"
        tvFinalScore.text = "結果：兔子贏了 $rabbitWins 場，烏龜贏了 $turtleWins 場"

        // 顯示勝利者圖片
        when (finalWinner) {
            "兔子" -> ivWinnerImage.setImageResource(R.drawable.rabbit)
            "烏龜" -> ivWinnerImage.setImageResource(R.drawable.turtle)
        }

        // 返回按鈕事件
        btnBackToRace.setOnClickListener {
            finish()
        }
    }
}