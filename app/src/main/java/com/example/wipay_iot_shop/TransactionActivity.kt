package com.example.wipay_iot_shop

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.testpos.database.transaction.*
import com.example.testpos.evenbus.data.MessageEvent
import com.example.wipay_iot_shop.printer.Printer
import com.example.wipay_iot_shop.transaction.FlagReverseDao
import com.example.wipay_iot_shop.transaction.FlagReverseEntity
import com.example.wipay_iot_shop.transaction.StuckReverseDao
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
import java.io.IOException
import kotlin.experimental.and

class TransactionActivity : AppCompatActivity() {
    var appDatabase : AppDatabase? = null
    var reversalDAO : ReversalDao? = null
    var saleDAO : SaleDao? = null
    var flagReverseDAO : FlagReverseDao? = null
    var stuckReverseDAO : StuckReverseDao? = null

    private val MY_PREFS = "my_prefs"
    private lateinit var sp: SharedPreferences

    var processing = false
    var totalAmount:Int? = null
    var cardNO:String = ""
    var cardEXD:String = ""
    var menuName:String = ""
    var DE55:String = ""

    var initialStan: Int? = 1150

    var output1: TextView? = null
    var output2: TextView? = null
    var stan: Int? = null
    var reverseFlag :Boolean? = null
    var reversal: String? = null
    var responseCode: String? = null
    var reReversal: String? = null
    var reversalMsg: ISOMessage? = null
    var saleMsg: ISOMessage? = null
    var readSale: String? = null
    var readStan: Int? = null
    var stuckReverse :Boolean? = null
    var readFlagReverse :Boolean? = null
    var readStuckReverse :Boolean? = null

    var printer :Printer?=null

    var settlementFlag:Boolean? = null
    var firstTransactionFlag:Boolean? = null
    var startId:Int = 0


    private val HOST = "192.168.1.16"
    var PORT = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)
        val setOrder = findViewById<TextView>(R.id.order)
        val setAmount = findViewById<TextView>(R.id.amount)
        settlementFlag =  sp.getBoolean("settlementFlag",false)
        firstTransactionFlag = sp.getBoolean("firstTransactionFlag",true)


            intent.apply {
//          processing = getBooleanExtra("processing",false)
            totalAmount = getIntExtra("totalAmount",145)
            cardNO = getStringExtra("cardNO").toString()
            cardEXD = getStringExtra("cardEXD").toString()
            menuName = getStringExtra("menuName").toString()
                DE55 = getStringExtra("DE55").toString()
                Log.i("log_tag",DE55)

        }

        setOrder.setText(menuName)
        setAmount.setText(totalAmount.toString())

        Log.i("log_tag","onCreate!!!")
        Log.d("log_tag","on transactionActivity.")
        Log.w("log_tag","settlementFlag: " + settlementFlag)
        Log.w("log_tag","firstTransactionFlag: " + firstTransactionFlag)

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

//        EventBus.getDefault().post(MessageEvent(
//            "runDBthread",
//            ""
//        ))
//      Check settlementFlag
        if(settlementFlag == true){

            setDialog("Transaction Error!!.","The sales report has not yet been completed.\n" +
                    "Must return to complete the sales report first.")
        }else{

            setDialogS("","Comfirm your order.")
            processing = true
        }

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({

            Log.i("log_tag","processing: "+processing)
            manageProcessing()

        }, 7000)
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

    fun accessDatabase(){

        appDatabase = AppDatabase.getAppDatabase(this)
        reversalDAO = appDatabase?.reversalDao()
        saleDAO = appDatabase?.saleDao()
        flagReverseDAO = appDatabase?.flagReverseDao()
        stuckReverseDAO = appDatabase?.stuckReverseDao()
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

    fun manageProcessing(){

        stan = readStan
        if(readStan == null){
            stan = initialStan
        }

        reverseFlag = readFlagReverse
        if(readFlagReverse == null){
            reverseFlag = false
        }

        stuckReverse = readStuckReverse
        if(readStuckReverse == null){
            stuckReverse = false
        }

        if(processing == true){

            if (reverseFlag!!) {
                stuckReverse = true
                var reverseStuck = StuckReverseEntity(null, stuckReverse)

                Log.i("log_tag", "send reverse packet")
//                    sendPacket(reversalPacket(stan.toString()))
//                Log.i("log_tag", "reversal:  " + reReversal.toString())
                sendPacket(reBuildISOPacket(reReversal.toString()))

                Log.i("log_tag", "reverseFlag:  " + reverseFlag)

                Thread{

                    accessDatabase()
                    stuckReverseDAO?.insertStuckReverse(reverseStuck)
                    readStuckReverse = stuckReverseDAO?.getStuckReverse()?.stuckReverse!!
//                    Log.i("log_tag"," : " + readStan)

                }.start()

            }
            else
            {
                sendTransactionProcess()

            }

        }

    }


    fun manageResponse(event: MessageEvent){

        output1?.setText("Response Message: " + event.message)
        Log.i("log_tag", "Response Message:" + event.message)
        responseCode = codeUnpack(event.message,39)
        output2?.setText("response code: " + responseCode)
        Log.i("log_tag", "response code:"+ responseCode)

        if(responseCode == "3030"){
            reverseFlag = false
            var flagReverse = FlagReverseEntity(null, reverseFlag)

            if(stuckReverse == true){

                Log.i("log_tag", "Reversal Approve.")
                stuckReverse = false
                var reverseStuck = StuckReverseEntity(null, stuckReverse)
//                reversalApprove()
//                setDialog("Cenceling Success.","Successfully canceled the transaction.")

                var reStan = codeUnpack(reReversal.toString(),11)
//                var reversalApprove = SaleEntity(null,reReversal.toString(), reStan!!.toInt())
                var reversalApprove = SaleEntity(null,null, reStan!!.toInt())

                Thread{

                    accessDatabase()
                    flagReverseDAO?.insertFlagReverse(flagReverse)
                    stuckReverseDAO?.insertStuckReverse(reverseStuck)
                    saleDAO?.insertSale(reversalApprove)
                    readSale = saleDAO?.getSale()?.isoMsg
                    readStan = saleDAO?.getSale()?.STAN
                    Log.i("log_tag","saveReverse-sale :  " + readSale)
                    Log.i("log_tag","saveSTAN : " + readStan)

                }.start()
                stan = stan?.plus(1)
                sendTransactionProcess()

            }else{      //transactionApprove


                Log.i("log_tag", "Transaction Approve.")
//                transactionApprove()
                setDialogApprove(null,"Transaction complete.")
                var saleApprove = SaleEntity(null,saleMsg.toString(),stan)

                Thread{

                    accessDatabase()
                    flagReverseDAO?.insertFlagReverse(flagReverse)
                    saleDAO?.insertSale(saleApprove)
                    readSale = saleDAO?.getSale()?.isoMsg
                    readStan = saleDAO?.getSale()?.STAN
                    startId = saleDAO?.getSale()?._id!!

                    if(firstTransactionFlag == true){

                        val editor: SharedPreferences.Editor = sp.edit()
                        editor.putInt("startId", startId)
                        editor.putBoolean("firstTransactionFlag", false)
                        editor.commit()

                        Log.i("log_tag","startId :  " + startId)
                    }

                    Log.w("log_tag","saveId :  " + startId)
                    Log.w("log_tag","firstTransactionFlag: " + firstTransactionFlag)
                    Log.w("log_tag","saveTransaction :  " + readSale)
                    Log.w("log_tag","saveSTAN : " + readStan)

                }.start()
            }

        }else{

            if(stuckReverse == true){

                errorCode(responseCode,null)

            } else{

                reverseFlag = false
                var flagReverse = FlagReverseEntity(null, reverseFlag)

                if(responseCode == "3934"){

                    errorCode(responseCode,"Seqence error / Duplicate transmission")

                }else{

                    errorCode(responseCode,null)

                }

                Log.i("log_tag", "Error code: " + responseCode)
                var saleApprove = SaleEntity(null,null.toString(),stan)

                Thread{

                    accessDatabase()

                    flagReverseDAO?.insertFlagReverse(flagReverse)
                    saleDAO?.insertSale(saleApprove)
                    readSale = saleDAO?.getSale()?.isoMsg
                    readStan = saleDAO?.getSale()?.STAN
                    Log.i("log_tag","saveTransaction :  " + readSale)
                    Log.i("log_tag","saveSTAN : " + readStan)

                }.start()

            }

        }

        Log.i("log_tag", "reverseFlag:  " + reverseFlag)

    }

    fun sendTransactionProcess(){

        stan = stan?.plus(1)
        saleMsg = salePacket(stan.toString())
        Log.i("log_tag", "Current stan: " + stan)

        reversalMsg = reversalPacket(stan.toString())
        var reverseTrans = ReversalEntity(null,reversalMsg.toString())

        reverseFlag = true
        var flagReverse = FlagReverseEntity(null, reverseFlag)

        Log.i("log_tag", "send sale packet")
        sendPacket(saleMsg)
//        Log.i("log_tag", "send ${reBuildISOPacket(saleMsg.toString())}")
        Log.i("log_tag", "sale: " + saleMsg.toString())
        Log.i("log_tag", "reverseFlag:  " + reverseFlag)

        Thread{
            accessDatabase()
            reversalDAO?.insertReversal(reverseTrans)
            flagReverseDAO?.insertFlagReverse(flagReverse)
            reReversal = reversalDAO?.getReversal()?.isoMsg
            Log.i("log_tag", "reversel :$reReversal")
        }.start()
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
                EventBus.getDefault().post(MessageEvent(
                    "iso_response",
                    response.toString()))

                client.disconnect()

            } catch (err: ISOClientException) {
                Log.e("log_tag", "error1 is ${err.message}")
                if (err.message!!.equals("Read Timeout")) {

                    runOnUiThread {
//
                        if(stuckReverse == true){
                            setDialog("Cancel failed!!.Stuck in reverse.","Failed to cancel previous transaction.Timeout! have no response message")
                        }else{
//                            timeoutAlert()
                            setDialog("Transaction failed!!","Timeout! have no response message.This transaction must be cancelled.")
                        }

                        Log.i("log_tag", "reverseFlag:  " + reverseFlag)
                    }
                }

            } catch(err: ISOException){
                Log.e("log_tag", "error2 is ${err.message}")
            } catch (err: IOException){

                if (err.message!!.indexOf("ECONNREFUSED") > -1) {
                    Log.e("log_tag", "connection fail.")

                    runOnUiThread {
                        if(stuckReverse == true){
//                            reverselNonApproveConnectLoss()
                            setDialog("Cancel failed!!.Stuck in reverse.","Failed to cancel previous transaction.Connection failed! have no response message")
                        }else{
//                            connectionFailAlert()
                            setDialog("Transaction failed!!","Connection failed! Please check your network.This transaction must be cancelled.")
                        }

                        Log.i("log_tag", "reverseFlag:  " + reverseFlag)
                    }
                }
            }
        }.start()
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

    @Throws(ISOException::class, ISOClientException::class, IOException::class)
    fun salePacket(STAN: String): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .financial()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("000000")
            .setField(FIELDS.F2_PAN, cardNO)
            .setField(FIELDS.F4_AmountTransaction, totalamount(totalAmount!!.toDouble() ))
            .setField(FIELDS.F11_STAN, STAN)
            .setField(FIELDS.F14_ExpirationDate, cardEXD)
            .setField(FIELDS.F22_EntryMode, "0010")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray("3232323232323232"))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray("323232323232323232323232323232"))
            .setField(FIELDS.F55_ICC,DE55)
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setHeader("6001208000")
            .build()
    }

    fun reversalPacket(STAN: String): ISOMessage? {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .reversal()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("000000")
            .setField(FIELDS.F2_PAN, cardNO)
            .setField(FIELDS.F4_AmountTransaction, totalamount(totalAmount!!.toDouble()))
            .setField(FIELDS.F11_STAN, STAN)
            .setField(FIELDS.F14_ExpirationDate, cardEXD)
            .setField(FIELDS.F22_EntryMode, "0010")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray("3232323232323232"))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray("323232323232323232323232323232"))
            .setField(FIELDS.F55_ICC,DE55)
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setHeader("6001208000")
            .build()
    }

    private fun testNetwork(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .networkManagement()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("990000")
            .setField(FIELDS.F24_NII_FunctionCode,"120")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray("3232323232323232"))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray("323232323232323232323232323232"))
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray("303030343841"))
            .setHeader("6001208000")
            .build()
    }


    fun reBuildISOPacket(packet: String): ISOMessage? {
        val isoMessage: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(packet)
            .build()
        return isoMessage
    }

    fun codeUnpack(response: String,field: Int): String? {
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(response)
            .build()
        val responseCode: String? = bytesArrayToHexString(isoMessageUnpacket.getField(field))
        return responseCode
    }

    fun mtiUnpack(response: String): String? {
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(response)
            .build()
        val mti: String? = isoMessageUnpacket.getMti()
        return mti
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

    fun setDialogS(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
            Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_LONG).show()

                Thread{
                    accessDatabase()
                    readStan = saleDAO?.getSale()?.STAN
                    readFlagReverse = flagReverseDAO?.getFlagReverse()?.flagReverse
                    readStuckReverse = stuckReverseDAO?.getStuckReverse()?.stuckReverse
                    reReversal = reversalDAO?.getReversal()?.isoMsg
                    Log.i("log_tag","readSTAN : " + readStan)
                    Log.i("log_tag","readFlagReverse : " + readFlagReverse)
                    Log.i("log_tag","readStuckReverse : " + readStuckReverse)
//                    Log.i("log_tag","reReversal : $reReversal ")
                }.start()
            })
        
            DialogInterface.OnClickListener{ dialog, which ->
                Toast.makeText(applicationContext,android.R.string.cancel, Toast.LENGTH_LONG).show()
                startActivity(Intent(this,MenuActivity::class.java))
            }

        val dialog = builder.create()
        dialog.show()
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

    private fun bytesArrayToHexString(b1: ByteArray): String? {
        val strBuilder = StringBuilder()
        for (`val` in b1) {
            strBuilder.append(String.format("%02x", `val` and 0xff.toByte()))
        }
        return strBuilder.toString()
    }
    fun setDialogApprove(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
                Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_LONG).show()
//                sendEmailProcess()
                val itn =Intent(this,MenuActivity::class.java).apply{
                    putExtra("totalAmount",totalAmount)
                    putExtra("menuName",menuName)
//                    putExtra("from","transAct")
                }
                startActivity(itn)
            })
        val dialog = builder.create()
        dialog.show()
    }

    fun totalamount(Totalamount : Double ):String{

        var amount : List<String> = String.format("%.2f",Totalamount).split(".")
        var Amount = amount[0]+amount[1]

        return Amount
    }
}