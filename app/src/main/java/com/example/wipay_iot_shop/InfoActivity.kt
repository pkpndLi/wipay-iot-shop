package com.example.wipay_iot_shop

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

 class InfoActivity : AppCompatActivity() {


     var menu:String? = null
     var amount:Int? = null
     var totalAmount:Int? = null
     var quantity:Int? = null

     var readStan: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val cupImg = findViewById<ImageView>(R.id.cup)
        val menuImg = findViewById<ImageView>(R.id.img)
        val menuName = findViewById<TextView>(R.id.menuName)
        val payBtn = findViewById<Button>(R.id.payBtn)
        val sumAmount = findViewById<TextView>(R.id.sumAmount)
        val inputQuantity = findViewById<EditText>(R.id.inputQuantity)

        cupImg.setImageDrawable(getImage(this, "coffee96"))

        intent.apply {
            menu = getStringExtra("menu")
            amount = getIntExtra("amount",145)
        }

        Log.i("logtag", menu.toString())

        if(menu == "goods1"){
              menuImg.setImageDrawable(getImage(this, "coffee"))
              menuName.setText("S'mores Coffee Fappuccino 145B")
        }
        if(menu == "goods2"){
            menuImg.setImageDrawable(getImage(this, "coffee1"))
            menuName.setText("Neapolitan Fappuccino 145B ")
        }
        if(menu == "goods3"){
            menuImg.setImageDrawable(getImage(this, "brownie"))
            menuName.setText("Espresso ChoChip Bronie 120B")
        }

        cupImg.setOnClickListener{
            quantity = inputQuantity.text.toString().toInt()
            totalAmount = quantity!! * amount!!


            runOnUiThread {
                sumAmount.setText("${totalAmount}B")
            }

        }

        payBtn.setOnClickListener {

            val itn = Intent(this,PaymentActivity::class.java).apply{
                putExtra("totalAmount",totalAmount)

            }
            startActivity(itn)

        }




    }




     fun getImage(context: Context, name: String?): Drawable? {
        return context.getResources().getDrawable(
            context.getResources().getIdentifier(name, "drawable", context.getPackageName())
        )
    }
 }