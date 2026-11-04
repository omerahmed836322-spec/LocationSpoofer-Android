package com.locationspoofer.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStartOverlay = findViewById<Button>(R.id.btnStartOverlay)
        val btnToggleSpoof = findViewById<Button>(R.id.btnToggleSpoof)
        val etLat = findViewById<EditText>(R.id.etLat)
        val etLon = findViewById<EditText>(R.id.etLon)

        btnStartOverlay.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    startActivity(intent)
                    return@setOnClickListener
                }
            }
            startService(Intent(this, OverlayService::class.java))
            Toast.makeText(this, "تم تشغيل الزر العائم. اضغط بـ 3 أصابع للتفعيل", Toast.LENGTH_LONG).show()
        }

        btnToggleSpoof.setOnClickListener {
            LocationEngine.targetLat = etLat.text.toString().toDoubleOrNull() ?: 24.7136
            LocationEngine.targetLon = etLon.text.toString().toDoubleOrNull() ?: 46.6753
            LocationEngine.isSpoofingEnabled = !LocationEngine.isSpoofingEnabled
            
            val status = if (LocationEngine.isSpoofingEnabled) "مفعل ✅" else "معطل ❌"
            Toast.makeText(this, "حالة التزييف: $status", Toast.LENGTH_SHORT).show()
        }
    }
}
