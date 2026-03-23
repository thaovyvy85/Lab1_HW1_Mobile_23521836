package com.example.lab2_inclass_23521836

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.*
import kotlin.random.Random

class GameView(context: Context, attrs: android.util.AttributeSet? = null) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    enum class GameState { MENU, READY, PLAYING, DEAD }

    var gameState = GameState.MENU
    var onScoreChanged: ((Int) -> Unit)? = null
    var onStateChanged: ((GameState) -> Unit)? = null
    var onHighScoreChanged: ((Int) -> Unit)? = null

    private var gameThread: GameThread? = null

    private var W = 0f
    private var H = 0f
    private val GROUND_H = 120f
    private val PIPE_W = 90f
    private val PIPE_GAP = 280f
    private val PIPE_SPEED = 5f
    private val PIPE_INTERVAL = 130

    private var birdX = 0f
    private var birdY = 0f
    private var birdVY = 0f
    private val BIRD_RADIUS = 38f
    private val FLAP_POWER = -16f
    private val GRAVITY = 0.7f
    private val MAX_FALL = 16f
    private var birdAngle = 0f
    private var flapTimer = 0
    private var birdAlive = true
    private var deathTimer = 0

    data class Pipe(val id: Long, var x: Float, val topH: Float, var passed: Boolean = false)
    private val pipes = mutableListOf<Pipe>()
    private var nextPipeFrame = PIPE_INTERVAL

    var score = 0
    var highScore = 0
    private val prefs: SharedPreferences =
        context.getSharedPreferences("FlappyVyPrefs", Context.MODE_PRIVATE)

    data class Particle(
        var x: Float, var y: Float,
        var vx: Float, var vy: Float,
        var life: Float, val decay: Float,
        var radius: Float, val color: Int
    )
    private val particles = mutableListOf<Particle>()

    data class TrailPoint(var x: Float, var y: Float, var life: Float)
    private val trail = mutableListOf<TrailPoint>()

    data class Star(val x: Float, val y: Float, val r: Float, var twinkle: Float, val speed: Float)
    private val stars = mutableListOf<Star>()

    data class Cloud(var x: Float, val y: Float, val w: Float, val speed: Float, val alpha: Float)
    private val clouds = mutableListOf<Cloud>()

    private var groundOffset = 0f
    private var menuAnimFrame = 0f
    private var scoreFlashText = ""
    private var scoreFlashLife = 0f
    private var scoreFlashY = 0f

    private var birdBitmap: Bitmap? = null

    private val skyPaint = Paint()
    private val groundPaint = Paint()
    private val grassPaint = Paint()
    private val pipePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val birdBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val birdEyePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val birdBeakPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val flashPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        holder.addCallback(this)
        highScore = prefs.getInt("highScore", 0)
        flashPaint.apply {
            color = Color.parseColor("#FFD700")
            textSize = 65f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
    }

    override fun surfaceCreated(h: SurfaceHolder) {
        W = width.toFloat(); H = height.toFloat()
        birdX = W * 0.28f; birdY = H / 2f
        initBg(); loadBirdBitmap()
        gameThread = GameThread(holder, this).also { it.start() }
    }

    override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, ht: Int) {
        W = w.toFloat(); H = ht.toFloat()
        birdX = W * 0.28f; initBg()
    }

    override fun surfaceDestroyed(h: SurfaceHolder) {
        gameThread?.running = false; gameThread?.join()
    }

    private fun loadBirdBitmap() {
        try {
            val res = context.resources
            val id = res.getIdentifier("hinhthe", "drawable", context.packageName)
            if (id != 0) {
                val raw = BitmapFactory.decodeResource(res, id)
                val size = (BIRD_RADIUS * 2).toInt()
                val scaled = Bitmap.createScaledBitmap(raw, size, size, true)
                val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val c = Canvas(output)
                val p = Paint(Paint.ANTI_ALIAS_FLAG)
                c.drawCircle(size / 2f, size / 2f, size / 2f, p)
                p.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                c.drawBitmap(scaled, 0f, 0f, p)
                birdBitmap = output
            }
        } catch (_: Exception) {}
    }

    private fun initBg() {
        stars.clear()
        repeat(70) {
            stars.add(Star(Random.nextFloat() * W, Random.nextFloat() * H * 0.75f,
                Random.nextFloat() * 2.5f + 0.5f, Random.nextFloat() * PI.toFloat() * 2,
                Random.nextFloat() * 0.04f + 0.02f))
        }
        clouds.clear()
        repeat(6) {
            clouds.add(Cloud(Random.nextFloat() * W, Random.nextFloat() * H * 0.4f + 40f,
                Random.nextFloat() * 160f + 80f, Random.nextFloat() * 0.5f + 0.2f,
                Random.nextFloat() * 0.12f + 0.04f))
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) { flap(); return true }
        return super.onTouchEvent(event)
    }

    fun flap() {
        when (gameState) {
            GameState.READY -> {
                gameState = GameState.PLAYING
                onStateChanged?.invoke(gameState)
                birdVY = FLAP_POWER; flapTimer = 10; spawnFlapParticles()
            }
            GameState.PLAYING -> if (birdAlive) {
                birdVY = FLAP_POWER; flapTimer = 10; spawnFlapParticles()
            }
            else -> {}
        }
    }

    fun startGame() { resetGame(); gameState = GameState.READY; onStateChanged?.invoke(gameState) }
    fun goToMenu() { resetGame(); gameState = GameState.MENU; onStateChanged?.invoke(gameState) }

    private fun resetGame() {
        birdY = H / 2f; birdVY = 0f; birdAngle = 0f; birdAlive = true; deathTimer = 0
        pipes.clear(); nextPipeFrame = PIPE_INTERVAL; score = 0
        particles.clear(); trail.clear(); groundOffset = 0f; scoreFlashLife = 0f
        onScoreChanged?.invoke(0)
    }

    fun update() {
        menuAnimFrame += 0.04f
        stars.forEach { it.twinkle += it.speed }
        when (gameState) {
            GameState.PLAYING -> updatePlaying()
            GameState.DEAD -> updateDead()
            else -> { birdY = H / 2f + sin(menuAnimFrame) * 28f; birdAngle = sin(menuAnimFrame) * 0.15f }
        }
        if (gameState == GameState.PLAYING) clouds.forEach { c -> c.x -= c.speed; if (c.x + c.w < 0) c.x = W + c.w }
        if (scoreFlashLife > 0) { scoreFlashLife -= 0.05f; scoreFlashY -= 1.2f }
        particles.removeAll { p -> p.x += p.vx; p.y += p.vy; p.vy += 0.2f; p.life -= p.decay; p.life <= 0 }
        trail.removeAll { t -> t.life -= 0.1f; t.life <= 0 }
    }

    private fun updatePlaying() {
        birdVY = min(birdVY + GRAVITY, MAX_FALL); birdY += birdVY
        birdAngle = min(PI.toFloat() / 3.5f, max(-PI.toFloat() / 6f, birdVY * 0.065f))
        if (flapTimer > 0) flapTimer--
        trail.add(TrailPoint(birdX, birdY, 1f)); if (trail.size > 12) trail.removeAt(0)
        nextPipeFrame--
        if (nextPipeFrame <= 0) {
            val minTop = 100f; val maxTop = H - GROUND_H - PIPE_GAP - 100f
            pipes.add(Pipe(System.currentTimeMillis(), W + 20f, Random.nextFloat() * (maxTop - minTop) + minTop))
            nextPipeFrame = PIPE_INTERVAL
        }
        pipes.forEach { it.x -= PIPE_SPEED }
        pipes.removeAll { it.x + PIPE_W + 20 < 0 }
        pipes.forEach { p ->
            if (!p.passed && p.x + PIPE_W < birdX) {
                p.passed = true; score++; onScoreChanged?.invoke(score)
                scoreFlashText = if (score % 5 == 0) "x$score 🎉" else "+1"
                scoreFlashLife = 1f; scoreFlashY = birdY - 60f
            }
        }
        groundOffset = (groundOffset + PIPE_SPEED) % 60f
        if (checkCollision()) triggerDeath()
    }

    private fun updateDead() {
        deathTimer++
        if (deathTimer < 40) { birdVY = min(birdVY + GRAVITY * 2f, MAX_FALL * 2f); birdY += birdVY; birdAngle = min(PI.toFloat() / 1.4f, birdAngle + 0.1f) }
    }

    private fun checkCollision(): Boolean {
        val hr = BIRD_RADIUS - 8f
        if (birdY + hr > H - GROUND_H || birdY - hr < 0) return true
        for (p in pipes) {
            if (birdX + hr > p.x - 12f && birdX - hr < p.x + PIPE_W + 12f)
                if (birdY - hr < p.topH || birdY + hr > p.topH + PIPE_GAP) return true
        }
        return false
    }

    private fun triggerDeath() {
        birdAlive = false; gameState = GameState.DEAD; spawnDeathParticles()
        if (score > highScore) { highScore = score; prefs.edit().putInt("highScore", highScore).apply(); onHighScoreChanged?.invoke(highScore) }
        onStateChanged?.invoke(gameState)
    }

    private fun spawnFlapParticles() {
        repeat(5) {
            particles.add(Particle(birdX - 20f, birdY + (Random.nextFloat() * 20f - 10f),
                -(Random.nextFloat() * 4f + 1f), Random.nextFloat() * 3f - 1.5f,
                0.9f, 0.07f, Random.nextFloat() * 5f + 2f, Color.parseColor("#4ECDC4")))
        }
    }

    private fun spawnDeathParticles() {
        val colors = listOf(0xFFFFD700.toInt(), 0xFFFF6B6B.toInt(), 0xFF4ECDC4.toInt(), 0xFFFFFFFF.toInt())
        repeat(18) { i ->
            val angle = (PI * 2 / 18 * i + Random.nextFloat() * 0.5f).toFloat()
            val spd = Random.nextFloat() * 9f + 3f
            particles.add(Particle(birdX, birdY, cos(angle) * spd, sin(angle) * spd,
                1f, Random.nextFloat() * 0.03f + 0.025f, Random.nextFloat() * 8f + 3f, colors[i % colors.size]))
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawBackground(canvas); pipes.forEach { drawPipe(canvas, it) }
        drawTrail(canvas); drawBird(canvas); drawGround(canvas)
        drawParticles(canvas); if (scoreFlashLife > 0) drawScoreFlash(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        skyPaint.shader = LinearGradient(0f, 0f, 0f, H,
            intArrayOf(Color.parseColor("#0d0d2b"), Color.parseColor("#16213e"),
                Color.parseColor("#0f3460"), Color.parseColor("#1a5276")),
            floatArrayOf(0f, 0.4f, 0.75f, 1f), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, W, H, skyPaint)
        stars.forEach { s ->
            val alpha = (0.5f + 0.5f * sin(s.twinkle)).coerceIn(0f, 1f)
            starPaint.color = Color.argb((alpha * 200).toInt(), 255, 255, 255)
            canvas.drawCircle(s.x, s.y, s.r, starPaint)
        }
        val cp = Paint(Paint.ANTI_ALIAS_FLAG)
        clouds.forEach { c ->
            cp.color = Color.argb((c.alpha * 255).toInt(), 255, 255, 255)
            canvas.drawOval(c.x - c.w/2, c.y - c.w/6, c.x + c.w/2, c.y + c.w/6, cp)
            canvas.drawOval(c.x - c.w*0.3f, c.y - c.w/5, c.x + c.w*0.1f, c.y + c.w/7, cp)
            canvas.drawOval(c.x, c.y - c.w/4.5f, c.x + c.w*0.45f, c.y + c.w/8, cp)
        }
    }

    private fun drawPipe(canvas: Canvas, pipe: Pipe) {
        val capH = 44f; val capExt = 12f; val botY = pipe.topH + PIPE_GAP
        val pShader = LinearGradient(pipe.x, 0f, pipe.x + PIPE_W, 0f,
            intArrayOf(Color.parseColor("#2d6a4f"), Color.parseColor("#52b788"),
                Color.parseColor("#40916c"), Color.parseColor("#1b4332")),
            floatArrayOf(0f, 0.3f, 0.7f, 1f), Shader.TileMode.CLAMP)
        pipePaint.shader = pShader
        canvas.drawRect(pipe.x, 0f, pipe.x + PIPE_W, pipe.topH - capH, pipePaint)
        canvas.drawRect(pipe.x, botY + capH, pipe.x + PIPE_W, H, pipePaint)
        val cShader = LinearGradient(pipe.x - capExt, 0f, pipe.x + PIPE_W + capExt, 0f,
            intArrayOf(Color.parseColor("#1b4332"), Color.parseColor("#74c69d"),
                Color.parseColor("#40916c"), Color.parseColor("#1b4332")),
            floatArrayOf(0f, 0.35f, 0.65f, 1f), Shader.TileMode.CLAMP)
        pipePaint.shader = cShader
        canvas.drawRoundRect(RectF(pipe.x - capExt, pipe.topH - capH, pipe.x + PIPE_W + capExt, pipe.topH), 14f, 14f, pipePaint)
        canvas.drawRoundRect(RectF(pipe.x - capExt, botY, pipe.x + PIPE_W + capExt, botY + capH), 14f, 14f, pipePaint)
        val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#5074c69d") }
        canvas.drawRect(pipe.x + 10f, 4f, pipe.x + 24f, pipe.topH - capH, sp)
        canvas.drawRect(pipe.x + 10f, botY + capH, pipe.x + 24f, H, sp)
    }

    private fun drawTrail(canvas: Canvas) {
        trail.forEachIndexed { i, t ->
            val alpha = (t.life * 0.35f * (i.toFloat() / trail.size)).coerceIn(0f, 1f)
            trailPaint.color = Color.argb((alpha * 255).toInt(), 78, 205, 196)
            canvas.drawCircle(t.x, t.y, (i.toFloat() / trail.size) * BIRD_RADIUS * 0.55f, trailPaint)
        }
    }

    private fun drawBird(canvas: Canvas) {
        canvas.save()
        canvas.translate(birdX, birdY)
        canvas.rotate(Math.toDegrees(birdAngle.toDouble()).toFloat())
        val glowP = Paint(Paint.ANTI_ALIAS_FLAG)
        glowP.shader = RadialGradient(0f, 0f, BIRD_RADIUS * 2f,
            intArrayOf(Color.parseColor("#604ECDC4"), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
        canvas.drawCircle(0f, 0f, BIRD_RADIUS * 2f, glowP)
        if (birdBitmap != null) {
            val clipPath = Path().apply { addCircle(0f, 0f, BIRD_RADIUS, Path.Direction.CW) }
            canvas.clipPath(clipPath)
            canvas.drawBitmap(birdBitmap!!, -BIRD_RADIUS, -BIRD_RADIUS, null)
        } else {
            drawProceduralBird(canvas)
        }
        canvas.restore()
    }

    private fun drawProceduralBird(canvas: Canvas) {
        birdBodyPaint.shader = RadialGradient(-6f, -6f, BIRD_RADIUS,
            intArrayOf(Color.parseColor("#FFE8D6"), Color.parseColor("#FFB7A0"), Color.parseColor("#E07B6A")),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
        canvas.drawCircle(0f, 0f, BIRD_RADIUS, birdBodyPaint)
        val wingY = if (flapTimer > 0) flapTimer * 1.5f - 8f else 8f
        val wP = Paint(Paint.ANTI_ALIAS_FLAG)
        wP.shader = LinearGradient(-6f, wingY - 14f, -6f, wingY + 14f,
            Color.parseColor("#4ECDC4"), Color.parseColor("#26C6DA"), Shader.TileMode.CLAMP)
        canvas.drawOval(-22f, wingY - 14f, 14f, wingY + 14f, wP)
        birdEyePaint.color = Color.WHITE; canvas.drawCircle(14f, -10f, 9f, birdEyePaint)
        birdEyePaint.color = Color.parseColor("#1A1A2E"); canvas.drawCircle(16f, -10f, 5.5f, birdEyePaint)
        birdEyePaint.color = Color.WHITE; canvas.drawCircle(18f, -12f, 2f, birdEyePaint)
        val beak = Path().apply { moveTo(24f, -2f); lineTo(40f, 3f); lineTo(24f, 8f); close() }
        birdBeakPaint.color = Color.parseColor("#FFD700"); canvas.drawPath(beak, birdBeakPaint)
        val blush = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#55FF9678") }
        canvas.drawOval(8f, -2f, 22f, 8f, blush)
    }

    private fun drawGround(canvas: Canvas) {
        val gy = H - GROUND_H
        groundPaint.shader = LinearGradient(0f, gy, 0f, H,
            intArrayOf(Color.parseColor("#4a7c59"), Color.parseColor("#3a6b30"), Color.parseColor("#2d4a1e")),
            floatArrayOf(0f, 0.15f, 1f), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, gy, W, H, groundPaint)
        grassPaint.shader = LinearGradient(0f, gy, 0f, gy + 22f,
            Color.parseColor("#6abf5e"), Color.parseColor("#4a9940"), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, gy, W, gy + 22f, grassPaint)
        val dp = Paint().apply { color = Color.parseColor("#22FFFFFF") }
        var gx = -groundOffset
        while (gx < W) { canvas.drawRect(gx + 10f, gy + 6f, gx + 44f, gy + 14f, dp); gx += 60f }
    }

    private fun drawParticles(canvas: Canvas) {
        particles.forEach { p ->
            particlePaint.color = p.color
            particlePaint.alpha = (p.life * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(p.x, p.y, p.radius, particlePaint)
        }
    }

    private fun drawScoreFlash(canvas: Canvas) {
        flashPaint.alpha = (scoreFlashLife * 255).toInt().coerceIn(0, 255)
        canvas.drawText(scoreFlashText, birdX + 60f, scoreFlashY, flashPaint)
    }
}