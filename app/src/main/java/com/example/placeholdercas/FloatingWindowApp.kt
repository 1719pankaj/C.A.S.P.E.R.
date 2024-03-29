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
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.placeholdercas.Common.Companion.currDes
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.floating_layout.view.*


class FloatingWindowApp : Service() {

    private lateinit var floatView: ViewGroup
    private lateinit var floatWindowLayoutPrams: WindowManager.LayoutParams
    private var LAYOUT_TYPE: Int? = null
    private lateinit var windowManager: WindowManager

    private lateinit var database: DatabaseReference

    lateinit var btnWeii: ImageView
    lateinit var edtHolder: EditText
    lateinit var btnCopy: ImageView
    lateinit var btnClear: ImageView
    lateinit var saveBT: ImageView
    lateinit var dropBT: ImageView
    lateinit var cpsBT: ImageView


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

        database = FirebaseDatabase.getInstance("https://casper-64636-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        Firebase.database.setPersistenceEnabled(true)




        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatView = inflater.inflate(R.layout.floating_layout, null) as ViewGroup
        btnWeii = floatView.findViewById(R.id.weiiBT)

        edtHolder = floatView.findViewById(R.id.holderET)
        edtHolder.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        edtHolder.isSingleLine = false
        edtHolder.maxHeight = 500

        btnCopy = floatView.findViewById(R.id.copyIV)
        btnClear = floatView.findViewById(R.id.listDeleteIV)

        edtHolder.setText(currDes)
        edtHolder.setSelection(edtHolder.text.toString().length)
        edtHolder.isCursorVisible = false

        saveBT = floatView.findViewById(R.id.saveBT)
        dropBT = floatView.findViewById(R.id.dropBT)
        cpsBT = floatView.findViewById(R.id.cpsBT)



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

        saveBT.setOnClickListener {
            if(currDes == "") {
                val toast = Toast.makeText(applicationContext, "No Text", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP or Gravity.START, 0, 0)
                toast.show()
            } else {
                currDes?.let { it1 -> database.child("clipboard").child(if (it1.length >= 20) it1.substring(0, 20) else it1).setValue(currDes) }
                val toast = Toast.makeText(applicationContext, "Saved", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP or Gravity.START, 0, 0)
                toast.show()
                val it1 = currDes
            }
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


        val channelId = createNotificationChannel()

        val notificationBuilder = NotificationCompat.Builder(this, channelId )
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
    }


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