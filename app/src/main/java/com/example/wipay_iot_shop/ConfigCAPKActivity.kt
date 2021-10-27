package com.example.wipay_iot_shop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import vpos.apipackage.PosApiHelper
import vpos.apipackage.StringUtil
import vpos.keypad.EMVCOHelper

class ConfigCAPKActivity : AppCompatActivity() {
    var data=""
    var CAPK = byteArrayOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_capkactivity)
        EMVCOHelper.EmvEnvParaInit()
        PosApiHelper.getInstance().SysLogSwitch(1)


        var edit_9F06 = findViewById<EditText>(R.id.edit_9F06)
        var edit_9F22 = findViewById<EditText>(R.id.edit_9F22)
        var edit_DF07 = findViewById<EditText>(R.id.edit_DF07)
        var edit_DF06 = findViewById<EditText>(R.id.edit_DF06)
        var edit_DF02 = findViewById<EditText>(R.id.edit_DF02)
        var edit_DF04 = findViewById<EditText>(R.id.edit_DF04)
        var edit_DF05 = findViewById<EditText>(R.id.edit_DF05)
        var edit_DF03 = findViewById<EditText>(R.id.edit_DF03)
        var btn_addCAPK_contact = findViewById<Button>(R.id.btn_addCAPK_contact)
        var btn_addCAPK_contactless = findViewById<Button>(R.id.btn_addCAPK_contactless)
        var btn_clearCAPK_contact = findViewById<Button>(R.id.btn_clearCAPK_contact)
        var btn_clearCAPK_contactless = findViewById<Button>(R.id.btn_clearCAPK_contactless)
        var btn_ok = findViewById<Button>(R.id.btn_ok)


        btn_addCAPK_contact.setOnClickListener {
            data = ""
            data = data+edit_9F06.text+edit_9F22.text+edit_DF07.text+edit_DF06.text+edit_DF02.text+
                    edit_DF04.text+edit_DF05.text+edit_DF03.text

            CAPK = StringUtil.hexStringToBytes(data)
            EMVCOHelper.EmvAddOneCAPK(CAPK, CAPK.size)

        }
        btn_clearCAPK_contact.setOnClickListener {
            EMVCOHelper.EmvClearAllCapks()
        }
        btn_ok.setOnClickListener {
            startActivity(Intent(this,ConfigActivity::class.java))
        }

    }
}