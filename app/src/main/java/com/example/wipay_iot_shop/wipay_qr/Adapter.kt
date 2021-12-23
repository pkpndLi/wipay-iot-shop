package com.example.wipay_iot_shop.wipay_qr

import android.util.Log
import android.util.Log.e
import com.example.wipay_iot_shop.wipay_qr.data.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Adapter {
    var access_token :String?=null

    fun getToken(){
        var retroInstance =
            Instance.getRetroInstance().create(Service::class.java)
        val call = retroInstance.getToken("application/x-www-form-urlencoded","password","merchant1","merchant1","gamiqo-app","a5cb9f5b-faf7-4ec4-813f-72407901323a")
        call.enqueue(object : Callback<Token> {
            override fun onFailure(call: Call<Token>, t: Throwable) {

            }

            override fun onResponse(call: Call<Token>, response: Response<Token>) {
                if (response.isSuccessful) {
//                    recyclerListData.postValue(response.body())
                    Log.e("TESTTEST", response.body()!!.access_token.toString())
                    access_token = response.body()!!.access_token.toString()
//                    val param = "{\"input\":\"{\"terminalRefId\":\"B00000512S00000001T00000001\",\"reference1\":\"B00000512S00000001\",\"reference2\":\"EXT1234567890\",\"amount\":\"100.00\",\"currency\":\"THB\"}\"}"
                    val param = input("B00000512S00000001T00000001","B00000512S00000001","EXT1234567890","100.00","THB")
                    val test = "{\"mutation\":{\"generateShopQrCode\":{\"input\":{\"amount\":\"100.00\",\"currency\":\"THB\",\"reference1\":\"B00000512S00000001\",\"reference2\":\"EXT1234567890\",\"terminalRefId\":\"B00000512S00000001T00000001\"}}}}"
                    val test2="{\"query\":\"mutation\"{\"generateShopQrCode\"(\"input\":\"{\"terminalRefId\":\"B00000512S00000001T00000001\",\"reference1\":\"B00000512S00000001\",\"reference2\":\"EXT1234567890\",\"amount\":\"100.00\",\"currency\":\"THB\",\"}\")\",\"{\"qrCode\"}\"}\""
                    val generate = generateShopQrCode(param)
                    val gen = mutation(generate,null)
                    val GenQRcode = GenQRcode(gen)
                    genQR(access_token!!,test2)
                } else {

                }
            }
        })
    }

    fun genQR(token:String, param: String){

        Log.e("TESTTEST", param.toString())
        var retroInstance =
            Instance.getRetroInstance().create(Service::class.java)
        if (access_token!=null){
            val call = retroInstance.genQR("Bearer $token", param)
            call.enqueue(object : Callback<QRcodeData> {
                override fun onFailure(call: Call<QRcodeData>, t: Throwable) {
                    e("TESTTEST",t.toString())
                }

                override fun onResponse(call: Call<QRcodeData>, response: Response<QRcodeData>) {
                    if (response.isSuccessful) {
//                    recyclerListData.postValue(response.body())
                        Log.e("TESTTEST", response.body().toString())
//                    access_token = response.body()!!.access_token.toString()
                    } else {

                    }
                }
            })
        }

    }
}