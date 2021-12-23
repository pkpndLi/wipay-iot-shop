package com.example.wipay_iot_shop.wipay_qr

import com.example.wipay_iot_shop.wipay_qr.data.*
import retrofit2.Call
import retrofit2.http.*

interface Service {
    @FormUrlEncoded
    @POST("services/wiid/api/v1/auth/oauth/token")
//    @Headers("Content-Type:application/x-www-form-urlencoded")
    fun getToken(
        @Header("Content-Type")ContentType:String,
        @Field("grant_type") grant_type:String,
        @Field("username") username:String,
        @Field("password") password:String,
        @Field("client_id") client_id:String,
        @Field("client_secret") client_secret:String
    ): Call<Token>

    @POST("services/wifrost/api/v1/ecom-app/graphql")
    @Headers ("Content-Type:application/json")
    fun genQR(
        @Header ("Authorization") Authorization:String,
        @Body query:String
    ): Call<QRcodeData>
}