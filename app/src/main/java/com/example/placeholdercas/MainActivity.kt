package com.example.placeholdercas

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.example.placeholdercas.Common.Companion.currDes
import kotlinx.android.synthetic.main.activity_main.holderET
import kotlinx.android.synthetic.main.activity_main.wooshBT


//TODO MAKE COPY AND PASTE WORK | DONE
//TODO MAKE THE NOTIFICATION | DONE
//TODO MAKE IT WORK WITHOUT THE BACKGROUND SERVICE | DONE
//TODO MAKE THE FOCUS SHIFT AUTOMATICALLY | BYPASSED WITH A BUTTON (KINDA FINE WORKS THO)
//TODO MAKE THE QS COLLAPSE ON CLICK
//TODO MAKE THE FLOATING WINDOW COLLAPSABLE


class MainActivity : AppCompatActivity() {

    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(isServiceRunning()) {
            stopService(Intent(this@MainActivity, FloatingWindowApp::class.java))
        }

        holderET.setText(currDes)
        holderET.setSelection(holderET.text.toString().length)
        holderET.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                 currDes = holderET.text.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        wooshBT.setOnClickListener {
            if(checkOverlayPermission()) {
                startForegroundService(Intent(this@MainActivity, FloatingWindowApp::class.java))
                finish()
            } else {
                requestFloatingWindowPermision()
            }
        }

    }


    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager

        for(service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (FloatingWindowApp::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun requestFloatingWindowPermision() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle("Screen Overlay Permission")
        builder.setMessage("This app needs Screen Overlay Permission to run")
        builder.setPositiveButton("Grant", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, RESULT_OK)
        })
        dialog = builder.create()
        dialog.show()
    }

    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }
}