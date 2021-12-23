package com.example.wipay_iot_shop.wipay_qr

import android.content.Context
import android.util.Log
import android.util.Log.e
import com.example.wipay_iot_shop.QRpaymentActivity
import com.example.wipay_iot_shop.wipay_qr.data.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Adapter {
    var access_token :String?=null
    var event:Event?=null
    fun getToken(amount :String){
        var amount = amount.toDouble()
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
//                    val param = input("B00000512S00000001T00000001","B00000512S00000001","EXT1234567890","100.00","THB")
//                    val test = "{\"mutation\":{\"generateShopQrCode\":{\"input\":{\"amount\":\"100.00\",\"currency\":\"THB\",\"reference1\":\"B00000512S00000001\",\"reference2\":\"EXT1234567890\",\"terminalRefId\":\"B00000512S00000001T00000001\"}}}}"
                    val test2="{\"mutation\"{\"generateShopQrCode\"(\"input\":\"{\"terminalRefId\":\"B00000512S00000001T00000001\",\"reference1\":\"B00000512S00000001\",\"reference2\":\"EXT1234567890\",\"amount\":\"$amount\",\"currency\":\"THB\",\"}\")\",\"{\"qrCode\"}\"}\""
                    val graphQL = "mutation {generateShopQrCode(input:{terminalRefId:\"B00000512S00000001T00000001\"reference1:\"B00000512S00000001\"reference2:\"EXT1234567890\"amount:$amount currency:THB}){qrCode}}"
                    val genQRcode = GenQRcode(graphQL)
                    genQR(access_token!!,genQRcode)
                } else {
                }
            }
        })
    }

    fun genQR(token:String, param:GenQRcode ){

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
                        Log.e("TESTTEST", response.body()!!.data.generateShopQrCode.qrCode.toString())
                        event!!.onGenQRcode(response.body()!!.data.generateShopQrCode.qrCode)
//                    access_token = response.body()!!.access_token.toString()
                    } else {

                    }
                }
            })
        }

    }

    fun setProvisioningEvent(event: Event) {
        this.event = event
    }
    private fun totalamount(Totalamount : Double ):String{

        var amount : List<String> = String.format("%.2f",Totalamount).split(".")
        var Amount = amount[0]+amount[1]

        return Amount
    }
}