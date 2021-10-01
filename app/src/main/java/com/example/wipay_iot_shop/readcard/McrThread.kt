package com.example.wipay_iot_shop.readcard

import android.util.Log
import com.example.wipay_iot_shop.readcard.data.DataMcr
import com.google.gson.Gson
import vpos.apipackage.PosApiHelper
import java.lang.Exception

class McrThread{
    var test = ""
    var gson = Gson()
    var posApiHelper = PosApiHelper.getInstance()
    var event: McrEvent? = null
    fun setMcrEvent(event: EmvEvent?) {
        this.event = event as McrEvent?
    }
    var track1 = ByteArray(250)
    var track2 = ByteArray(250)
    var track3 = ByteArray(250)
    fun mrcRead() {
        posApiHelper.McrOpen()
        var temp = -1
        while (temp != 0) {   //C1 05
            try {
                temp = posApiHelper.McrCheck()
                Log.e("liuhao", "Lib_McrCheck =$temp")
                //                Thread.sleep(200);
            } catch (e: Exception) { //InterruptedException
                e.printStackTrace()
            }
        }
        val ret = posApiHelper.McrRead(0.toByte(), 0.toByte(), track1, track2, track3)
        if (ret > 0) {
            var string = ""
            Log.e("liuhao", "ret = $ret")
            if (ret <= 7) {
                if (ret and 0x02 == 0x02) {
                    test = String(track2).trim { it <= ' ' }
                }
            } else {
                string = "Lib_MsrRead check data error"
            }

            Log.e("liuhao", "Lib_MsrRead succeed!")
            posApiHelper.SysBeep()
            Log.i("TESTTEST", test)
            posApiHelper.McrClose()

        } else {
            Log.e("liuhao", "Lib_MsrRead failed!")
        }
        val data : kotlin.Array<String>? = test?.split("=")?.toTypedArray()
        val cardNO = data?.get(0)
        val EXD: CharArray? = data?.get(1)?.toCharArray()
        var exd = ""
        for (i in 0..3) {
            exd += EXD?.get(i)
        }
        var Object = "{\"cardNO\": \"$cardNO\",\"cardEXD\": \"$exd\"}"
        var magTag: DataMcr = gson.fromJson<DataMcr>(Object, DataMcr::class.java)
        Log.i("test",magTag.cardNO+"\t"+magTag.cardEXD);
        Log.i("test",magTag.cardNO+"\t"+magTag.cardEXD);
        if (event != null)
        {
            event!!.onGetDataCardMagnetic(magTag)
        }
    }
}