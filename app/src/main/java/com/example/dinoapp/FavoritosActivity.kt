package com.example.dinoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.dinoapp.modelo.DinoFavorito
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class FavoritosActivity : AppCompatActivity() {

    /*
    * AGREGAR MAS LIBROS A LA APP SIN NECESIDAD DE PONERLOS POR NOMBRE, SINO POR ERA
    * VERIFICAR PORQUE NO GUARDA IMAGENES EN FAVORITOS
    * REVISAR PORQUE NO HAY DATOS DEL DINOSAURIO EN LA BASE DE DATOS
    * REVISAR PORQUE EL NOMBRE DE USUARIO CAMBIA CUANDO SE METE A FAVORITOS Y SALE OTRA VEZ
    * */

    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var auth : FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    private lateinit var tvNombre: TextView
    private lateinit var tvEstado: TextView
    private lateinit var gridFavoritos: GridLayout
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favoritos)

        tvEstado = findViewById(R.id.tvEstado)
        gridFavoritos = findViewById(R.id.gridFavoritos)
        dbHelper = DatabaseHelper(this)

        tvNombre = findViewById(R.id.tvNombre)
        val nombreUsuario = intent.getStringExtra("EXTRA_USER_NAME") ?: "Usuario"
        tvNombre.text = nombreUsuario

        bottomNavigation = findViewById(R.id.bottomNavigation)

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        menuNavegacion()
        consultarFavoritos()

    }

    //Actualizacion inmediata de la pantalla
    override fun onResume() {
        super.onResume()
        consultarFavoritos()
    }

    private fun menuNavegacion() {

        //Indicar cual es el item seleccionado
        bottomNavigation.selectedItemId = R.id.nav_inicio

        //Indicar cual es el item al que se va a ir el usuario
        bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_inicio -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_favoritos -> {
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
                Toast.makeText(this@FavoritosActivity, "Error al cerrar sesion", Toast.LENGTH_SHORT).show()
            }

        }

        val intent = Intent(this, MainActivity::class.java)

        //Indicar que se borre toda la info y la app comience desde 0
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

    }

    //Metodo para mostrar los libros
    private fun mostrarLibrosFavoritos( favoritos : List<DinoFavorito> ) {

        for (dino in favoritos) {

            val vistaDino = layoutInflater.inflate(
                R.layout.item_libro_favorito,
                gridFavoritos,
                false
            )

            val imagen = vistaDino.findViewById<ImageView>(R.id.imgFavoritoLibro)

            Glide.with(this)
                .load(dino.imagen)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .centerCrop()
                .into(imagen)

            vistaDino.setOnClickListener {

                val intent = Intent(this, LibrosActivity::class.java)
                intent.putExtra("EXTRA_DINOSAUR_NAME", dino.nombre)
                startActivity(intent)

            }

            gridFavoritos.addView(vistaDino)

        }

    }

    //Metodo de consulta de informacion
    private fun consultarFavoritos() {

        val favoritos = dbHelper.obtenerDinoFavorito()

        //Eliminar vistas repetidas
        gridFavoritos.removeAllViews()

        if (favoritos.isEmpty()) {
            tvEstado.text = "No tienes Dinosaurios Favoritos, agrega uno!!! :("
            tvEstado.visibility = View.VISIBLE
        } else {
            tvEstado.visibility = View.GONE
            mostrarLibrosFavoritos(favoritos)
        }

    }

}