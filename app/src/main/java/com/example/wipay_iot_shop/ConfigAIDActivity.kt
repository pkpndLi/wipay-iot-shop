package com.example.wipay_iot_shop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import vpos.apipackage.PosApiHelper
import vpos.apipackage.StringUtil
import vpos.keypad.EMVCOHelper

class ConfigAIDActivity : AppCompatActivity() {
    var data=""
    var AID = byteArrayOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_aidactivity)
        EMVCOHelper.EmvEnvParaInit()
        PosApiHelper.getInstance().SysLogSwitch(1)

        var edit_9F06 = findViewById<EditText>(R.id.edit_9F06)
        var edit_9F01 = findViewById<EditText>(R.id.edit_9F01)
        var edit_9F09 = findViewById<EditText>(R.id.edit_9F09)
        var edit_9F15 = findViewById<EditText>(R.id.edit_9F15)
        var edit_9F16 = findViewById<EditText>(R.id.edit_9F16)
        var edit_9F4E = findViewById<EditText>(R.id.edit_9F4E)
        var edit_DF11 = findViewById<EditText>(R.id.edit_DF11)
        var edit_DF13 = findViewById<EditText>(R.id.edit_DF13)
        var edit_DF12 = findViewById<EditText>(R.id.edit_DF12)
        var edit_DF14 = findViewById<EditText>(R.id.edit_DF14)
        var edit_DF15 = findViewById<EditText>(R.id.edit_DF15)
        var edit_DF16 = findViewById<EditText>(R.id.edit_DF16)
        var edit_DF17 = findViewById<EditText>(R.id.edit_DF17)
        var edit_DF18 = findViewById<EditText>(R.id.edit_DF18)
        var edit_9F1B = findViewById<EditText>(R.id.edit_9F1B)
        var edit_9F1C = findViewById<EditText>(R.id.edit_9F1C)
        var edit_5F2A = findViewById<EditText>(R.id.edit_5F2A)
        var edit_5F36 = findViewById<EditText>(R.id.edit_5F36)
        var edit_9F3C = findViewById<EditText>(R.id.edit_9F3C)
        var edit_9F3D = findViewById<EditText>(R.id.edit_9F3D)
        var edit_9F1D = findViewById<EditText>(R.id.edit_9F1D)
        var edit_DF01 = findViewById<EditText>(R.id.edit_DF01)
        var edit_DF19 = findViewById<EditText>(R.id.edit_DF19)
        var edit_DF20 = findViewById<EditText>(R.id.edit_DF20)
        var edit_DF21 = findViewById<EditText>(R.id.edit_DF21)
        var edit_9F7B = findViewById<EditText>(R.id.edit_9F7B)
        var btn_addAID_contact = findViewById<Button>(R.id.btn_addAID_contact)
        var btn_addAID_contactless = findViewById<Button>(R.id.btn_addAID_contactless)
        var btn_clearAID_contact = findViewById<Button>(R.id.btn_clearAID_contact)
        var btn_clearAID_contactless = findViewById<Button>(R.id.btn_clearAID_contactless)
        var btn_ok = findViewById<Button>(R.id.btn_ok)

        btn_addAID_contact.setOnClickListener{
            data=""
            data = data+edit_9F06.text+edit_9F01.text+edit_9F09.text+edit_9F15.text+edit_9F16.text+edit_9F4E.text+
                    edit_DF11.text+edit_DF13.text+edit_DF12.text+edit_DF14.text+edit_DF15.text+edit_DF16.text+
                    edit_DF17.text+edit_DF18.text+edit_9F1B.text+edit_9F1C.text+edit_5F2A.text+edit_5F36.text+
                    edit_9F3C.text+edit_9F3D.text+edit_9F1D.text+edit_DF01.text+edit_DF19.text+edit_DF20.text+
                    edit_DF21.text+edit_9F7B.text

            AID = StringUtil.hexStringToBytes(data)
            EMVCOHelper.EmvAddOneAIDS(AID, AID.size)
        }
        btn_clearAID_contact.setOnClickListener{
            EMVCOHelper.EmvClearAllAIDS()
        }
        btn_ok.setOnClickListener{
            startActivity(Intent(this,ConfigActivity::class.java))
        }
    }


}