package com.example.dinoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class FavoritosActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var auth : FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favoritos)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        menuNavegacion()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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
}