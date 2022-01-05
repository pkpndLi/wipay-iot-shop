package com.example.wipay_iot_shop

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
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
import com.imohsenb.ISO8583.utils.StringUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.sql.DriverManager
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import kotlin.experimental.and
import android.preference.PreferenceManager
import android.util.Log.e
import android.widget.TextView
import com.example.testpos.database.transaction.AppDatabase
import com.example.testpos.database.transaction.SaleDao
import com.example.wipay_iot_shop.crypto.DataConverter
import com.example.wipay_iot_shop.crypto.iDES
import com.example.wipay_iot_shop.crypto.iRSA
import com.example.wipay_iot_shop.transaction.ResponseDao
import java.security.interfaces.RSAPrivateKey


class DownloadKey : AppCompatActivity() {

    lateinit var tv_downloadKey : TextView
    var view :String? = null
    var appDatabase : AppDatabase? = null
    var saleDAO : SaleDao? = null
    var responseDAO : ResponseDao? = null
    private lateinit var sp: SharedPreferences
    private val MY_PREFS = "my_prefs"
    var log = "log"
    var indicator = "HTLE"
    var version = "04"
    var downlondType = "4"
    var reqType = "1"
    var acqID = "120"
    var LTID = "00000000"
    var vendorID = "12000002"
    var stan:Int = 0
    var TE_ID = "12002002"

    var TE_PIN = "22222222"
    var keyIdLtmk = "4339"
    var keyKCVltmk = ""
    var ltwkId = "0000"
    var tid = "55555555"
    var padding = "1234"
    var pinHash:String  = ""
    var txnHash:String = ""
    var stringHash = ""

    var strBit62Ltmk:String = ""
    var strBit62Ltwk:String = ""

    var responseCode:String = ""

    var ltmkState:Boolean = false
    var ltwkState:Boolean = false


//    private val HOST = "223.27.234.243"
//    var PORT = 5000

    private val HOST = "192.168.1.184"
    var PORT = 5000
//    private val HOST = "192.168.1.184"
//    var PORT = 5000

    private val HEX_UPPER = "0123456789ABCDEF".toCharArray()
    private val HEX_LOWER = "0123456789abcdef".toCharArray()

    val MS_key = "EB8F1A2986CF113175EC2102C26161BB"
//    val rsa: iRSA? = null
//    val dataConverter : DataConverter? = null

    val rsa = iRSA()
    val des = iDES()
    val dataConverter = DataConverter()

    ////
    var DEK : String?=null
    var MAK : String?=null
    var LTWK_ID : String?=null
    var masterKey : String?=null
    var masterKeyID : String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_key)
        tv_downloadKey = findViewById(R.id.tv_downloadKey)
        sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)
        val rsa_pubmod = sp.getString("rsa_publickey_mod",null)
        val rsa_pubexp = sp.getString("rsa_pubblickey_exp",null)
        val rsa_primod = sp.getString("rsa_privatekey_mod",null)
        val rsa_priexp = sp.getString("rsa_privatekey_exp",null)
        masterKey = sp.getString("MasterKey",null)
        masterKeyID = sp.getString("MasterKeyID",null)
        DEK = sp.getString("DEK",null)
        MAK = sp.getString("MAK",null)
        LTWK_ID = sp.getString("LTWK_ID",null)

        var ltmkBtn = findViewById<Button>(R.id.ltmkBtn)
        var ltwkBtn = findViewById<Button>(R.id.ltwkBtn)
        var btn_genRSA = findViewById<Button>(R.id.btn_genRSA)

        LTID = serialNumber()

        setTextView("LTID",LTID)
        setTextView("Master key",masterKey)
        setTextView("Master key ID",masterKeyID)
        setTextView("Working key MAK",MAK)
        setTextView("Working key DEK",DEK)
        setTextView("Working key ID",LTWK_ID)



        Log.e(log,"DEK :: "+DEK)
        Log.e(log,"MAK :: "+MAK)

        Log.i(log,"privateKey.modulus = "+rsa_primod)
        Log.i(log,"privateKey.privateExponent = "+rsa_priexp)
        Log.i(log,"publicKey.modulus = "+rsa_pubmod)
        Log.i(log,"publicKey.publicExponent = "+rsa_pubexp)

        Log.i(log,"privateKey.modulus = "+rsa_primod)
        Log.i(log,"privateKey.privateExponent = "+rsa_priexp)
        Log.i(log,"publicKey.modulus = "+rsa_pubmod)
        Log.i(log,"publicKey.publicExponent = "+rsa_pubexp)

        btn_genRSA.setOnClickListener {


            var rsa_privatekey_mod :String?=null
            var rsa_privatekey_exp :String?=null
            var rsa_publickey_mod :String?=null
            var rsa_pubblickey_exp :String?=null

            try {
                rsa.genKeyPair(2048)

                rsa_privatekey_mod = rsa.privateKey.modulus.toString(16).toUpperCase()
                rsa_privatekey_exp = rsa.privateKey.privateExponent.toString(16).toUpperCase()
                rsa_publickey_mod = rsa.publicKey.modulus.toString(16).toUpperCase()
                rsa_pubblickey_exp = rsa.publicKey.publicExponent.toString(16).toUpperCase()

                rsa_privatekey_mod = if (rsa_privatekey_mod.length%2!=0) '0'+rsa_privatekey_mod else rsa_privatekey_mod
                rsa_privatekey_exp = if (rsa_privatekey_exp.length%2!=0) '0'+rsa_privatekey_exp else rsa_privatekey_exp
                rsa_publickey_mod = if (rsa_publickey_mod.length%2!=0) '0'+rsa_publickey_mod else rsa_publickey_mod
                rsa_pubblickey_exp = if (rsa_pubblickey_exp.length%2!=0) '0'+rsa_pubblickey_exp else rsa_pubblickey_exp

                val editor: SharedPreferences.Editor = sp.edit()
                editor.putString("rsa_privatekey_mod", rsa_privatekey_mod)
                editor.putString("rsa_privatekey_exp", rsa_privatekey_exp)
                editor.putString("rsa_publickey_mod", rsa_publickey_mod)
                editor.putString("rsa_publickey_exp", rsa_pubblickey_exp)
                editor.commit()

                Log.i(log,"privateKey.modulus = "+rsa_privatekey_mod)
                Log.i(log,"privateKey.privateExponent = "+rsa_privatekey_exp)
                Log.i(log,"publicKey.modulus = "+rsa_publickey_mod)
                Log.i(log,"publicKey.publicExponent = "+rsa_pubblickey_exp)

            } catch (e: InvalidParameterException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: InvalidKeySpecException) {
                e.printStackTrace()
            } catch (e: InvalidAlgorithmParameterException) {
                e.printStackTrace()
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            } catch (e: IllegalBlockSizeException) {
                e.printStackTrace()
            } catch (e: BadPaddingException) {
                e.printStackTrace()
            } catch (e: NoSuchPaddingException) {
                e.printStackTrace()
            }
        }

        ltmkBtn.setOnClickListener{
            sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)
            val rsa_primod = sp.getString("rsa_privatekey_mod",null)
            val rsa_priexp = sp.getString("rsa_privatekey_exp",null)
            val rsa_pubmod = sp.getString("rsa_publickey_mod",null)
            val rsa_pubexp = sp.getString("rsa_publickey_exp",null)

            if ((rsa_pubexp!=null)&&(rsa_pubmod!=null)&&(rsa_priexp!=null)&&(rsa_primod!=null)){
                rsa.setPrivateKey(rsa_primod,rsa_priexp)
                rsa.setPublicKey(rsa_pubmod,rsa_pubexp)
                ltmkState = true
                stan = stan.plus(1)
                txnHash = TXN_Hash(TE_ID,TE_PIN,LTID,stan.toString())
                Log.i(log,"exp = "+rsa_pubexp+"\nmod = "+rsa_pubmod)
                strBit62Ltmk = bit62Ltmk(indicator,version,downlondType,reqType,acqID,LTID,vendorID,TE_ID,txnHash,rsa_pubexp,rsa_pubmod)
                Log.e(log,"txnHash: " + txnHash)
                Log.e(log,"bit62Ltmk: " + strBit62Ltmk)
                Log.e(log, "send ltmk")
                Log.e(log, "ltmk msg: " + ltmkPacket())
                sendPacket(ltmkPacket())

            }
            else{
                Log.i(log,"Don't have RSA,please gen RSA")
            }
        }

        ltwkBtn.setOnClickListener{
            Log.e(log,"testMasterKeyID :: "+masterKeyID)
            ltwkState = true
            stan = stan.plus(1)
            strBit62Ltwk = bit62Ltwk(indicator,version,reqType,acqID,acqID,LTID,vendorID,masterKeyID!!,ltwkId)
            Log.e(log,"bit62Ltwk: " + strBit62Ltwk)
            Log.e(log, "send ltwk")
            Log.w(log, "ltwk msg: " + ltwkPacket())
            sendPacket(ltwkPacket())
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

    }

    override fun onResume() {
        super.onResume()

    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    val SEND_MESSAGE = "send_message"
    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessageEvent(event: MessageEvent){

        if (event.type == "iso_response") {
            manageResponse(event)
        }


    }

    fun manageResponse(event: MessageEvent){

        Log.i(log, "Response Message:" + event.message)
        var responseMsg = event.message
        responseCode = codeUnpack(responseMsg,39).toString()
        Log.e(log, "response code:"+ responseCode)

        if(responseCode == "3030"){

            if(ltmkState == true){

                setNormalDialog("","Download Master Key Success.")
                var bit62Msg = codeUnpack(responseMsg,62).toString()
                Log.e(log,"bit62: " + bit62Msg)

                Log.i(log,"test len = "+bit62Msg.length)
                var getKeyIdMsg = getKeyId(bit62Msg)
                var getKeyKCVMsg = getKeyKCV(bit62Msg)
                var getMasterKey = dataConverter.HexByteToHexString(rsa.deRSA_private(dataConverter.HexString2HexByte(get_eKey(bit62Msg))))


                if (ChackKCV(getKeyKCVMsg,getMasterKey) == true){
                    masterKey = getMasterKey
                    masterKeyID = getKeyIdMsg
                    val editor: SharedPreferences.Editor = sp.edit()
                    editor.putString("MasterKey", getMasterKey)
                    editor.putString("MasterKeyID",getKeyIdMsg)
                    editor.commit()
                }else{
                    setNormalDialog("error","Wrong KCV")
                }
                Log.w(log,"resr_geteKey_func: "+ getMasterKey)
                Log.w(log,"test getKeyId func: " + getKeyIdMsg)
                Log.w(log,"test getKeyKCV func: " + getKeyKCVMsg)

                view = ""
                setTextView("LTID",LTID)
                setTextView("Master key",masterKey)
                setTextView("Master key ID",masterKeyID)
                setTextView("Working key MAK",MAK)
                setTextView("Working key DEK",DEK)
                setTextView("Working key ID",LTWK_ID)

                ltmkState = false

            }else if(ltwkState == true){

                setNormalDialog("","Download Working Key Success.")
                var bit62Msg = codeUnpack(responseMsg,62).toString()
                Log.e(log,"bit62: " + bit62Msg)



                var DEK = des.deDESede(dataConverter.HexString2HexByte(masterKey),"DESede/CBC/NoPadding", dataConverter.HexString2HexByte(get_eDEK(bit62Msg)))
                var MAK = des.deDESede(dataConverter.HexString2HexByte(masterKey),"DESede/CBC/NoPadding", dataConverter.HexString2HexByte(get_eMAK(bit62Msg)))
                var KCV_MAK = if (get_KCV_DEK(bit62Msg)==dataConverter.HexByteToHexString(des.enDESede(MAK, "DESede/CBC/NoPadding", dataConverter.HexString2HexByte("0000000000000000"))).substring(0,8)) true else false
                var KCV_DEK = if (get_KCV_MAK(bit62Msg)==dataConverter.HexByteToHexString(des.enDESede(DEK, "DESede/CBC/NoPadding", dataConverter.HexString2HexByte("0000000000000000"))).substring(0,8)) true else false
                if (KCV_MAK&&KCV_DEK){
                    Log.w(log,"test get MAK func: " + dataConverter.HexByteToHexString(MAK))
                    Log.w(log,"test get DEK func: " + dataConverter.HexByteToHexString(DEK))
                    Log.w(log,"test get_KCV_MAK func: " + get_KCV_MAK(bit62Msg))
                    Log.w(log,"test get_KCV_DEK func: " + get_KCV_DEK(bit62Msg))
                    Log.w(log,"test get_LTWK_ID func: " + hexToString(get_LTMK_ID(bit62Msg)!!))

                    val editor: SharedPreferences.Editor = sp.edit()
                    editor.putString("DEK", dataConverter.HexByteToHexString(DEK))
                    editor.putString("MAK", dataConverter.HexByteToHexString(MAK))
                    editor.putString("LTWK_ID", hexToString(get_LTMK_ID(bit62Msg)!!))
                    editor.commit()

                    view = ""
                    setTextView("LTID",LTID)
                    setTextView("Master key",masterKey)
                    setTextView("Master key ID",masterKeyID)
                    setTextView("Working key MAK",dataConverter.HexByteToHexString(MAK))
                    setTextView("Working key DEK",dataConverter.HexByteToHexString(DEK))
                    setTextView("Working key ID",hexToString(get_LTMK_ID(bit62Msg)!!))
                    startActivity(Intent(this, MenuActivity::class.java))
                }

                ltwkState = false
            }
        }else{

            var errorMsg = hexToString(codeUnpack(responseMsg,63).toString())
            Log.e(log,"Download Key Error: " + errorMsg)
            setNormalDialog("Download Key Fail.",errorMsg)

        }

    }

    fun accessDatabase(){
        appDatabase = AppDatabase.getAppDatabase(this)
        saleDAO = appDatabase?.saleDao()
        responseDAO = appDatabase?.responseDao()

    }

    private fun ltwkPacket(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .networkManagement()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("970400")
            .setField(FIELDS.F11_STAN,stan.toString())
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(
                FIELDS.F41_CA_TerminalID,
                StringUtil.hexStringToByteArray(convertStringToHex(tid, false))
            )
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray(strBit62Ltwk))
            .setHeader("6001268001")
            .build()
    }
    private fun ltmkPacket(): ISOMessage {
        return ISOMessageBuilder.Packer(VERSION.V1987)
            .networkManagement()
            .setLeftPadding(0x00.toByte())
            .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
            .processCode("970000")
            .setField(FIELDS.F11_STAN,stan.toString())
            .setField(FIELDS.F24_NII_FunctionCode, "120")
            .setField(
                FIELDS.F41_CA_TerminalID,
                StringUtil.hexStringToByteArray(convertStringToHex(tid, false))
            )
            .setField(FIELDS.F62_Reserved_Private,hexStringToByteArray(strBit62Ltmk))
            .setHeader("6001268001")
            .build()
    }

    fun getKeyId(bit62 : String):String{
        var keyIdMsg = bit62.substring(546,554)
        var keyId:String = hexToString(keyIdMsg).toString()
        return keyId
    }
    fun ChackKCV(KCV:String,MasterKey:String):Boolean?{
        var kcv = des.enDESede(dataConverter.HexString2HexByte(MasterKey), "DESede/CBC/NoPadding", dataConverter.HexString2HexByte("0000000000000000"));
        return KCV == dataConverter.HexByteToHexString(kcv).substring(0,6)
    }

    fun getKeyKCV(bit62 : String):String{
        var keyKCVMsg = bit62.substring(534,546)
        var keyKCV:String = hexToString(keyKCVMsg).toString()
        return keyKCV
    }
    fun get_eKey(bit62: String) : String? {
        var eKey:String? = null
        if (bit62.length == 586){
            eKey = bit62.substring(22,bit62.length-52).toUpperCase()
        }
        return eKey
    }

    fun get_KCV_MAK(bit62:String):String?{
        val kcv = bit62.substring(86,102).toUpperCase()
        return hexToString(kcv)
    }
    fun get_KCV_DEK(bit62:String):String?{
        val kcv = bit62.substring(102,118).toUpperCase()
        return hexToString(kcv)
    }
    fun get_eDEK(bit62:String):String?{
        val dek = bit62.substring(22,54).toUpperCase()
        return dek
    }
    fun get_eMAK(bit62:String):String?{
        val mak = bit62.substring(54,86).toUpperCase()
        return mak
    }
    fun get_LTMK_ID(bit62:String):String?{
        val ltmk_id = bit62.substring(14,22).toUpperCase()
        return ltmk_id
    }


    fun bit62Ltmk(indicator:String,version:String,downlondType:String,requestType:String,acqID:String,LTID:String,vendorID:String,TEID:String,txnHash:String,rsaExp:String,rsaMod:String):String {

        var str = indicator + version + downlondType + requestType + acqID + LTID + vendorID + TEID + txnHash
        var strToHex = convertStringToHex(str,false)
        var buildBit62 = strToHex + rsaExp + rsaMod

        return buildBit62
    }

    fun bit62Ltwk(indicator:String,version:String,requestType:String,LTMKacqID:String,acqID:String,LTID:String,vendorID:String,ltmkId:String,ltwkId:String):String {

        var str = indicator + version + requestType + LTMKacqID + acqID + LTID + vendorID + ltmkId + ltwkId
        var strToHex: String = convertStringToHex(str,false).toString()

        return strToHex
    }

    fun sha1(str: String): ByteArray = MessageDigest.getInstance("SHA-1").digest(str.toByteArray(
        StandardCharsets.UTF_8
    ))


    fun TXN_Hash(TE_ID:String,TE_PIN:String,LITD:String,STAN:String):String{
        var pinHash:String  = ""
        var txnHash:String = ""
        pinHash = bytesArrayToHexString(sha1(TE_ID+TE_PIN+padding))?.uppercase(Locale.getDefault()) ?: String()
        pinHash = pinHash.substring(0,8)
        var STAN = "000000".substring(STAN.length) + STAN
        txnHash = bytesArrayToHexString(sha1(pinHash+LITD+STAN.substring(STAN.length-4,STAN.length)))?.uppercase(
            Locale.getDefault()) ?: String()
        Log.e(log,"convert to hash: " + txnHash)
        return txnHash.substring(0,8)
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
                    Log.e(log, "Read Timeout")
                }

            } catch(err: ISOException){
                Log.e("log_tag", "error2 is ${err.message}")
            } catch (err: IOException){

                if (err.message!!.indexOf("ECONNREFUSED") > -1) {
                    Log.e(log, "connection fail.")

                }
            }
        }.start()
    }

    fun codeUnpack(response: String,field: Int): String? {
        val isoMessageUnpacket: ISOMessage = ISOMessageBuilder.Unpacker()
            .setMessage(response)
            .build()
        val responseCode: String? = bytesArrayToHexString(isoMessageUnpacket.getField(field))
        return responseCode
    }

    fun setNormalDialog(title: String?,msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
        builder.setPositiveButton(getString(R.string.ok),
            DialogInterface.OnClickListener{ dialog, which ->
//                Toast.makeText(applicationContext,android.R.string.ok, Toast.LENGTH_SHORT).show()
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

    fun convertStringToHex(str: String, lowercase: Boolean): String? {
        val HEX_ARRAY: CharArray = if (lowercase) HEX_LOWER else HEX_UPPER
        val bytes = str.toByteArray(StandardCharsets.UTF_8)

        // two chars form the hex value.
        val hex = CharArray(bytes.size * 2)
        for (j in bytes.indices) {

            // 1 byte = 8 bits,
            // upper 4 bits is the first half of hex
            // lower 4 bits is the second half of hex
            // combine both and we will get the hex value, 0A, 0B, 0C
            val v = (bytes[j] and 0xFF.toByte()).toInt() // byte widened to int, need mask 0xff
            // prevent sign extension for negative number
            hex[j * 2] = HEX_ARRAY[v ushr 4] // get upper 4 bits
            hex[j * 2 + 1] = HEX_ARRAY[v and 0x0F] // get lower 4 bits
        }
        return String(hex)
    }

    fun hexToString(hex: String): String? {
        val sb = java.lang.StringBuilder()
        val hexData = hex.toCharArray()
        var count = 0
        while (count < hexData.size - 1) {
            val firstDigit = Character.digit(hexData[count], 16)
            val lastDigit = Character.digit(hexData[count + 1], 16)
            val decimal = firstDigit * 16 + lastDigit
            sb.append(decimal.toChar())
            count += 2
        }
        return sb.toString()
    }

    fun setTextView(topic:String?,msg: String?){
        view = if(view==null) "" else view
        var msg = if(msg==null) "" else msg
        view = view+topic+" : "+msg+"\n"
        tv_downloadKey.setText(view)
        e(log,view!!)
    }
    fun serialNumber():String {
        var androidId: String = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
//        var androidId = "24215d325528a108"
        var sn = androidId.substring(androidId.length - 8)
        return sn
    }


}