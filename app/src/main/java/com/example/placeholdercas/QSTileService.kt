package com.example.placeholdercas

import android.app.AlertDialog
import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.TileService
import android.widget.Toast

class QSTileService : TileService() {

    private lateinit var dialog: AlertDialog

    override fun onClick() {
        super.onClick()

        if(checkOverlayPermission()) {
            startForegroundService(Intent(this@QSTileService, FloatingWindowApp::class.java))
        } else {
            Toast.makeText(this, "Grant overlay permission from settings", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun requestFloatingWindowPermision() {
//        val builder = AlertDialog.Builder(this)
//        builder.setCancelable(true)
//        builder.setTitle("Screen Overlay Permission")
//        builder.setMessage("This app needs Screen Overlay Permission to run")
//        builder.setPositiveButton("Grant", DialogInterface.OnClickListener { dialog, which ->
//            dialog.dismiss()
//            val intent = Intent(
//                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                Uri.parse("package:$packageName")
//            )
//            someActivityResultLauncher.launch(intent)
//        })
//        dialog = builder.create()
//        dialog.show()
//    }

    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }
}