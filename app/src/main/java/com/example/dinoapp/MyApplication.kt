package com.example.dinoapp

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import okhttp3.OkHttpClient

class MyApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestWithUserAgent = originalRequest.newBuilder()
                    .header("User-Agent", "DinoApp/1.0 (contacto@upemor.edu.mx)")
                    .build()
                chain.proceed(requestWithUserAgent)
            }
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .crossfade(true)
            .build()
    }
}