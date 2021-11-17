package com.example.wipay_iot_shop

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ColorSpace.connect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.annotation.RequiresApi
import android.os.Build
import android.system.Os.connect
import com.example.wipay_iot_shop.helper.AWSIoTEvent
import com.example.wipay_iot_shop.helper.AWSIoTHelper
import com.example.wipay_iot_shop.helper.Certificate
import com.example.wipay_iot_shop.helper.data.CertificateObject
import com.example.wipay_iot_shop.helper.data.RegisterObject
import com.example.wipay_iot_shop.helper.data.SetTView
import java.lang.Exception
import java.lang.reflect.Method


class MenuActivity : AppCompatActivity() {
    var MY_PERMISSIONS_STORAGE = arrayOf(
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.MOUNT_UNMOUNT_FILESYSTEMS",
        "android.permission.READ_PRIVILEGED_PHONE_STATE"
    )
    val REQUEST_EXTERNAL_STORAGE = 1

    private var awsHelper: AWSIoTHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        requestPermission()



        val devID = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        Log.i("testtest", getSerialNumber().toString())
        val listImg = findViewById<ImageView>(R.id.list)
        val goodsImg1 = findViewById<ImageView>(R.id.goods1)
        val goodsImg2 = findViewById<ImageView>(R.id.goods2)
        val goodsImg3 = findViewById<ImageView>(R.id.goods3)

        listImg.setImageDrawable(getImage(this, "list96"))
        goodsImg1.setImageDrawable(getImage(this, "coffee"))
        goodsImg2.setImageDrawable(getImage(this, "coffee1"))
        goodsImg3.setImageDrawable(getImage(this, "brownie"))

        goodsImg1.setOnClickListener{

            val itn =Intent(this,InfoActivity::class.java).apply{
                putExtra("menu","goods1")
                putExtra("amount",145)
            }
            startActivity(itn)
        }

        goodsImg2.setOnClickListener{
            val itn =Intent(this,InfoActivity::class.java).apply{
                putExtra("menu","goods2")
                putExtra("amount",145)
            }
            startActivity(itn)
        }

        goodsImg3.setOnClickListener{
            val itn =Intent(this,InfoActivity::class.java).apply{
                putExtra("menu","goods3")
                putExtra("amount",120)
            }
            startActivity(itn)
        }
        listImg.setOnClickListener {
            startActivity(Intent(this,SettingActivity::class.java))
            awsHelper!!.publish("test","test")

        }


    }

    fun getImage(context: Context, name: String?): Drawable? {
        return context.getResources().getDrawable(
            context.getResources().getIdentifier(name, "drawable", context.getPackageName())
        )
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
        }
    }


    fun getSerialNumber(): String? {
        var serialNumber: String?
        try {
            val c = Class.forName("android.os.SystemProperties")
            val get: Method = c.getMethod("get", String::class.java)
            serialNumber = get.invoke(c, "gsm.sn1").toString()
            if (serialNumber == "") serialNumber = get.invoke(c, "ril.serialnumber").toString()
            if (serialNumber == "") serialNumber = get.invoke(c, "ro.serialno").toString()
            if (serialNumber == "") serialNumber = get.invoke(c, "sys.serialnumber").toString()
            if (serialNumber == "") serialNumber = Build.SERIAL

            // If none of the methods above worked
            if (serialNumber == "") serialNumber = null
        } catch (e: Exception) {
            e.printStackTrace()
            serialNumber = null
        }
        return serialNumber
    }

//    private fun requestPermission() {
//        val checkCallPhonePermission = ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.READ_PHONE_STATE
//        )
//        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(
//                this, arrayOf("android.permission.READ_PHONE_STATE"), 1)
//        } else {
//        }
//    }



}