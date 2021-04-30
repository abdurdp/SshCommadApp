package com.zerobracket.sshcommadapp.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    var retrofit: Retrofit? = null

    //main url//
    val apiClient: Retrofit?
        get() {
            if (retrofit == null) {
                val okHttpClient = OkHttpClient.Builder()
                        .connectTimeout(1, TimeUnit.MINUTES)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()
                retrofit = Retrofit.Builder() //main url//
                        .baseUrl("https://6088cfd3a6f4a30017426f6c.mockapi.io/api/v1/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(okHttpClient)
                        .build()
            }
            return retrofit
        }
}