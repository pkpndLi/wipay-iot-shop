package com.example.wipay_iot_shop

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wipay_iot_shop.readcard.EmvEvent
import com.example.wipay_iot_shop.readcard.EmvThread
import com.example.wipay_iot_shop.readcard.EmvThread.TYPE_TEST_EMV
import com.example.wipay_iot_shop.readcard.McrEvent
import com.example.wipay_iot_shop.readcard.McrThread
import com.example.wipay_iot_shop.readcard.data.DataEmv
import com.example.wipay_iot_shop.readcard.data.DataMcr
import com.example.wipay_iot_shop.printer.Printer

class PaymentActivity : AppCompatActivity() ,View.OnClickListener,EmvEvent,McrEvent{

    lateinit var btn_SelectMag : Button
    lateinit var btn_SelectEMV : Button
    lateinit var btn_QR : Button
    lateinit var btn_SelectOK : Button
    lateinit var btn_QR_crypto : Button
    lateinit var tv_InfoPayment : TextView
    var status:Int? = null
    var totalAmount:Int? = null
    var menuName: String = ""
    var mcrThread: McrThread? = null
    private var emvThread: EmvThread? = null
    var printer: Printer? =null
    var isOpen = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)


            intent.apply {
                totalAmount = getIntExtra("totalAmount",145)
                menuName = getStringExtra("menuName").toString()
            }

//            Toast.makeText(applicationContext,"totalAmount" + totalAmount,Toast.LENGTH_LONG).show()

            setView()


    }
    fun setView(){
        //textview
        tv_InfoPayment = findViewById(R.id.tv_InfoPayment)
        //buttom
        btn_SelectMag = findViewById(R.id.btn_SelectMag)
        btn_SelectEMV = findViewById(R.id.btn_SelectEMV)
        btn_QR = findViewById(R.id.btn_QR)
        btn_SelectOK = findViewById(R.id.btn_SelectOK)
        btn_QR_crypto = findViewById(R.id.btn_QR_crypto)
        btn_SelectMag.setOnClickListener(this)
        btn_SelectEMV.setOnClickListener(this)
        btn_QR.setOnClickListener(this)
        btn_SelectOK.setOnClickListener(this)
        btn_QR_crypto.setOnClickListener(this)
    }
    override fun onClick(view: View?) {
        when(view?.id){
            R.id.btn_SelectMag->{
                try {
                    btn_SelectMag.isEnabled = false
                    btn_SelectEMV.isEnabled = true
                    btn_QR.isEnabled = true
                    btn_QR_crypto.isEnabled = true
                    Toast.makeText(applicationContext,"Click pay and swipe card",Toast.LENGTH_LONG).show()
                    status = 3
                }catch (e: Exception){
                }
            }
//

            R.id.btn_SelectEMV->{
                try {
                    btn_QR_crypto.isEnabled = true
                    btn_SelectEMV.isEnabled = false
                    btn_QR.isEnabled = true
                    btn_SelectMag.isEnabled = true
                    Toast.makeText(applicationContext,"Contact card and click pay  ",Toast.LENGTH_LONG).show()
                    status = 1
                }catch (e: Exception){

                }

            }
            R.id.btn_QR->{
                try {
                    btn_QR_crypto.isEnabled = true
                    btn_QR.isEnabled = false
                    btn_SelectEMV.isEnabled = true
                    btn_SelectMag.isEnabled = true
                    status = 2
                    Toast.makeText(applicationContext,"click pay and scan QR ",Toast.LENGTH_LONG).show()
                }catch (e: Exception){
                }

            }
            R.id.btn_QR_crypto->{
                try {
                    btn_QR_crypto.isEnabled = false
                    btn_QR.isEnabled = true
                    btn_SelectEMV.isEnabled = true
                    btn_SelectMag.isEnabled = true
                    status = 4
                    Toast.makeText(applicationContext,"click pay and scan QR ",Toast.LENGTH_LONG).show()
                }catch (e: Exception){

                }

            }
            R.id.btn_SelectOK->{
                if (status != null) {
                    when (status) {
                        1 -> {
                            testEmv()
                        }
                        2 -> {
                            startActivity(Intent(this,QRpaymentActivity::class.java))
//                            val itn =Intent(this,QRpaymentActivity::class.java).apply{
//                                putExtra("processing",true)
//                            }
//                            startActivity(itn)
                        }
                        3->{
                            try {
                                mcrThread = McrThread()
                                mcrThread!!.setMcrEvent(this)
                                mcrThread?.mrcRead()

                            }catch (e: Exception){

                            }
                        }
                    }
                }
            }
        }
    }

    private fun testEmv() {
        if (emvThread != null && !emvThread!!.isThreadFinished()) {
            Log.e(TAG, "Thread is still running...")
        }
        emvThread = EmvThread(TYPE_TEST_EMV,this,totalAmount!!)
        emvThread!!.setEmvEvent(this)
        emvThread!!.start()
    }

    private val DISABLE_FUNCTION_LAUNCH_ACTION = "android.intent.action.DISABLE_FUNCTION_LAUNCH"

    // disable the power key when the device is boot from alarm but not ipo boot
    private fun disableFunctionLaunch(state: Boolean) {
        val disablePowerKeyIntent = Intent(DISABLE_FUNCTION_LAUNCH_ACTION)
        if (state) {
            disablePowerKeyIntent.putExtra("state", true)
        } else {
            disablePowerKeyIntent.putExtra("state", false)
        }
        sendBroadcast(disablePowerKeyIntent)
    }

    override fun onGetDataCard(dataEmv: DataEmv?) {
        runOnUiThread{
            val itn =Intent(this,TransactionActivity::class.java).apply{
                putExtra("processing",true)
                putExtra("cardNO",dataEmv!!.cardNO)
                putExtra("cardEXD",dataEmv!!.cardEXD)
                putExtra("DE55",dataEmv!!.cardDE55)
                putExtra("totalAmount",totalAmount)
                putExtra("menuName",menuName)
            }
        startActivity(itn)
        }

    }

    override fun onGetDataCardMagnetic(dataMag: DataMcr?) {
        runOnUiThread{
            val itn =Intent(this,TransactionActivity::class.java).apply{
                putExtra("processing",true)
                putExtra("cardNO",dataMag!!.cardNO)
                putExtra("cardEXD",dataMag!!.cardEXD)
                putExtra("totalAmount",totalAmount)
                putExtra("menuName",menuName)
                putExtra("DE55","")
            }
            startActivity(itn)
        }
    }
}


