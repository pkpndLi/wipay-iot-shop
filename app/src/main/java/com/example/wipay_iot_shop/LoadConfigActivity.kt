package com.example.wipay_iot_shop

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.wipay_iot_shop.helper.AWSIoTEvent
import com.example.wipay_iot_shop.helper.AWSIoTHelper
import com.example.wipay_iot_shop.helper.Certificate
import com.example.wipay_iot_shop.helper.data.CertificateObject
import com.example.wipay_iot_shop.helper.data.RegisterObject
import com.google.gson.Gson
import vpos.apipackage.PosApiHelper
import vpos.apipackage.StringUtil
import vpos.keypad.EMVCOHelper

class LoadConfigActivity : AppCompatActivity(), AWSIoTEvent {
    private var awsHelper: AWSIoTHelper? = null

    lateinit var btn_addaid:Button
    lateinit var btn_addcapk:Button
    lateinit var btn_addterm:Button
    lateinit var btn_clearaid:Button
    lateinit var btn_clearcapk:Button
    lateinit var btn_loadconfig:Button
    lateinit var tv_loadconfig:TextView

    var view : String? = null
    var AID : String? = null
    var TERM : String? = null
    var CAPK : String? = null

    var aid = byteArrayOf()
    var term = byteArrayOf()
    var capk = byteArrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_config)

        this.btn_addaid=findViewById(R.id.btn_addaid)
        this.btn_addcapk=findViewById(R.id.btn_addcapk)
        this.btn_addterm=findViewById(R.id.btn_addterm)
        this.btn_clearaid=findViewById(R.id.btn_clearaid)
        this.btn_clearcapk=findViewById(R.id.btn_clearcapk)
        this.btn_loadconfig=findViewById(R.id.btn_loadconfig)
        tv_loadconfig = findViewById(R.id.tv_loadconfig)

        btn_clearcapk.isEnabled = false
        btn_clearaid.isEnabled = false
        btn_addterm.isEnabled = false
        btn_addcapk.isEnabled = false
        btn_addaid.isEnabled = false
//        btn_loadconfig.isEnabled = false

        val keyStorePath = filesDir.path
        awsHelper = AWSIoTHelper(
            "a33gna0t4ob4fa-ats.iot.ap-southeast-1.amazonaws.com",
            keyStorePath,
            "keystore",
            "test_shadow"
        )
        awsconnect()

        EMVCOHelper.EmvEnvParaInit()
        PosApiHelper.getInstance().SysLogSwitch(1)


        intent.apply {
            AID = getStringExtra("AID")
            TERM = getStringExtra("TERM")
            CAPK = getStringExtra("CAPK")
        }

        btn_addaid.setOnClickListener {
            Log.i("test_config",""+AID)
            aid = StringUtil.hexStringToBytes(AID)
            EMVCOHelper.EmvAddOneAIDS(aid, aid.size)
            setTextView("AID",AID)
        }
        btn_addcapk.setOnClickListener {
            Log.i("test_config",""+CAPK)
            capk = StringUtil.hexStringToBytes(CAPK)
            EMVCOHelper.EmvAddOneCAPK(capk, capk.size)
            setTextView("CAPK",CAPK)
        }
        btn_addterm.setOnClickListener {
            Log.i("test_config",""+TERM)
            term = StringUtil.hexStringToBytes(TERM)
            EMVCOHelper.EmvSaveTermParas(term, term.size, 0)
            setTextView("TERM",TERM)
        }
        btn_clearaid.setOnClickListener {
            EMVCOHelper.EmvClearAllAIDS()
        }
        btn_clearcapk.setOnClickListener {
            EMVCOHelper.EmvClearAllCapks()
        }
        btn_loadconfig.setOnClickListener {
            awsHelper!!.publish("","\$aws/things/POS_config/shadow/name/POS_CS10_config/get")
            awsHelper!!.publish("","\$aws/things/POS_config/shadow/name/POS_CS10_config/get")
            btn_clearcapk.isEnabled = true
            btn_clearaid.isEnabled = true
            btn_addterm.isEnabled = true
            btn_addcapk.isEnabled = true
            btn_addaid.isEnabled = true

        }

    }
    override fun onGetPermanentCertificate(certificateObject: CertificateObject?) {

    }

    override fun onRegisterComplete(registerObject: RegisterObject?) {

    }

    override fun onSetConfig(set : String?) {
//        val test = Test()
        val config = Gson().fromJson(set, Test::class.java)
        if (config!=null){
            AID=""
            CAPK=""
            TERM=""
            for (element in config.state.delta.AID){
                AID += element
            }
            for (element in config.state.delta.term){
                TERM += element
            }
            for (element in config.state.delta.capk){
                CAPK += element
            }
        }
//        setTextView("AID",AID)
//        setTextView("TERM",TERM)
//        setTextView("CAPK",CAPK)
//        Log.i("aaaaaaaa", "AID" + AID+"\nTERM"+TERM+"\nCAPK"+CAPK)
    }

    override fun onStatusConnection(Showmsg: String?) {
        Log.i("TESTTEST", "Status :" + Showmsg)
        if (Showmsg == "connected") {
            awsHelper!!.subscribe("test")
            awsHelper!!.subscribe("\$aws/things/POS_config/shadow/name/POS_CS10_config/get/accepted")
//            btn_loadconfig.isEnabled = true
        }
//        if (Showmsg == "connection lost") {
//            btn_loadconfig.isEnabled = false
//        }
//        if (Showmsg == "Try to connect") {
//            btn_loadconfig.isEnabled = false
//        }
//        if (Showmsg == "reconnecting") {
//            btn_loadconfig.isEnabled = false
//        }

    }
    fun awsconnect() {


        // init aws helper

        awsHelper!!.setProvisioningEvent(this)
        awsHelper!!.saveKeyAndCertificate(
            Certificate.KEY_CLAIM,
            Certificate.CERT_CLAIM,
            "CA_ID",
            "12345678"
        )

        awsHelper!!.connect("CA_ID", "12345678")


    }
    fun setNormalDialog(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
//                Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_SHORT).show()
            })
        val dialog = builder.create()
        dialog.show()
    }
    fun setTextView(topic:String?,msg: String?){
        view = if(view==null) "" else view
        var msg = if(msg==null) "" else msg
        view = view+topic+" : "+msg+"\n"
        tv_loadconfig.setText(view)
    }
}
data class Test(
    val state : State
)
data class State(
    val delta : Config
)
data class Config(
    val AID : Array<String>,
    val term : Array<String>,
    val capk : Array<String>
)