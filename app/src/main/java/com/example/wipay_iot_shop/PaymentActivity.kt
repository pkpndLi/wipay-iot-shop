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
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wipay_iot_shop.emv.EmvEvent
import com.example.wipay_iot_shop.emv.EmvThread
import com.example.wipay_iot_shop.emv.EmvThread.TYPE_TEST_EMV
import com.example.wipay_iot_shop.emv.McrEvent
import com.example.wipay_iot_shop.emv.McrThread
import com.example.wipay_iot_shop.emv.data.DataEmv
import com.example.wipay_iot_shop.emv.data.DataMcr
import com.example.wipay_iot_shop.printer.Printer
import vpos.apipackage.ByteUtil
import vpos.apipackage.PosApiHelper
import vpos.apipackage.Print
import vpos.apipackage.StringUtil
import vpos.keypad.EMVCOHelper
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class PaymentActivity : AppCompatActivity() ,View.OnClickListener,EmvEvent,McrEvent{

    lateinit var btn_SelectMag : Button
    lateinit var btn_SelectEMV : Button
    lateinit var btn_QR : Button
    lateinit var btn_SelectOK : Button
    lateinit var tv_InfoPayment : TextView
    var status:Int? = null
    var totalAmount:Int? = null
    var menuName: String = ""
    var mcrThread: McrThread? = null
    private var emvThread: EmvThread? = null
    var printer: Printer? =null
    var isOpen = false


    var MY_PERMISSIONS_STORAGE = arrayOf(
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"
    )
    val REQUEST_EXTERNAL_STORAGE = 1

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
        btn_SelectMag.setOnClickListener(this)
        btn_SelectEMV.setOnClickListener(this)
        btn_QR.setOnClickListener(this)
        btn_SelectOK.setOnClickListener(this)
    }
    override fun onClick(view: View?) {
        when(view?.id){
            R.id.btn_SelectMag->{
                try {
                    btn_SelectMag.isEnabled = false
                    btn_SelectEMV.isEnabled = true
                    btn_QR.isEnabled = true

                    Toast.makeText(applicationContext,"Click pay and swipe card",Toast.LENGTH_LONG).show()
                    status = 3
                }catch (e: Exception){
                }
            }
//

            R.id.btn_SelectEMV->{
                try {
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

                    btn_QR.isEnabled = false
                    btn_SelectEMV.isEnabled = true
                    btn_SelectMag.isEnabled = true
                    status = 2
                }catch (e: Exception){

                }

            }
            R.id.btn_SelectOK->{
                if (status != null) {
                    when (status) {
                        1 -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermission()
                            } else {
                                testEmv()
                            }
                        }
                        2 -> {
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

    private fun requestPermission() {
        //检测是否有写的权限
        //Check if there is write permission
        val checkCallPhonePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            // 没有写文件的权限，去申请读写文件的权限，系统会弹出权限许可对话框
            //Without the permission to Write, to apply for the permission to Read and Write, the system will pop up the permission dialog
            ActivityCompat.requestPermissions(
                this, MY_PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
        } else {
            testEmv()
        }
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
            }
            startActivity(itn)
        }
    }
}


