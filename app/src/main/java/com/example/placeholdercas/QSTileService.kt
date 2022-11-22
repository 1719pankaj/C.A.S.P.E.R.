package com.example.placeholdercas

import android.content.Intent
import android.service.quicksettings.TileService

class QSTileService : TileService() {

    override fun onClick() {
        super.onClick()
        startForegroundService(Intent(this@QSTileService, FloatingWindowApp::class.java))
    }
}