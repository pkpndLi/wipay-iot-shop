package com.example.wipay_iot_shop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

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

                }
                2->{
                    startActivity(Intent(this,SettlementActivity::class.java))
                }

            }
        }
    }
}