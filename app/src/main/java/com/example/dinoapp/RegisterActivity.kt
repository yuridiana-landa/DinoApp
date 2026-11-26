package com.example.dinoapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class RegisterActivity : AppCompatActivity() {

    companion object {

        //Datos de la notificacion
        private const val CANAL_ID = "canal_general"
        private const val NOTIFICACION_ID = 2
        private const val CODIGO_PERMISO = 100

    }

    private lateinit var etCorreo : EditText
    private lateinit var etUsuario : EditText
    private lateinit var etPassword : EditText
    private lateinit var etConfirmar : EditText
    private lateinit var btnRegistrar : Button
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        val tvCrearCuenta = findViewById<TextView>(R.id.tvCrearCuenta)
        etCorreo = findViewById(R.id.etCorreo)
        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        etConfirmar = findViewById(R.id.etConfirmar)
        btnRegistrar = findViewById(R.id.btnRegistrar)

        dbHelper = DatabaseHelper(this)

        btnRegistrar.setOnClickListener {
            registrarUsuario()
        }

        tvCrearCuenta.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        crearCanalNoti()
    }

    private fun registrarUsuario () {
        val usuario = etUsuario.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmar = etConfirmar.text.toString().trim()

        if (usuario.isEmpty() || correo.isEmpty() || password.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmar) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        val resultado = dbHelper.registrarUsuario(usuario, correo, password)

        if (resultado) {
            //Si el resultado es verdadero
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
            mostrarNoti()
            finish()
        } else {
            Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show()
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
            .setContentText("Has registrado una cuenta exitosamente")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        //Mandar la notificacion
        NotificationManagerCompat
            .from(this)
            .notify(NOTIFICACION_ID, notificacion)

    }
}