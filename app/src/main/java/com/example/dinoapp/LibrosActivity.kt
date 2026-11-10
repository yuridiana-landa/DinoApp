package com.example.dinoapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.dinoapp.modelo.WikiResponse
import com.example.dinoapp.network.WikipediaApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LibrosActivity : AppCompatActivity() {

    private lateinit var imgDino: ImageView
    private lateinit var txtNombre: TextView
    private lateinit var txtInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_libros)

        imgDino = findViewById(R.id.imgDino)
        txtNombre = findViewById(R.id.txtNombre)
        txtInfo = findViewById(R.id.txtInfo)

        val dinosaurName = intent.getStringExtra("EXTRA_DINOSAUR_NAME") ?: "Tiranosaurio"

        obtenerInfoDinosaurio(dinosaurName)
    }

    private fun obtenerInfoDinosaurio(name: String) {
        val apiService = WikipediaApiService.create()

        apiService.getDinosaurInfo(name).enqueue(object : Callback<WikiResponse> {
            override fun onResponse(call: Call<WikiResponse>, response: Response<WikiResponse>) {
                if (response.isSuccessful) {
                    val wikiResponse = response.body()

                    if (wikiResponse != null) {
                        txtNombre.text = wikiResponse.title
                        txtInfo.text = wikiResponse.extract

                        val imageUrl = wikiResponse.thumbnail?.source ?: ""
                        if (imageUrl.isNotEmpty()) {
                            Glide.with(this@LibrosActivity)
                                .load(imageUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_dialog_alert)
                                .centerCrop()
                                .into(imgDino)
                        } else {
                            imgDino.setImageResource(android.R.drawable.ic_menu_gallery)
                        }
                    }
                } else {
                    txtInfo.text = "No se encontró información detallada para este dinosaurio."
                }
            }

            override fun onFailure(call: Call<WikiResponse>, t: Throwable) {
                txtInfo.text = "Error de conexión: ${t.message}"
            }
        })
    }
}