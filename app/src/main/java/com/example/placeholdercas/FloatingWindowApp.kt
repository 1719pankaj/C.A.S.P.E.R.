package com.example.placeholdercas

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.text.ClipboardManager
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import com.example.placeholdercas.Common.Companion.currDes
//import kotlinx.android.synthetic.main.activity_main.* TODO This should allow safe delete of activity_main.xml
import kotlinx.android.synthetic.main.floating_layout.view.*

class FloatingWindowApp : Service() {

    private lateinit var floatView: ViewGroup
    private lateinit var floatWindowLayoutPrams: WindowManager.LayoutParams
    private var LAYOUT_TYPE: Int? = null
    private lateinit var windowManager: WindowManager

    lateinit var btnWeii: ImageView
    lateinit var edtHolder: EditText
    lateinit var btnCopy: ImageView
    lateinit var btnClear: ImageView

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        startForeground()

        val metrics = applicationContext.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatView = inflater.inflate(R.layout.floating_layout, null) as ViewGroup
        btnWeii = floatView.findViewById(R.id.weiiBT)
        edtHolder = floatView.findViewById(R.id.holderET)
        btnCopy = floatView.findViewById(R.id.copyIV)
        btnClear = floatView.findViewById(R.id.clearIV)

        edtHolder.setText(currDes)
        edtHolder.setSelection(edtHolder.text.toString().length)
        edtHolder.isCursorVisible = false

        LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        floatWindowLayoutPrams = WindowManager.LayoutParams(
            (width*0.85f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            LAYOUT_TYPE!!,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        floatWindowLayoutPrams.gravity = Gravity.CENTER
        floatWindowLayoutPrams.x = 0
        floatWindowLayoutPrams.y =0

        windowManager.addView(floatView, floatWindowLayoutPrams)

        btnWeii.setOnClickListener {
            loseFocusAndKeyboard()

        }

        btnWeii.setOnLongClickListener(object : View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {
                stopSelf()
                windowManager.removeView(floatView)

//                val back = Intent( this@FloatingWindowApp, MainActivity::class.java)
//                back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//
//                startActivity(back)
                return false
            }
        })

        edtHolder.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                currDes = edtHolder.text.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        floatView.setOnTouchListener(object : View.OnTouchListener{

            val updatedFloatWindowLayoutPrams = floatWindowLayoutPrams
            var x = 0.0
            var y = 0.0
            var px = 0.0
            var py = 0.0

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when(event!!.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = updatedFloatWindowLayoutPrams.x.toDouble()
                        y = updatedFloatWindowLayoutPrams.y.toDouble()

                        px = event.rawX.toDouble()
                        py = event.rawY.toDouble()
                    }

                    MotionEvent.ACTION_MOVE -> {
                        updatedFloatWindowLayoutPrams.x = (x + (event.rawX - px)).toInt()
                        updatedFloatWindowLayoutPrams.y = (y + (event.rawY - py)).toInt()

                        windowManager.updateViewLayout(floatView, updatedFloatWindowLayoutPrams)
                    }
                }
                return false
            }

        })

        edtHolder.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                edtHolder.isCursorVisible = true
                val updatedFloatingWindowPramsFlag = floatWindowLayoutPrams
                updatedFloatingWindowPramsFlag.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

                windowManager.updateViewLayout(floatView, updatedFloatingWindowPramsFlag)

                return false
            }
        })

        btnCopy.setOnClickListener {
            toClipboard()
            loseFocusAndKeyboard(false)
        }

        btnClear.setOnClickListener {
            edtHolder.text = null
        }

    }

    private fun loseFocusAndKeyboard(keyboardFlag: Boolean = true) {
        edtHolder.isCursorVisible = false
        val updatedFloatingWindowPramsFlag = floatWindowLayoutPrams
        updatedFloatingWindowPramsFlag.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        windowManager.updateViewLayout(floatView, updatedFloatingWindowPramsFlag)
        if(keyboardFlag) {
            val inputMethodManager: InputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(floatView.windowToken, 0)
        }
    }

    private fun toClipboard() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = ClipData.newPlainText("label", currDes)
        clipboard.setPrimaryClip(clip)
//            Toast.makeText(this@FloatingWindowApp, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun startForeground() {


        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId )
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String{
        val channelId = "service"
        val channelName = "Floating Window Service"
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH)
        chan.lightColor = Color.BLUE
        chan.importance = NotificationManager.IMPORTANCE_NONE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        windowManager.removeView(floatView)
    }


}