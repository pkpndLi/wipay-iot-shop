package com.example.wipay_iot_shop

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
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

    var AID : String? = null
    var TERM : String? = null
    var CAPK : String? = null

    var aid = byteArrayOf()
    var term = byteArrayOf()
    var capk = byteArrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_config)

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

        this.btn_addaid=findViewById(R.id.btn_addaid)
        this.btn_addcapk=findViewById(R.id.btn_addcapk)
        this.btn_addterm=findViewById(R.id.btn_addterm)
        this.btn_clearaid=findViewById(R.id.btn_clearaid)
        this.btn_clearcapk=findViewById(R.id.btn_clearcapk)

        intent.apply {
            AID = getStringExtra("AID")
            TERM = getStringExtra("TERM")
            CAPK = getStringExtra("CAPK")
        }

        btn_addaid.setOnClickListener {
            Log.i("test_config",""+AID)
            aid = StringUtil.hexStringToBytes(AID)
            EMVCOHelper.EmvAddOneAIDS(aid, aid.size)
        }
        btn_addcapk.setOnClickListener {
            Log.i("test_config",""+CAPK)
            capk = StringUtil.hexStringToBytes(CAPK)
            EMVCOHelper.EmvAddOneCAPK(capk, capk.size)
        }
        btn_addterm.setOnClickListener {
            Log.i("test_config",""+TERM)
            term = StringUtil.hexStringToBytes(TERM)
            EMVCOHelper.EmvSaveTermParas(term, term.size, 0)
        }
        btn_clearaid.setOnClickListener {
            EMVCOHelper.EmvClearAllAIDS()
        }
        btn_clearcapk.setOnClickListener {
            EMVCOHelper.EmvClearAllCapks()
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
        Log.i("aaaaaaaa", "" + AID)
    }

    override fun onStatusConnection(Showmsg: String?) {
        Log.i("TESTTEST", "Status :" + Showmsg)
        if (Showmsg == "connected") {
            awsHelper!!.subscribe("test")
            awsHelper!!.subscribe("\$aws/things/POS_config/shadow/name/POS_CS10_config/get/accepted")
//            setDialogNormal("test T","test M")
        }
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
    fun setDialogNormal(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
                awsHelper!!.publish("test","\$aws/things/POS_config/shadow/name/POS_CS10_config/get")
                Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_LONG).show()
            })
        val dialog = builder.create()
        dialog.show()
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