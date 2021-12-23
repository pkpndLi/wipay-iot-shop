package com.example.wipay_iot_shop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import com.example.wipay_iot_shop.wipay_qr.Adapter
import com.example.wipay_iot_shop.wipay_qr.Event
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder

class QRpaymentActivity : AppCompatActivity() ,Event{
    lateinit var adapter: Adapter
    lateinit var btn_OK_QRcode:Button

    var totalAmount:Int?=null
    var menuName:String?=null
    lateinit var iv_QR : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrpayment)
        this.btn_OK_QRcode = findViewById(R.id.btn_OK_QRcode)
        intent.apply {
            totalAmount = getIntExtra("totalAmount",145)
            menuName = getStringExtra("menuName").toString()
        }

        adapter = Adapter()
        adapter.setProvisioningEvent(this)
        adapter.getToken(totalAmount.toString())

        btn_OK_QRcode.setOnClickListener {
            startActivity(Intent(this,MenuActivity::class.java))
        }
    }

    override fun onGenQRcode(QRcode: String?) {
        iv_QR = findViewById(R.id.iv_QR)
        val writer = MultiFormatWriter()
        try {
            val matrix = writer.encode(QRcode, BarcodeFormat.QR_CODE, 250, 250)
            val encoder = BarcodeEncoder()
            val bitmap = encoder.createBitmap(matrix)
            iv_QR.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

}