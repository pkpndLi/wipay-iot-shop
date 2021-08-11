package com.example.wipay_iot_shop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class PaymentActivity : AppCompatActivity() {

    var totalAmount:Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        intent.apply {
            totalAmount = getIntExtra("totalAmount",145)
        }

        Toast.makeText(applicationContext,"totalAmount" + totalAmount,Toast.LENGTH_LONG).show()




    }
}