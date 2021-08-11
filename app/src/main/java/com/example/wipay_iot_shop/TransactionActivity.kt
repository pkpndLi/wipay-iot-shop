package com.example.wipay_iot_shop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class TransactionActivity : AppCompatActivity() {

    var totalAmount:Int? = null
    var cardNO:String = ""
    var cardEXD:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        intent.apply {
            totalAmount = getIntExtra("totalAmount",145)
            cardNO = getStringExtra("cardNO").toString()
            cardEXD = getStringExtra("cardEXD").toString()
        }
    }
}