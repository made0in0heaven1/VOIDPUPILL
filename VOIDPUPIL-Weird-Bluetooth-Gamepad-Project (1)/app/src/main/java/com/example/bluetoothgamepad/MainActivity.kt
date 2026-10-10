package com.example.bluetoothgamepad

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.util.concurrent.Executor

class MainActivity : Activity() {
    private var hid: BluetoothHidDevice? = null
    private var connected: BluetoothDevice? = null
    private var leftX = 0
    private var leftY = 0
    private var rightX = 0
    private var rightY = 0
    private var buttons = 0
    private lateinit var status: TextView
    private val executor = Executor { it.run() }

    // Standard gamepad: 16 buttons followed by X/Y/Rx/Ry axes.
    private val descriptor = byteArrayOf(
        0x05, 0x01,       // Usage Page (Generic Desktop)
        0x09, 0x05,       // Usage (Game Pad)
        0xA1.toByte(), 0x01, // Collection (Application)
        0x05, 0x09,       // Usage Page (Button)
        0x19, 0x01,       // Usage Minimum (Button 1)
        0x29, 0x10,       // Usage Maximum (Button 16)
        0x15, 0x00,       // Logical Minimum (0)
        0x25, 0x01,       // Logical Maximum (1)
        0x95.toByte(), 0x10, // Report Count (16)
        0x75, 0x01,       // Report Size (1)
        0x81.toByte(), 0x02, // Input (Data, Variable, Absolute)
        0x05, 0x01,       // Usage Page (Generic Desktop)
        0x09, 0x30,       // Usage X
        0x09, 0x31,       // Usage Y
        0x09, 0x33,       // Usage Rx
        0x09, 0x34,       // Usage Ry
        0x15, 0x81.toByte(), // Logical Minimum (-127)
        0x25, 0x7F,       // Logical Maximum (127)
        0x75, 0x08.toByte(), // Report Size (8)
        0x95.toByte(), 0x04, // Report Count (4)
        0x81.toByte(), 0x02, // Input (Data, Variable, Absolute)
        0xC0.toByte()        // End Collection
    )

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            runOnUiThread {
                status.text = if (registered) "𖤐 SIGNAL OPEN // PAIR WITH THE PC 𖤐" else "☠ THE VOID REJECTED THIS DEVICE ☠"
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            runOnUiThread {
                if (state == BluetoothProfile.STATE_CONNECTED) {
                    connected = device
                    status.text = "✦ LINK ESTABLISHED // THE PC IS YOURS ✦"
                    Toast.makeText(this@MainActivity, "Gamepad connected", Toast.LENGTH_SHORT).show()
                    sendState()
                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    if (connected == device) connected = null
                    status.text = "☠ LINK SEVERED // SUMMON THE PC AGAIN ☠"
                    sendState()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(13, 4, 24)
        window.navigationBarColor = Color.rgb(13, 4, 24)
        buildUi()
        requestBluetoothPermissions()
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= 31) {
            val missing = arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE)
                .filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
            if (missing.isNotEmpty()) {
                requestPermissions(missing.toTypedArray(), 40)
                return
            }
        }
        startHid()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 40 && grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startHid()
        } else if (requestCode == 40) {
            status.text = "THE DEMON NEEDS BLUETOOTH PERMISSION"
        }
    }

    private fun startHid() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            status.text = "This phone has no Bluetooth adapter"
            return
        }
        if (!adapter.isEnabled) {
            status.text = "TURN ON THE INVISIBLE RADIO, THEN REOPEN"
            return
        }
        status.text = "SUMMONING THE POCKET DEMON…"
        val ok = adapter.getProfileProxy(this, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile != BluetoothProfile.HID_DEVICE) return
                hid = proxy as BluetoothHidDevice
                val sdp = BluetoothHidDeviceAppSdpSettings(
                    "Bluetooth Gamepad", "Phone game controller", "OpenAI sample",
                    BluetoothHidDevice.SUBCLASS1_GAMEPAD, descriptor
                )
                val registered = hid?.registerApp(sdp, null, null, executor, hidCallback) ?: false
                runOnUiThread {
                    status.text = if (registered) "Gamepad registered — open Windows Bluetooth settings and add device" else
                        "Could not register HID. Phone manufacturer may block Bluetooth gamepad mode."
                }
            }
            override fun onServiceDisconnected(profile: Int) {
                hid = null
                connected = null
                runOnUiThread { status.text = "THE INVISIBLE RADIO VANISHED" }
            }
        }, BluetoothProfile.HID_DEVICE)
        if (!ok) status.text = "NO PORTAL HERE. THIS PHONE BLOCKS HID."
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(Color.rgb(13, 4, 24))
        }
        status = TextView(this).apply {
            text = "𖤐 VOIDPUPIL // WAITING FOR THE MACHINE 𖤐"
            textSize = 14f
            setTextColor(Color.rgb(242, 255, 0))
            gravity = Gravity.CENTER
            setPadding(4, 4, 4, 10)
        }
        root.addView(status, LinearLayout.LayoutParams(-1, -2))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val leftColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        leftColumn.addView(label("LEFT STICK"))
        val leftStick = StickView { x, y -> leftX = x; leftY = y; sendState() }
        leftColumn.addView(leftStick, LinearLayout.LayoutParams(0, 0, 1f).apply { width = 0; height = 0 })
        // Give the custom view a useful fixed size in the row.
        leftColumn.addView(leftStick, LinearLayout.LayoutParams(220, 220))
        row.addView(leftColumn, LinearLayout.LayoutParams(0, -1, 1f))

        val center = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        center.addView(label("D-PAD"))
        val dpad = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
        val up = gameButton("▲", 8)
        val mid = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        mid.addView(gameButton("◀", 7))
        mid.addView(gameButton("●", 6))
        mid.addView(gameButton("▶", 5))
        dpad.addView(up)
        dpad.addView(mid)
        dpad.addView(gameButton("▼", 4))
        center.addView(dpad)
        center.addView(gameButton("VOID-L", 4))
        center.addView(gameButton("VOID-R", 5))
        row.addView(center, LinearLayout.LayoutParams(0, -1, 1f))

        val rightColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        rightColumn.addView(label("RIGHT STICK"))
        val rightStick = StickView { x, y -> rightX = x; rightY = y; sendState() }
        rightColumn.addView(rightStick, LinearLayout.LayoutParams(220, 220))
        val face = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
        face.addView(gameButton("☄", 3))
        val faceMid = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        faceMid.addView(gameButton("✹", 2))
        faceMid.addView(gameButton("☠", 1))
        face.addView(faceMid)
        face.addView(gameButton("𖤐", 0))
        rightColumn.addView(face)
        row.addView(rightColumn, LinearLayout.LayoutParams(0, -1, 1f))
        root.addView(row, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun label(s: String) = TextView(this).apply {
        text = s
        textSize = 12f
        setTextColor(Color.LTGRAY)
        gravity = Gravity.CENTER
        setPadding(0, 3, 0, 6)
    }

    private fun gameButton(textValue: String, bit: Int): Button {
        return Button(this).apply {
            text = textValue
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(242, 255, 0))
            setBackgroundColor(Color.rgb(84, 0, 115))
            setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                        buttons = buttons or (1 shl bit); sendState(); true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP -> {
                        buttons = buttons and (1 shl bit).inv(); sendState(); true
                    }
                    else -> true
                }
            }
        }
    }

    private fun sendState() {
        val device = connected ?: return
        val service = hid ?: return
        val report = byteArrayOf(
            (buttons and 0xFF).toByte(),
            ((buttons shr 8) and 0xFF).toByte(),
            leftX.toByte(), leftY.toByte(), rightX.toByte(), rightY.toByte()
        )
        try {
            service.sendReport(device, 0, report)
        } catch (_: SecurityException) {
            runOnUiThread { status.text = "PERMISSION LOST. REOPEN THE PORTAL." }
        }
    }

    override fun onDestroy() {
        try {
            hid?.unregisterApp()
            BluetoothAdapter.getDefaultAdapter()?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hid)
        } catch (_: Exception) {}
        super.onDestroy()
    }

    inner class StickView(private val onAxis: (Int, Int) -> Unit) : View(this@MainActivity) {
        private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        private var cx = 0f
        private var cy = 0f
        private var px = 0f
        private var py = 0f

        override fun onDraw(canvas: android.graphics.Canvas) {
            super.onDraw(canvas)
            val radius = (minOf(width, height) / 2f) - 12f
            cx = width / 2f; cy = height / 2f
            paint.color = Color.rgb(32, 0, 48); paint.style = android.graphics.Paint.Style.FILL
            canvas.drawCircle(cx, cy, radius, paint)
            paint.color = Color.rgb(255, 0, 170); paint.style = android.graphics.Paint.Style.STROKE; paint.strokeWidth = 3f
            canvas.drawCircle(cx, cy, radius * 0.62f, paint)
            paint.color = Color.rgb(0, 255, 190); paint.style = android.graphics.Paint.Style.FILL
            val knobX = if (px == 0f && py == 0f) cx else px
            val knobY = if (px == 0f && py == 0f) cy else py
            canvas.drawCircle(knobX, knobY, radius * 0.28f, paint)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val radius = (minOf(width, height) / 2f) - 12f
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    var dx = event.x - width / 2f
                    var dy = event.y - height / 2f
                    val len = kotlin.math.sqrt(dx * dx + dy * dy)
                    val max = radius * 0.62f
                    if (len > max && len > 0) { dx = dx / len * max; dy = dy / len * max }
                    px = width / 2f + dx; py = height / 2f + dy
                    onAxis((dx / max * 127).toInt().coerceIn(-127, 127),
                        (dy / max * 127).toInt().coerceIn(-127, 127))
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    px = 0f; py = 0f
                    onAxis(0, 0); invalidate(); return true
                }
            }
            return true
        }
    }
}
