package com.example.upbitpricenoti

import android.annotation.SuppressLint
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.upbitpricenoti.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {

    private val TIMESTAMP = "timestamp"
    private val HIGH_PRICE = "high_price"
    private val LOW_PRICE = "low_price"

    private lateinit var binding: ActivityMainBinding
    private lateinit var requestQueue: RequestQueue
    private lateinit var ringtone: Ringtone
    private var isRingtoneTurnOn = false
    private var isRingtoneOn = false
    private var ringtoneOffTime = 0L
    private var ringtoneOffTerm = 10 * 60 * 1000
    private var ringtoneUri: Uri? = null

    private val url = "https://api.upbit.com/v1/candles/minutes/1?market=KRW-BTC&count=4"
    private var beforeTime = 0L
    private var beforeHighPrice = BigDecimal("0")
    private var beforeLowPrice = BigDecimal("0")
    private var crntTime = 0L
    private var crntHighPrice = BigDecimal("0")
    private var crntLowPrice = BigDecimal("0")

    //content://media/internal/audio/media/74?title=Bach%20Siciliano&canonical=1
    //content://media/internal/audio/media/78?title=Dvorak%20Songs%20My%20Mother%20Taught%20Me&canonical=1
    //content://media/internal/audio/media/82?title=Mozart%20Sonata&canonical=1
    //content://media/internal/audio/media/83?title=Schumann%20Piano%20Quartet&canonical=1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestQueue = Volley.newRequestQueue(this)
//        ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        ringtone = RingtoneManager.getRingtone(this, Uri.parse("content://media/internal/audio/media/78?title=Dvorak%20Songs%20My%20Mother%20Taught%20Me&canonical=1"))

        binding.btn1.setOnClickListener {
            isRingtoneTurnOn = !isRingtoneTurnOn
            binding.textNotiStatus.text = if (isRingtoneTurnOn) {
                "ALARM ON"
            } else {
                stopAlarmSound()
                "ALARM OFF"
            }
        }
        binding.btn2.setOnClickListener { stopAlarmSound() }
        binding.btn3.setOnClickListener {
//            ringtone = RingtoneManager.getRingtone(this, Uri.parse("content://media/internal/audio/media/78?title=Dvorak%20Songs%20My%20Mother%20Taught%20Me&canonical=1"))
//            ringtone.play()
        }

        Timer().schedule(object : TimerTask() {
            override fun run() {
                getData()
            }
        }, 0, 3000)
    }

    @SuppressLint("SetTextI18n")
    private fun getData() {
        val request = StringRequest(Request.Method.GET, url,
            {
                val jsonArray = JSONArray(it)
                for (i in 0..<jsonArray.length()) {
                    val data = JSONObject(jsonArray.get(i).toString())

                    val time = data.get(TIMESTAMP).toString().toLong()
                    val highPrice = BigDecimal(data.get(HIGH_PRICE).toString())
                    val lowPrice = BigDecimal(data.get(LOW_PRICE).toString())

                    if (i == 0) {
                        beforeTime = time
                        beforeHighPrice = highPrice
                        beforeLowPrice = lowPrice
                        crntTime = time
                        crntHighPrice = highPrice
                        crntLowPrice = lowPrice
                    } else {
                        if (time < beforeTime) {
                            beforeTime = time
                            beforeHighPrice = highPrice
                            beforeLowPrice = lowPrice
                        }
                        if (time > crntTime) {
                            crntTime = time
                            crntHighPrice = highPrice
                            crntLowPrice = lowPrice
                        }
                    }
                }

                Log.d("tmp88", "=================================================================================")
                Log.d("tmp88", "beforeTime : $beforeTime, beforeHighPrice : $beforeHighPrice, beforeLowPrice : $beforeLowPrice")
                Log.d("tmp88", "crntTime : $crntTime, crntHighPrice : $crntHighPrice, crntLowPrice : $crntLowPrice")


                val rise = crntHighPrice.minus(beforeLowPrice)
                val fall = beforeHighPrice.minus(crntLowPrice)
                Log.d("tmp88", "rise : $rise, fall : $fall")

                binding.stdPercent.text = "1% : ${beforeLowPrice.multiply(BigDecimal("0.01"))}"

                val riseNotiStd = beforeLowPrice.multiply(BigDecimal("1.0012"))
                val fallNotiStd = beforeHighPrice.multiply(BigDecimal("0.9988"))
                Log.d("tmp88", "riseNotiStd : $riseNotiStd, fallNotiStd : $fallNotiStd")

                val riseNotiIfPlus = crntHighPrice.minus(riseNotiStd)
                val fallNotiIfMinus = crntLowPrice.minus(fallNotiStd)
                Log.d("tmp88", "riseNotiIfPlus : $riseNotiIfPlus, fallNotiIfMinus : $fallNotiIfMinus")

                binding.stdPrice.text = "riseNotiIfPlus : $riseNotiIfPlus \nfallNotiIfMinus : $fallNotiIfMinus"

                var isNoti = false
                if (riseNotiIfPlus > BigDecimal("0")) {
                    Log.d("tmp88", "PLUS!!!!!!!!!!!!!!!!!!!!!")
                    binding.textNoti.text = "PLUS"
                    playAlarmSound()
                    isNoti = true
                }
                if (fallNotiIfMinus < BigDecimal("0")) {
                    Log.d("tmp88", "!!!!!!!!! MINUS  !!!!!!!!!!!!")
                    binding.textNoti.text = "MINUS"
                    playAlarmSound()
                    isNoti = true
                }
                if (!isNoti) {
                    binding.textNoti.text = "..."
                    stopAlarmSound()
                }
            },
            {
                Log.d("tmp88", "${it.message}")
                binding.textErr.text = it.message
            })
        requestQueue.add(request)
    }

    fun playAlarmSound() {
        val resumeTime = ringtoneOffTime + ringtoneOffTerm
        if (isRingtoneTurnOn && System.currentTimeMillis() > resumeTime) {
            Log.d("tmp87", "playAlarmSound")
            ringtone.play()
            isRingtoneOn = true
            ringtoneOffTime = 0L
        }
    }

    fun stopAlarmSound() {
        ringtone.stop()
        isRingtoneOn = false
        ringtoneOffTime = System.currentTimeMillis()
    }

    fun getRingtoneUris(): List<Uri> {
        val ringtoneUris = mutableListOf<Uri>()
        val ringtoneManager = RingtoneManager(this)
        ringtoneManager.setType(RingtoneManager.TYPE_RINGTONE)

        val cursor = ringtoneManager.cursor
        while (cursor.moveToNext()) {
            val ringtoneUri = ringtoneManager.getRingtoneUri(cursor.position)
            ringtoneUris.add(ringtoneUri)
        }

        return ringtoneUris
    }
}