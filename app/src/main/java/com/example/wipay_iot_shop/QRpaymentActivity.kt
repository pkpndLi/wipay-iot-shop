package com.example.wipay_iot_shop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder

class QRpaymentActivity : AppCompatActivity() {
    lateinit var iv_QR : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrpayment)

        iv_QR = findViewById(R.id.iv_QR)
        val writer = MultiFormatWriter()
        try {
            val matrix = writer.encode("00020101021129370016A000000677010111011300669102509515802TH53037646304BD9A", BarcodeFormat.QR_CODE, 250, 250)
            val encoder = BarcodeEncoder()
            val bitmap = encoder.createBitmap(matrix)
            iv_QR.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }


}