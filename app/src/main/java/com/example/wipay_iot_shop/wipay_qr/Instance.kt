package com.example.wipay_iot_shop.wipay_qr

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Instance {
    companion object {


        val BASE_URL = "https://dev-wi-platform.wipay.dev/ "
        fun getRetroInstance(): Retrofit {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder()
            client.addInterceptor(logging)

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}