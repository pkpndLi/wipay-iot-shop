package com.example.wipay_iot_shop

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.testpos.database.transaction.AppDatabase
import com.example.testpos.database.transaction.SaleDao
import com.example.testpos.database.transaction.SaleEntity
import com.example.testpos.evenbus.data.MessageEvent
import com.example.wipay_iot_shop.printer.Printer
import com.example.wipay_iot_shop.transaction.FlagReverseEntity
import com.example.wipay_iot_shop.transaction.StuckReverseEntity
import com.imohsenb.ISO8583.builders.ISOClientBuilder
import com.imohsenb.ISO8583.builders.ISOMessageBuilder
import com.imohsenb.ISO8583.entities.ISOMessage
import com.imohsenb.ISO8583.enums.FIELDS
import com.imohsenb.ISO8583.enums.MESSAGE_FUNCTION
import com.imohsenb.ISO8583.enums.MESSAGE_ORIGIN
import com.imohsenb.ISO8583.enums.VERSION
import com.imohsenb.ISO8583.exceptions.ISOClientException
import com.imohsenb.ISO8583.exceptions.ISOException
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.ArrayList
import kotlin.experimental.and

class SettlementActivity : AppCompatActivity() {

    var appDatabase : AppDatabase? = null
    var saleDAO : SaleDao? = null

    // Get SharedPreferences
    private val MY_PREFS = "my_prefs"
    private lateinit var sp: SharedPreferences

    var saleCountTxt: TextView? = null
    var saleAmountTxt: TextView? = null

    var stringValue = ""
    var booleanValue : Boolean? = null
    var log = "log"

    var readStan: Int? = null
    var stan: Int? = null
    var readId: Int? = null
    var isoMsgArray = ArrayList<String>()
    var isoMsg: String? = null
    var readIsoMsg: String? = null

    var saleCount: Int? = 0
    var saleAmount: Int? = 0
    var batchTotals: String? = null

    var responseCode: String? = null

    private var saleReport: View? = null
    private var bitmap: Bitmap? = null
    private val RC_WRITE_EXTERNAL_STORAGE = 123

    var printer :Printer?=null
    //    private val HOST = "192.168.43.195"
//    var PORT = 5000
    private val HOST = "192.168.1.9"
    var PORT = 5001


    //get initial value from MenuActivity
    var settlementFlag:Boolean? = null
    var firstTransactionFlag:Boolean? = null
    var oldStartId:Int? = null
    var startId:Int? = null
    var endId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settlement)

        Log.d(log,"on settlementActivity.")
        saleReport = findViewById(R.id.SaleReportActivity)

        var confirmBtn = findViewById<Button>(R.id.confirmBtn)
         saleCountTxt = findViewById<TextView>(R.id.saleCountTxt)
         saleAmountTxt = findViewById<TextView>(R.id.sumAmountTxt)


         sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)
         startId = sp.getInt("startId",1)
         oldStartId = sp.getInt("oldStartId",0)
         Log.w(log,"oldStartId: " + oldStartId)
         Log.w(log,"startId: " + startId)


        if(oldStartId == startId){
            setDialog("Processing failed.","There has never been any transaction.")

        }else{
            setDialogQueryTransaction("","Wait a moment, the system is processing...")
        }

        confirmBtn.setOnClickListener{
            //set settlementFlag = 1
            val editor: SharedPreferences.Editor = sp.edit()
            editor.putBoolean("settlementFlag", true)
            editor.commit()

            stan = readStan?.plus(1)
            batchTotals = buildBatchTotals(saleCount!!, amount(saleAmount!!))
            //test settlementPacket
            settlementFlag =  sp.getBoolean("settlementFlag",false)
            Log.w(log,"settlementFlag: " + settlementFlag)
            Log.e(log,"stan: "+ stan + "\n" + "batchTotals: " + batchTotals)
            Log.e(log,"Settlement Packet: " + settlementPacket())

            sendPacket(settlementPacket())

        }




//       stringValue = sp.getString("stringKey","not found!").toString()
//       booleanValue = sp.getBoolean("booleanKey",false)
//
//        Log.i(log, "String value: " + stringValue)
//        Log.i(log, "Boolean value: " + booleanValue)

        //test Save SharedPreferences
//        val editor: SharedPreferences.Editor = sp.edit()
//        editor.putString("stringKey", "This is a book!")
//        editor.putBoolean("settlementFlag", true)
//        editor.commit()


//        test function sum transaction
//        btn.setOnClickListener{
//            Thread{
//                transaction!!.accessDatabase()
//                readStan = transaction!!.saleDAO?.getSale()?.STAN
//
//                for(i in startId..endId){
//                    readIsoMsg = transaction!!.saleDAO?.getSaleWithID(i)?.isoMsg
//                        if(readIsoMsg != null){
//                            saleCount = saleCount?.plus(1)
//                            saleAmount = saleAmount?.plus(codeUnpack(readIsoMsg!!,4)!!.toInt())
//                        }
//                    Log.e(log, "Read isoMag: " + readIsoMsg)
//                }
//                Log.e(log, "Read STAN: " + readStan)
//                Log.e(log, "Sale Count: " + saleCount)
//                Log.e(log, "Sale Amount: " + saleAmount)
//            }.start()
//        }


    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

//        EventBus.getDefault().post(MessageEvent(
//            "runDBthread",
//            ""
//        ))


    }

    override fun onResume() {
        super.onResume()

//        EventBus.getDefault().post(MessageEvent(
//            "runProcessing",
//            ""
//        ))

    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessageEvent(event: MessageEvent){

//        if(event.type == "runDBthread"){
//            Thread{
//                accessDatabase()
//                readStan = saleDAO?.getSale()?.STAN
//                Log.i("log_tag","readSTAN : " + readStan)
//            }.start()
//
//            if(readStan == null) {
//                stan = 1117
//            }
//        }
//        if(event.type == "runProcessing"){
//            manageProcessing()
//        }
//        else {
//
//        }

        manageResponse(event)
    }


    fun sendPacket(packet: ISOMessage?){
        Thread {
            try {
                var client = ISOClientBuilder.createSocket(HOST, PORT)
                    .configureBlocking(false)
                    .setReadTimeout(5000)
                    .build()
                client.connect()

                var response = bytesArrayToHexString(client.sendMessageSync(packet))
                EventBus.getDefault().post(
                    MessageEvent(
                    "iso_response",
                    response.toString())
                )

                client.disconnect()

            } catch (err: ISOClientException) {
                Log.e(log, "error1 is ${err.message}")
                if (err.message.equals("Read Timeout")) {

                    runOnUiThread {
                        setDialog("Transaction failed!.","Response Timeout.")
                    }
                }

            } catch(err: ISOException){
                Log.e("log_tag", "error2 is ${err.message}")
            } catch (err: IOException){

                if (err.message!!.indexOf("ECONNREFUSED") > -1) {
                    Log.e(log, "connection fail.")

                    runOnUiThread {
                        setDialog("Transaction failed!.","Unable to connect to bank.")
                    }
                }
            }
        }.start()
    }

    fun manageResponse(event: MessageEvent){

        Log.i("log_tag", "Response Message:" + event.message)
        responseCode = codeUnpack(event.message,39)
        Log.e("log_tag", "response code:"+ responseCode)

        if(responseCode == "3030"){

            Log.i(log, "Settlement Approve.")

            val editor: SharedPreferences.Editor = sp.edit()
            editor.putBoolean("settlementFlag", false)
            editor.putBoolean("firstTransactionFlag", true)
            editor.putInt("oldStartId", startId!!)
            editor.commit()

            setDialog(null,"Transaction complete.")

            settlementFlag =  sp.getBoolean("settlementFlag",false)
            firstTransactionFlag = sp.getBoolean("firstTransactionFlag",false)
            Log.w(log,"settlementFlag: " + settlementFlag)
            Log.w(log,"firstTransactionFlag: " + firstTransactionFlag)

            var settlementApprove  = SaleEntity(null,null,stan)

            Thread{

                accessDatabase()
                saleDAO?.insertSale(settlementApprove)
                readStan = saleDAO?.getSale()?.STAN
//                Log.i("log_tag","saveTransaction :  " + )
                Log.w(log,"saveSTAN : " + readStan)

            }.start()

            //save  sale report in photo album
//
//            printer = Printer(menuName,totalAmount)
//            printer!!.printSlip()
//            bitmap = ScreenShott.getInstance().takeScreenShotOfView(saleReport)
//            screenshotTask()
//            Log.i(log,"save sale report already.")

        }else{

            if(responseCode == "3935"){
                //implement batch upload
            }

            errorCode(responseCode,"Please check your problem.")
            Log.e(log,"Settlement Error!!!.")
        }

    }


    fun accessDatabase(){
        appDatabase = AppDatabase.getAppDatabase(this)
        saleDAO = appDatabase?.saleDao()

    }

    fun settlementPacket(): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .reconciliation()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("920000")
            .setField(FIELDS.F11_STAN, stan.toString())
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray("3535353535353535"))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray("353535353535353535353535353535"))
            .setField(FIELDS.F60_Reserved_National,"000142")
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setField(FIELDS.F63_Reserved_Private,hexStringToByteArray(batchTotals.toString()))
            .setHeader("6001208000")
            .build()
    }

    fun setDialogQueryTransaction(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
                Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_LONG).show()

                Thread{
                    //query transaction from DB and sum saleCount and saleAmount
                   accessDatabase()
                    readStan = saleDAO?.getSale()?.STAN
                    readId = saleDAO?.getSale()?._id
                    endId = readId!!

                    Log.w(log, "endId: " + readId)
                    Log.w(log, "Read STAN: " + readStan)


                    for(i in startId?.rangeTo(endId!!)!!){
                        readIsoMsg = saleDAO?.getSaleWithID(i)?.isoMsg
//                    isoMsgArray.add(readIsoMsg!!)
                        if(readIsoMsg != null){
                            saleCount = saleCount?.plus(1)
                            saleAmount = saleAmount?.plus(codeUnpack(readIsoMsg!!,4)!!.toInt())
                        }
                        Log.e(log, "Read isoMag: " + readIsoMsg)
                    }
                    Log.e(log, "Sale Count: " + saleCount)
                    Log.e(log, "Sale Amount: " + saleAmount)

                    runOnUiThread {

                        saleCountTxt?.setText(saleCount.toString())
                        saleAmountTxt?.setText(saleAmount.toString())

                    }

//                    accessDatabase()
//                    readStan = saleDAO?.getSale()?.STAN
//                    readFlagReverse = flagReverseDAO?.getFlagReverse()?.flagReverse
//                    readStuckReverse = stuckReverseDAO?.getStuckReverse()?.stuckReverse
//                    reReversal = reversalDAO?.getReversal()?.isoMsg
//                    Log.i("log_tag","readSTAN : " + readStan)
//                    Log.i("log_tag","readFlagReverse : " + readFlagReverse)
//                    Log.i("log_tag","readStuckReverse : " + readStuckReverse)
////                    Log.i("log_tag","reReversal : $reReversal ")
                }.start()
            })

            DialogInterface.OnClickListener{ dialog, which ->
                Toast.makeText(applicationContext,android.R.string.cancel, Toast.LENGTH_LONG).show()
                startActivity(Intent(this,MenuActivity::class.java))
            }

        val dialog = builder.create()
        dialog.show()
    }

    fun errorCode(code: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Transaction Error.")
        builder.setMessage("Error code: " + code +",  ${msg}")
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),DialogInterface.OnClickListener{ dialog, which ->
            Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_LONG).show()
            startActivity(Intent(this,MenuActivity::class.java))
        })
        val dialog = builder.create()
        dialog.show()
    }

    fun setDialog(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
                Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_LONG).show()
                startActivity(Intent(this,MenuActivity::class.java))
            })
        val dialog = builder.create()
        dialog.show()
    }

    private fun bytesArrayToHexString(b1: ByteArray): String? {
        val strBuilder = StringBuilder()
        for (`val` in b1) {
            strBuilder.append(String.format("%02x", `val` and 0xff.toByte()))
        }
        return strBuilder.toString()
    }

    private fun hexStringToByteArray(s: String): ByteArray? {
        val b = ByteArray(s.length / 2)
        for (i in b.indices) {
            val index = i * 2
            val v = s.substring(index, index + 2).toInt(16)
            b[i] = v.toByte()
        }
        return b
    }

    fun codeUnpack(response: String,field: Int): String? {
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(response)
            .build()
        val responseCode: String? = bytesArrayToHexString(isoMessageUnpacket.getField(field))
        return responseCode
    }


    fun amount(amount : Int):Double{
        var a = amount.toString()
        a = a.substring(0,a.length-2)
        Log.i("testtest",""+a.toDouble())
        return a.toDouble()
    }


    fun buildBatchTotals(Salecount :Int,Saleamount :Double):String{

        var DE63 =""
        var salecount = Salecount.toString().padStart(3,'0')
        var saleamount = String.format("%.2f",Saleamount)
        var arr : Array<String>
        arr = saleamount.split('.').toTypedArray()
        saleamount = arr[0]+arr[1]
        saleamount = saleamount.padStart(12,'0')
        var data = salecount+saleamount
        data = data.padEnd(90,'0')
        var data_arr :CharArray = data.toCharArray()
        for (i:Int in 0..data.length-1){
            val c = data_arr[i]
            val ascii = c.code
            DE63 += String.format("%02X", ascii)
        }
        return DE63
    }

}

