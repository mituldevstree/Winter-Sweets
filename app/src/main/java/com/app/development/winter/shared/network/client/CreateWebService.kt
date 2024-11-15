package com.app.development.winter.shared.network.client

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

fun <T> createWebService(
    baseUrl: String,
    clazz: Class<T>,
    okHttpClient: OkHttpClient,
): T {
    return Retrofit.Builder().baseUrl(baseUrl).client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create()).build().create(clazz)
}