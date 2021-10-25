package com.example.wipay_iot_shop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import vpos.apipackage.StringUtil
import vpos.keypad.EMVCOHelper

class ConfigTERMActivity : AppCompatActivity() {
    var data = ""
    var Term = byteArrayOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_termactivity)

        var edit_DF18 = findViewById<EditText>(R.id.edit_DF18)
        var edit_9F35 = findViewById<EditText>(R.id.edit_9F35)
        var edit_9F33 = findViewById<EditText>(R.id.edit_9F33)
        var edit_9F40 = findViewById<EditText>(R.id.edit_9F40)
        var edit_DF19 = findViewById<EditText>(R.id.edit_DF19)
        var edit_DF26 = findViewById<EditText>(R.id.edit_DF26)
        var edit_DF40 = findViewById<EditText>(R.id.edit_DF40)
        var edit_9F39 = findViewById<EditText>(R.id.edit_9F39)
        var edit_9F1A = findViewById<EditText>(R.id.edit_9F1A)
        var edit_9F1E = findViewById<EditText>(R.id.edit_9F1E)
        var edit_DF42 = findViewById<EditText>(R.id.edit_DF42)
        var edit_DF43 = findViewById<EditText>(R.id.edit_DF43)
        var edit_DF44 = findViewById<EditText>(R.id.edit_DF44)
        var edit_DF45 = findViewById<EditText>(R.id.edit_DF45)
        var edit_DF46 = findViewById<EditText>(R.id.edit_DF46)
        var btn_addTERM_contact = findViewById<Button>(R.id.btn_addTERM_contact)
        var btn_addTERM_contactless = findViewById<Button>(R.id.btn_addTERM_contactless)
        var btn_clearTERM_contactless = findViewById<Button>(R.id.btn_clearTERM_contactless)
        var btn_ok = findViewById<Button>(R.id.btn_ok)

        btn_addTERM_contact.setOnClickListener {
            data=""
            data= data+edit_DF18.text+edit_9F35.text+edit_9F33.text+edit_9F40.text+edit_DF19.text+
                    edit_DF26.text+edit_DF40.text+edit_9F39.text+edit_9F1A.text+edit_9F1E.text+
                    edit_DF42.text+edit_DF43.text+edit_DF44.text+edit_DF45.text+edit_DF46.text
            Term = StringUtil.hexStringToBytes(data)
            EMVCOHelper.EmvSaveTermParas(Term, Term.size, 0)
        }
        btn_ok.setOnClickListener {
            startActivity(Intent(this,ConfigActivity::class.java))
        }









    }
}