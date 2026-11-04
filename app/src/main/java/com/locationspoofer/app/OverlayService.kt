package com.locationspoofer.app

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private val handler = Handler()
    private var touchStartTime = 0L
    private var pointerCount = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        overlayView = Button(this).apply {
            text = "📍"
            textSize = 24f
            setBackgroundColor(0x80000000.toInt()) // خلفية شبه شفافة
            setOnTouchListener { _, event ->
                handleTouch(event)
                true
            }
        }

        val params = WindowManager.LayoutParams(
            150, 150,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        windowManager.addView(overlayView, params)
    }

    private fun handleTouch(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                pointerCount = event.pointerCount
                if (pointerCount >= 3) {
                    touchStartTime = System.currentTimeMillis()
                    handler.postDelayed({
                        if (System.currentTimeMillis() - touchStartTime >= 800 && event.pointerCount >= 3) {
                            LocationEngine.isSpoofingEnabled = !LocationEngine.isSpoofingEnabled
                            overlayView.alpha = if (LocationEngine.isSpoofingEnabled) 1.0f else 0.3f
                        }
                    }, 800)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                handler.removeCallbacksAndMessages(null)
                pointerCount = event.pointerCount
            }
            MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacksAndMessages(null)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        windowManager.removeView(overlayView)
    }
}
