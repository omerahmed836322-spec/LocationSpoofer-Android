package com.locationspoofer.app

import android.app.Application
import android.content.Context
import android.graphics.PixelFormat
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // تجاهل حزم النظام والموديل نفسه
        if (lpparam.packageName == "android" || lpparam.packageName == "com.locationspoofer.app") return

        // 1. اعتراض getLastKnownLocation
        XposedHelpers.findAndHookMethod(
            LocationManager::class.java.name, lpparam.classLoader,
            "getLastKnownLocation", String::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (LocationEngine.isSpoofingEnabled) {
                        param.result = LocationEngine.generateSpoofedLocation()
                    }
                }
            }
        )

        // 2. اعتراض requestLocationUpdates
        XposedHelpers.findAndHookMethod(
            LocationManager::class.java.name, lpparam.classLoader,
            "requestLocationUpdates", String::class.java, Long::class.java, 
            Float::class.java, LocationListener::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (LocationEngine.isSpoofingEnabled && param.args[3] is LocationListener) {
                        val listener = param.args[3] as LocationListener
                        val spoofedLocation = LocationEngine.generateSpoofedLocation()
                        Handler(Looper.getMainLooper()).post {
                            listener.onLocationChanged(spoofedLocation)
                        }
                    }
                }
            }
        )

        // 3. إنشاء الزر العائم تلقائياً عند بدء التطبيق
        XposedHelpers.findAndHookMethod(
            Application::class.java.name, lpparam.classLoader,
            "onCreate",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val context = param.thisObject as Context
                    Handler(Looper.getMainLooper()).post {
                        createFloatingButton(context)
                    }
                }
            }
        )
    }

    private fun createFloatingButton(context: Context) {
        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            
            val floatingButton = Button(context).apply {
                text = "📍"
                textSize = 20f
                setBackgroundColor(0xCC000000.toInt())
                
                // الضغط العادي لتفعيل/إيقاف التزييف
                setOnClickListener {
                    LocationEngine.isSpoofingEnabled = !LocationEngine.isSpoofingEnabled
                    alpha = if (LocationEngine.isSpoofingEnabled) 1.0f else 0.5f
                }
                
                // السحب
                var initialX = 0
                var initialY = 0
                var initialTouchX = 0f
                var initialTouchY = 0f
                
                setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = (layoutParams as WindowManager.LayoutParams).x
                            initialY = (layoutParams as WindowManager.LayoutParams).y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val params = layoutParams as WindowManager.LayoutParams
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager.updateViewLayout(this, params)
                            true
                        }
                        else -> false
                    }
                }
            }

            val params = WindowManager.LayoutParams(
                120, 120,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 50
                y = 200
            }

            windowManager.addView(floatingButton, params)
        } catch (e: Exception) {
            // تجاهل الأخطاء بصمت لتجنب إزعاج التطبيق
        }
    }
}
