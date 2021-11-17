package com.example.wipay_iot_shop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.example.wipay_iot_shop.helper.AWSIoTEvent
import com.example.wipay_iot_shop.helper.AWSIoTHelper
import com.example.wipay_iot_shop.helper.Certificate
import com.example.wipay_iot_shop.helper.data.CertificateObject
import com.example.wipay_iot_shop.helper.data.RegisterObject
import com.example.wipay_iot_shop.helper.data.SetTView

class SettingActivity : AppCompatActivity(), AWSIoTEvent {
    private var awsHelper: AWSIoTHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        awsconnect()

        val merchantList = findViewById<ListView>(R.id.merchantList)

        val Items = ArrayList<Model>()
//        Items.add(Model("Merchant Location","-",R.drawable.location64))
//        Items.add(Model("Merchant ID","222222222222222",R.drawable.shop64))
//        Items.add(Model("Terminal ID","22222222",R.drawable.terminal64))
        Items.add(Model("manual config","",R.drawable.config))
        Items.add(Model("Load config ","",R.drawable.loadconfig))
        Items.add(Model("Report sale","",R.drawable.summary))





        val adapter = SettingAdapter(this,R.layout.merchantlist,Items)
        merchantList.adapter = adapter

        merchantList.setOnItemClickListener { adapter, View, i, l->
            when(i){
                0->{
                    startActivity(Intent(this,ConfigActivity::class.java))
                }
                1->{
//                    $aws/things/POS_config/shadow/name/POS_CS10_config/get/accepted

//                    mqtt!!.publishString("","\$aws/things/POS_config/shadow/name/POS_CS10_config/get",AWSIotMqttQos.QOS0)
                }
                2->{
                    startActivity(Intent(this,SettlementActivity::class.java))
                }

            }
        }
    }

    fun awsconnect(){

        val keyStorePath = filesDir.path
        // init aws helper
        awsHelper = AWSIoTHelper(
            "a33gna0t4ob4fa-ats.iot.ap-southeast-1.amazonaws.com",
            keyStorePath,
            "keystore",
            "POS_CS10_config"
        )

        awsHelper!!.setProvisioningEvent(this)
        awsHelper!!.saveKeyAndCertificate(
            Certificate.KEY_CLAIM,
            Certificate.CERT_CLAIM,
            "CA_ID",
            "12345678"
        )

        awsHelper!!.connect("CA_ID", "12345678")


    }


    override fun onGetPermanentCertificate(certificateObject: CertificateObject?) {

    }

    override fun onRegisterComplete(registerObject: RegisterObject?) {

    }

    override fun onSetTextView(setTView: SetTView?) {

        Log.i("TESTTEST",setTView.toString())
    }

    override fun onStatusConnection(Showmsg: String?) {
        Log.i("TESTTEST","Status :"+Showmsg)
        if(Showmsg == "connected"){
            awsHelper!!.subscribe("test")
        }
    }
}