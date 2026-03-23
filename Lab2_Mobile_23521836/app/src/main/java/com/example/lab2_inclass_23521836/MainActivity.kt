package com.example.lab2_inclass_23521836

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var menuScreen: View
    private lateinit var tvMenuHighScore: TextView
    private lateinit var btnPlay: Button
    private lateinit var readyScreen: View
    private lateinit var hud: View
    private lateinit var tvScore: TextView
    private lateinit var tvHudBest: TextView
    private lateinit var gameOverScreen: View
    private lateinit var tvGoScore: TextView
    private lateinit var tvGoBest: TextView
    private lateinit var tvMedal: TextView
    private lateinit var tvNewBest: TextView
    private lateinit var btnRestart: Button
    private lateinit var btnMenu: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        setContentView(R.layout.activity_main)

        gameView           = findViewById(R.id.gameView)
        menuScreen         = findViewById(R.id.menuScreen)
        tvMenuHighScore    = findViewById(R.id.tvMenuHighScore)
        btnPlay            = findViewById(R.id.btnPlay)
        readyScreen        = findViewById(R.id.readyScreen)
        hud                = findViewById(R.id.hud)
        tvScore            = findViewById(R.id.tvScore)
        tvHudBest          = findViewById(R.id.tvHudBest)
        gameOverScreen     = findViewById(R.id.gameOverScreen)
        tvGoScore          = findViewById(R.id.tvGoScore)
        tvGoBest           = findViewById(R.id.tvGoBest)
        tvMedal            = findViewById(R.id.tvMedal)
        tvNewBest          = findViewById(R.id.tvNewBest)
        btnRestart         = findViewById(R.id.btnRestart)
        btnMenu            = findViewById(R.id.btnMenu)

        tvMenuHighScore.text = gameView.highScore.toString()
        tvHudBest.text = gameView.highScore.toString()

        gameView.onScoreChanged = { runOnUiThread { tvScore.text = it.toString() } }
        gameView.onHighScoreChanged = { runOnUiThread { tvHudBest.text = it.toString(); tvMenuHighScore.text = it.toString() } }
        gameView.onStateChanged = { state ->
            runOnUiThread {
                when (state) {
                    GameView.GameState.MENU    -> showMenuScreen()
                    GameView.GameState.READY   -> showReadyScreen()
                    GameView.GameState.PLAYING -> showHud()
                    GameView.GameState.DEAD    -> hud.postDelayed({ showGameOver() }, 800)
                }
            }
        }

        btnPlay.setOnClickListener    { bounce(it) { gameView.startGame() } }
        btnRestart.setOnClickListener { bounce(it) { hideScreen(gameOverScreen) { gameView.startGame() } } }
        btnMenu.setOnClickListener    { bounce(it) { hideScreen(gameOverScreen) { gameView.goToMenu() } } }
        readyScreen.setOnClickListener { gameView.flap() }

        showMenuScreen()
    }

    private fun showMenuScreen() {
        hideAll()
        tvMenuHighScore.text = gameView.highScore.toString()
        menuScreen.visibility = View.VISIBLE
        menuScreen.alpha = 0f
        menuScreen.animate().alpha(1f).setDuration(350).start()
        menuScreen.findViewById<View>(R.id.ivMenuBird)?.let { floatBird(it) }
    }

    private fun showReadyScreen() {
        hideAll()
        hud.visibility = View.VISIBLE; hud.alpha = 0f
        hud.animate().alpha(1f).setDuration(200).start()
        tvScore.text = "0"
        readyScreen.visibility = View.VISIBLE; readyScreen.alpha = 0f
        readyScreen.scaleX = 0.85f; readyScreen.scaleY = 0.85f
        readyScreen.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start()
    }

    private fun showHud() { readyScreen.visibility = View.GONE; hud.visibility = View.VISIBLE }

    private fun showGameOver() {
        val s = gameView.score; val b = gameView.highScore
        tvGoScore.text = s.toString(); tvGoBest.text = b.toString()
        tvMedal.text = when { s >= 25 -> "👑"; s >= 15 -> "🏆"; s >= 10 -> "🥇"; s >= 5 -> "🥈"; s >= 2 -> "🥉"; else -> "💀" }
        tvNewBest.visibility = if (s >= b && s > 0) View.VISIBLE else View.GONE
        gameOverScreen.visibility = View.VISIBLE; gameOverScreen.alpha = 0f
        val panel = gameOverScreen.findViewById<View>(R.id.goPanel)
        panel.scaleX = 0.5f; panel.scaleY = 0.5f; panel.translationY = 80f
        gameOverScreen.animate().alpha(1f).setDuration(250).start()
        panel.animate().scaleX(1f).scaleY(1f).translationY(0f).setDuration(450).start()
    }

    private fun hideAll() {
        listOf(menuScreen, readyScreen, hud, gameOverScreen).forEach { it.visibility = View.GONE }
    }

    private fun hideScreen(v: View, after: () -> Unit) {
        v.animate().alpha(0f).scaleX(0.85f).scaleY(0.85f).setDuration(200)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(a: Animator) {
                    v.visibility = View.GONE; v.alpha = 1f; v.scaleX = 1f; v.scaleY = 1f; after()
                }
            }).start()
    }

    private fun bounce(v: View, action: () -> Unit) {
        v.animate().scaleX(0.88f).scaleY(0.88f).setDuration(80)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(a: Animator) {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(a: Animator) { action() }
                        }).start()
                }
            }).start()
    }

    private fun floatBird(v: View) {
        v.animate().translationY(-30f).setDuration(900)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(a: Animator) {
                    v.animate().translationY(0f).setDuration(900)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(a: Animator) {
                                if (gameView.gameState == GameView.GameState.MENU) floatBird(v)
                            }
                        }).start()
                }
            }).start()
    }
}