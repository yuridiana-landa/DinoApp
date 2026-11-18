package com.example.dinoapp

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var btnFavorito: ImageButton

    private var dinoFavorito = false
    private lateinit var dbHelper: DatabaseHelper
    private var dinosaurName: String = "Tiranosaurio"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_libros)

        imgDino = findViewById(R.id.imgDino)
        txtNombre = findViewById(R.id.txtNombre)
        txtInfo = findViewById(R.id.txtInfo)
        btnFavorito = findViewById(R.id.btnFavorito)

        dbHelper = DatabaseHelper(this)

        // Obtenemos el nombre del dinosaurio enviado por el Intent
        dinosaurName = intent.getStringExtra("EXTRA_DINOSAUR_NAME") ?: "Tiranosaurio"

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

                        // Generamos un ID único basado en el nombre del dinosaurio para la BD
                        val idDino = wikiResponse.title.hashCode().toLong()

                        // Verificamos si ya está en favoritos una vez cargada la info
                        configurarFavoritos(idDino)
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

    private fun configurarFavoritos(idDino: Long) {
        // Verificar si el dinosaurio se encuentra como favorito
        dinoFavorito = dbHelper.existeDinoFav(idDino)
        actualizarIcono()

        btnFavorito.setOnClickListener {
            if (dinoFavorito) {
                val eliminado = dbHelper.eliminarDinoFavorito(idDino)

                if (eliminado) {
                    dinoFavorito = false
                    Toast.makeText(this, "Dinosaurio eliminado de favoritos", Toast.LENGTH_SHORT).show()
                }
            } else {
                val guardado = dbHelper.guardarDinoFavorito(
                    idDino,
                    "",
                    txtNombre.text.toString(),
                    txtInfo.text.toString()
                )

                if (guardado) {
                    dinoFavorito = true
                    Toast.makeText(this, "Dinosaurio agregado a favoritos", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "El dinosaurio ya esta en favoritos", Toast.LENGTH_SHORT).show()
                }
            }
            actualizarIcono()
        }
    }

    private fun actualizarIcono() {
        if (dinoFavorito) {
            btnFavorito.setImageResource(R.drawable.favorito)
        } else {
            btnFavorito.setImageResource(R.drawable.no_favorito)
        }
    }
}