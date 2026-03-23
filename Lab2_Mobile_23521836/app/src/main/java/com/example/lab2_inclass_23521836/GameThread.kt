package com.example.lab2_inclass_23521836

import android.graphics.Canvas
import android.view.SurfaceHolder

class GameThread(private val surfaceHolder: SurfaceHolder, private val gameView: GameView) : Thread() {

    var running = false
    private val TARGET_FPS = 60L
    private val FRAME_TIME = 1000L / TARGET_FPS

    override fun run() {
        running = true
        var canvas: Canvas?
        while (running) {
            val startTime = System.currentTimeMillis()
            canvas = null
            try {
                canvas = surfaceHolder.lockCanvas()
                synchronized(surfaceHolder) {
                    gameView.update()
                    canvas?.let { gameView.draw(it) }
                }
            } catch (e: Exception) {
                // Ignore
            } finally {
                canvas?.let {
                    try { surfaceHolder.unlockCanvasAndPost(it) }
                    catch (_: Exception) {}
                }
            }
            val elapsed = System.currentTimeMillis() - startTime
            val sleepTime = FRAME_TIME - elapsed
            if (sleepTime > 0) {
                try {
                    sleep(sleepTime)
                } catch (e: InterruptedException) {
                    // Ignore
                }
            }
        }
    }
}