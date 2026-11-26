package com.example.dinoapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.dinoapp.modelo.WikiResponse
import com.example.dinoapp.network.WikipediaApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LibrosActivity : AppCompatActivity() {

    companion object {

        //Datos de la notificacion
        private const val CANAL_ID = "canal_general"
        private const val NOTIFICACION_ID = 1
        private const val CODIGO_PERMISO = 100

    }

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
        dinosaurName = intent.getStringExtra("EXTRA_DINOSAUR_NAME") ?: "Dino desconocido"

        obtenerInfoDinosaurio(dinosaurName)

        crearCanalNoti()

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
                    btnFavorito.setImageResource(R.drawable.no_favorito)
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
                    btnFavorito.setImageResource(R.drawable.favorito)
                    mostrarNoti()
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

    //Metodo para crear el canal de notificacion y ver los permisos
    private fun crearCanalNoti() {

        //Verificar la version de android del dispositivo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //Crear el canal de difusion
            val canal = NotificationChannel(
                CANAL_ID,
                "Notificaciones generales",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            canal.description = "Canal de notificaciones de DinoApp"

            val adminNotificacion = getSystemService(NotificationManager::class.java)

            adminNotificacion.createNotificationChannel(canal)

        }

    }

    //Metodo para construir la notificacion
    private fun mostrarNoti() {

        //Validar la version del dispositivo y que el usuario nos haya dado permiso
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            //Pedir permiso al usuario nuevamente
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                CODIGO_PERMISO
            )

            Toast.makeText(this, "Autoriza el permiso y vuelve a intentar", Toast.LENGTH_SHORT).show()

            return

        }

        //Configurar la notificacion
        val notificacion = NotificationCompat.Builder(this, CANAL_ID)
            .setSmallIcon(R.drawable.perfil)
            .setContentTitle("DinoApp")
            .setContentText("Has agregado un dinosaurio a favoritos")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        //Mandar la notificacion
        NotificationManagerCompat
            .from(this)
            .notify(NOTIFICACION_ID, notificacion)

    }

}