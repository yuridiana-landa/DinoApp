package com.example.dinoapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.dinoapp.modelo.WikiResponse
import com.example.dinoapp.network.WikipediaApiService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var tvNombre: TextView
    private lateinit var contenedorTriasico: LinearLayout
    private lateinit var contenedorJurasico: LinearLayout
    private lateinit var contenedorCretacico: LinearLayout

    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var auth : FirebaseAuth
    private lateinit var credentialManager: CredentialManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        tvNombre = findViewById(R.id.tvNombre)
        val nombreUsuario = intent.getStringExtra("EXTRA_USER_NAME") ?: "Usuario"
        tvNombre.text = nombreUsuario

        contenedorTriasico = findViewById(R.id.contenedorTriasico)
        contenedorJurasico = findViewById(R.id.contenedorJurasico)
        contenedorCretacico = findViewById(R.id.contenedorCretacico)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        // Realizamos las 3 consultas múltiples por cada era geológica
        cargarEra(listOf("Coelophysis", "Plateosaurus", "Herrerasaurus"), contenedorTriasico)
        cargarEra(listOf("Diplodocus", "Brachiosaurus", "Stegosaurus"), contenedorJurasico)
        cargarEra(listOf("Tiranosaurio", "Triceratops", "Velociraptor"), contenedorCretacico)

        menuNavegacion()

    }

    private fun cargarEra(nombresDinos: List<String>, contenedor: LinearLayout) {
        contenedor.removeAllViews()

        for (nombre in nombresDinos) {
            WikipediaApiService.create().getDinosaurInfo(nombre).enqueue(object : Callback<WikiResponse> {
                override fun onResponse(call: Call<WikiResponse>, response: Response<WikiResponse>) {
                    val dino = response.body()

                    if (dino != null) {
                        val vistaDino = layoutInflater.inflate(
                            R.layout.item_libro,
                            contenedor,
                            false
                        )

                        val imgLibro = vistaDino.findViewById<ImageView>(R.id.imgLibro)
                        val txtNombreLibro = vistaDino.findViewById<TextView>(R.id.txtNombreLibro)

                        txtNombreLibro.text = dino.title

                        val imageUrl = dino.thumbnail?.source ?: ""
                        Glide.with(this@HomeActivity)
                            .load(imageUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_dialog_alert)
                            .centerCrop()
                            .into(imgLibro)

                        vistaDino.setOnClickListener {
                            val intent = Intent(this@HomeActivity, LibrosActivity::class.java)
                            intent.putExtra("EXTRA_DINOSAUR_NAME", dino.title)
                            startActivity(intent)
                        }

                        contenedor.addView(vistaDino)
                    }
                }

                override fun onFailure(call: Call<WikiResponse>, t: Throwable) {
                    Toast.makeText(this@HomeActivity, "Error al cargar $nombre", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    //Metodo para usar el menu de navegacion
    private fun menuNavegacion() {

        //Indicar cual es el item seleccionado
        bottomNavigation.selectedItemId = R.id.nav_inicio

        //Indicar cual es el item al que se va a ir el usuario
        bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_inicio -> {
                    true
                }

                R.id.nav_favoritos -> {
                    val intent = Intent(this, FavoritosActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_salir -> {
                    cerrarSesionGoogle()
                    true
                }

                else -> false
            }
        }

    }

    //Metodo para cerrar sesion
    private fun cerrarSesionGoogle() {

        auth.signOut()

        lifecycleScope.launch {

            try {
                credentialManager.clearCredentialState(
                    androidx.credentials.ClearCredentialStateRequest()
                )
            } catch (e: Exception) {
                Toast.makeText(this@HomeActivity, "Error al cerrar sesion", Toast.LENGTH_SHORT).show()
            }

        }

        val intent = Intent(this, MainActivity::class.java)

        //Indicar que se borre toda la info y la app comience desde 0
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

    }
}