package com.example.teachersteps

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import api.client.Responses
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object Client {

    private const val BASE_URL = "https://echo-api-senac.vercel.app/"

    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .registerTypeAdapter(
                Responses.Address::class.java,
                PaymentActivity.EnderecoDeserializer()
            )
            .create()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }


    private val retrofitScalar: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }

    fun <T> createServiceScalar(serviceClass: Class<T>): T {
        return retrofitScalar.create(serviceClass)
    }
}

