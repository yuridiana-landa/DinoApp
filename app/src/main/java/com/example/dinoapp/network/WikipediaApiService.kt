package com.example.dinoapp.network

import com.example.dinoapp.modelo.WikiResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface WikipediaApiService {

    @Headers("User-Agent: DinoApp/1.0 (contacto@upemor.edu.mx)")
    @GET("page/summary/{title}")
    fun getDinosaurInfo(@Path("title") title: String): Call<WikiResponse>

    companion object {
        private const val BASE_URL = "https://es.wikipedia.org/api/rest_v1/"

        fun create(): WikipediaApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(WikipediaApiService::class.java)
        }
    }
}