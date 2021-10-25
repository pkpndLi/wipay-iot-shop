package com.example.wipay_iot_shop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView

class ConfigActivity : AppCompatActivity() {
    lateinit var btn_aid:ImageView
    lateinit var btn_term:ImageView
    lateinit var btn_capk:ImageView
    lateinit var btn_back:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        this.btn_aid=findViewById(R.id.btn_aid)
        this.btn_term=findViewById(R.id.btn_term)
        this.btn_capk=findViewById(R.id.btn_capk)
        this.btn_back=findViewById(R.id.btn_back)

        this.btn_aid.setOnClickListener {
            startActivity(Intent(this,ConfigAIDActivity::class.java))
        }
        this.btn_term.setOnClickListener {
            startActivity(Intent(this,ConfigTERMActivity::class.java))
        }
        this.btn_capk.setOnClickListener {
            startActivity(Intent(this,ConfigCAPKActivity::class.java))
        }
        this.btn_back.setOnClickListener {
            startActivity(Intent(this,MenuActivity::class.java))
        }


    }
}