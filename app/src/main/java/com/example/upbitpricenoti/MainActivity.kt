package com.example.upbitpricenoti

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

class MainActivity : AppCompatActivity() {

    private val TIMESTAMP = "timestamp"
    private val HIGH_PRICE = "high_price"
    private val LOW_PRICE = "low_price"

    private lateinit var binding: ActivityMainBinding
    private lateinit var requestQueue: RequestQueue

    private val url = "https://api.upbit.com/v1/candles/minutes/1?market=KRW-BTC&count=4"
    private var beforeTime = 0L
    private var beforeHighPrice = BigDecimal("0")
    private var beforeLowPrice = BigDecimal("0")
    private var crntTime = 0L
    private var crntHighPrice = BigDecimal("0")
    private var crntLowPrice = BigDecimal("0")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestQueue = Volley.newRequestQueue(this)

        binding.test.setOnClickListener {
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

                    Log.d("tmp88", "beforeTime : $beforeTime, beforeHighPrice : $beforeHighPrice, beforeLowPrice : $beforeLowPrice")
                    Log.d("tmp88", "crntTime : $crntTime, crntHighPrice : $crntHighPrice, crntLowPrice : $crntLowPrice")
                },
                {
                    Log.d("tmp88", "${it.message}")
                })
//            request.
            requestQueue.add(request)
        }


    }
}