package com.example.wipay_iot_shop

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.testpos.database.transaction.*
import com.example.testpos.evenbus.data.MessageEvent
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

    var processing = false
    var totalAmount:Int? = null
    var cardNO:String = ""
    var cardEXD:String = ""

    var output1: TextView? = null
    var output2: TextView? = null
    var stan: Int? = null
    var reverseFlag = false
    var reversal: String? = null
    var responseCode: String? = null
    var reReversal: String? = null
    var reversalMsg: ISOMessage? = null
    var saleMsg: ISOMessage? = null
    var readSale: String? = null
    var readStan: Int? = null
    var stuckReverse = false

    private val HOST = "192.168.43.195"
    var PORT = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        intent.apply {
            processing = getBooleanExtra("processing",false)
            totalAmount = getIntExtra("totalAmount",145)
            cardNO = getStringExtra("cardNO").toString()
            cardEXD = getStringExtra("cardEXD").toString()
            stan = getIntExtra("STAN",1)
        }

        if(processing == true) {


            if (reverseFlag) {
                stuckReverse = true

                Log.i("log_tag", "send reverse packet")
//                    sendPacket(reversalPacket(stan.toString()))
                sendPacket(reBuildISOPacket(reReversal.toString()))
                Log.i("log_tag", "reversal:  " + reReversal.toString())
                Log.i("log_tag", "reverseFlag:  " + reverseFlag)

            }
            else{

                stan = stan?.plus(1)
                saleMsg = salePacket(stan.toString())
                Log.i("log_tag", "Current stan: " + stan)

                reversalMsg = reversalPacket(stan.toString())
                var reverseTrans = ReversalEntity(null,reversalMsg.toString())

                reverseFlag = true
                Log.i("log_tag", "send sale packet")
                sendPacket(saleMsg)
                Log.i("log_tag", "sale: " + saleMsg.toString())
                Log.i("log_tag", "reverseFlag:  " + reverseFlag)
                //                Log.i("log_tag", "else" + reverseFlag)

                Thread{

                    accessDatabase()

                    reversalDAO?.insertReversal(reverseTrans)
                    reReversal = reversalDAO?.getReversal()?.isoMsg
//                        Log.i("log_tag","reReversal:  " + reReversal.toString() )

                }.start()

            }

        }


    }

    fun accessDatabase(){

        appDatabase = AppDatabase.getAppDatabase(this)
        reversalDAO = appDatabase?.reversalDao()
        saleDAO = appDatabase?.saleDao()

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


        @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessageEvent(event: MessageEvent){

        reverseFlag = false
        output1?.setText("Response Message: " + event.message)
        Log.i("log_tag", "Response Message:" + event.message)
        responseCode = codeUnpack(event.message,39)
        output2?.setText("response code: " + responseCode)
        Log.i("log_tag", "response code:"+ responseCode)

        if(responseCode == "3030"){

            if(stuckReverse == true){

                Log.i("log_tag", "Reversal Approve.")
//                reversalApprove()
//                setDialog("Cenceling Success.","Successfully canceled the transaction.")

                var reStan = codeUnpack(reReversal.toString(),11)
                var reversalApprove = SaleEntity(null,reReversal.toString(), reStan!!.toInt())

                Thread{

                    accessDatabase()
                    saleDAO?.insertSale(reversalApprove)
                    readSale = saleDAO?.getSale()?.isoMsg
                    readStan = saleDAO?.getSale()?.STAN
                    Log.i("log_tag","saveReversalApprove :  " + readSale)
                    Log.i("log_tag","saveSTAN : " + readStan)

                }.start()

                stuckReverse = false

                stan = stan?.plus(1)
                saleMsg = salePacket(stan.toString())
                Log.i("log_tag", "Current stan: " + stan)

                reversalMsg = reversalPacket(stan.toString())
                var reverseTrans = ReversalEntity(null,reversalMsg.toString())

                reverseFlag = true
                Log.i("log_tag", "send sale packet")
                sendPacket(saleMsg)
                Log.i("log_tag", "sale: " + saleMsg.toString())
                Log.i("log_tag", "reverseFlag:  " + reverseFlag)
                //                Log.i("log_tag", "else" + reverseFlag)

                //reverse สำหรับ transaction ปัจจุบัน
                Thread{

                    accessDatabase()
                    reversalDAO?.insertReversal(reverseTrans)
                    reReversal = reversalDAO?.getReversal()?.isoMsg
//                        Log.i("log_tag","reReversal:  " + reReversal.toString() )

                }.start()

            }else{
                Log.i("log_tag", "Transaction Approve.")
//                transactionApprove()
                setDialog(null,"Transaction complete.")

                var saleApprove = SaleEntity(null,saleMsg.toString(),stan)

                Thread{

                    accessDatabase()

                    saleDAO?.insertSale(saleApprove)
                    readSale = saleDAO?.getSale()?.isoMsg
                    readStan = saleDAO?.getSale()?.STAN
                    Log.i("log_tag","saveTransaction :  " + readSale)
                    Log.i("log_tag","saveSTAN : " + readStan)

                }.start()
            }

        }else{

            if(responseCode == "3934"){

                errorCode(responseCode,"Seqence error / Duplicate transmission")

            }else{

                errorCode(responseCode,null)

            }

            Log.i("log_tag", "Error code: " + responseCode)
            var saleApprove = SaleEntity(null,null.toString(),stan)

            Thread{

                accessDatabase()

                saleDAO?.insertSale(saleApprove)
                readSale = saleDAO?.getSale()?.isoMsg
                readStan = saleDAO?.getSale()?.STAN
                Log.i("log_tag","saveTransaction :  " + readSale)
                Log.i("log_tag","saveSTAN : " + readStan)

            }.start()

        }

        Log.i("log_tag", "reverseFlag:  " + reverseFlag)

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
                    response.toString()
                )
                )
//                    Log.i("log_tag", "response : $response")
                client.disconnect()

            } catch (err: ISOClientException) {
                Log.e("log_tag", "error1 is ${err.message}")
                if (err.message.equals("Read Timeout")) {

                    runOnUiThread {
//
                        if(stuckReverse == true){
                            setDialog("Cancel failed!!.Stuck in reverse.","Failed to cancel previous transaction.Timeout! have no response message")
                        }else{
//                            timeoutAlert()
                            setDialog("Transaction failed!!","Timeout! have no response message.This transaction must be cancelled.")
                        }
                        output2?.setText("Reverse flag: " + reverseFlag)
                        Log.i("log_tag", "reverseFlag:  " + reverseFlag)
                    }
                }

            } catch(err: ISOException){
                Log.e("log_tag", "error2 is ${err.message}")
            } catch (err: IOException){
//                    Log.e("log_tag", "error3 is ${err.message}")
                if (err.message!!.indexOf("ECONNREFUSED") > -1) {
                    Log.e("log_tag", "connection fail.")

                    runOnUiThread {
//
                        if(stuckReverse == true){
//                            reverselNonApproveConnectLoss()
                            setDialog("Cancel failed!!.Stuck in reverse.","Failed to cancel previous transaction.Connection failed! have no response message")
                        }else{
//                            connectionFailAlert()
                            setDialog("Transaction failed!!","Connection failed! Please check your network.This transaction must be cancelled.")
                        }
                        output2?.setText("Reverse flag: " + reverseFlag)
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
            .setField(FIELDS.F4_AmountTransaction, totalAmount.toString())
            .setField(FIELDS.F11_STAN, STAN)
            .setField(FIELDS.F14_ExpirationDate, cardEXD)
            .setField(FIELDS.F22_EntryMode, "0010")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray("3232323232323232"))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray("323232323232323232323232323232"))
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
            .setField(FIELDS.F4_AmountTransaction, totalAmount.toString())
            .setField(FIELDS.F11_STAN, STAN)
            .setField(FIELDS.F14_ExpirationDate, cardEXD)
            .setField(FIELDS.F22_EntryMode, "0010")
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(FIELDS.F25_POS_ConditionCode, "00")
            .setField(FIELDS.F41_CA_TerminalID,hexStringToByteArray("3232323232323232"))
            .setField(FIELDS.F42_CA_ID,hexStringToByteArray("323232323232323232323232323232"))
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

}