package com.example.dinoapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.dinoapp.modelo.DinoFavorito

class DatabaseHelper(context : Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "dinoapp.db"
        private const val DATABASE_VERSION = 2

        //tabla
        private const val TABLE_USUARIOS = "usuarios"

        //columnas
        private const val COL_ID = "id"
        private const val COL_USUARIO = "usuario"
        private const val COL_CORREO = "correo"
        private const val COL_PASSWORD = "password"

        //Tabla para guardar los favoritos
        private const val TABLE_FAVORITOS = "favoritos"
        private const val COL_ID_FAVORITO = "id"
        private const val COL_ID_DINO = "dino_id"
        private const val COL_IMAGEN = "imagen"
        private const val COL_NOMBRE = "nombre"
        private const val COL_INFO = "informacion"

        private const val CREATE_TABLE_FAVORITOS = """
            CREATE TABLE $TABLE_FAVORITOS (
                $COL_ID_FAVORITO INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ID_DINO INTEGER UNIQUE NOT NULL,
                $COL_IMAGEN TEXT,
                $COL_NOMBRE TEXT NOT NULL,
                $COL_INFO TEXT NOT NULL
            )
        """

        private const val CREATE_TABLE_USUARIOS = """
            CREATE TABLE $TABLE_USUARIOS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USUARIO TEXT NOT NULL UNIQUE,
                $COL_CORREO TEXT NOT NULL UNIQUE,
                $COL_PASSWORD TEXT NOT NULL
            )
        """
    }

    //Metodo para crear la base de datos y sus tablas
    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL(CREATE_TABLE_USUARIOS)
        p0?.execSQL(CREATE_TABLE_FAVORITOS)
    }

    //Metodo para actualizar la base de datos
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        // Validar la version actual de la base de datos
        if(p1 < 2){
            p0?.execSQL(CREATE_TABLE_FAVORITOS)
        }
    }

    //Metodo para registrar un usuario (Insertar)
    fun registrarUsuario(usuario: String, correo: String, password: String): Boolean {

        //Crear variable con permisos para escribir en la BD
        val db = writableDatabase

        //Generar variable para guardar la informacion
        val datos = ContentValues().apply {
            put(COL_USUARIO, usuario)
            put(COL_CORREO, correo)
            put(COL_PASSWORD, password)
        }

        val resultado = db.insert(TABLE_USUARIOS, null, datos)

        return resultado != -1L
    }

    //Metodo para consulta de usuarios
    fun iniciarSesion(identificador: String, password: String) : Boolean {
        val db = readableDatabase

        val query_sql = """
        SELECT * FROM $TABLE_USUARIOS
        WHERE ($COL_CORREO = ? OR $COL_USUARIO = ?) AND $COL_PASSWORD = ?
        """.trimIndent()

        val cursor = db.rawQuery(
            query_sql, arrayOf(identificador, identificador, password)
        )

        // Contar el número de resultados encontrados
        val usuarioEncontrado = cursor.count > 0

        cursor.close()
        db.close()

        return usuarioEncontrado
    }

    fun obtenerNombreUsuario(identificador: String): String? {
        val db = readableDatabase
        val query = "SELECT $COL_USUARIO FROM $TABLE_USUARIOS WHERE $COL_CORREO = ? OR $COL_USUARIO = ?"
        val cursor = db.rawQuery(query, arrayOf(identificador, identificador))

        var user: String? = null
        if (cursor.moveToFirst()) {
            user = cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO))
        }

        cursor.close()
        db.close()
        return user
    }

    //Metodo para insertar un registro desde la api
    fun guardarDinoFavorito(idDino : Long, imagen : String, nombre : String, info : String) : Boolean {
        val db = writableDatabase

        val datos = ContentValues().apply {
            put(COL_ID_DINO, idDino)
            put(COL_IMAGEN, imagen)
            put(COL_NOMBRE, nombre)
            put(COL_INFO, info)
        }

        //Si hay errores lo ignara e inserta
        val resultado = db.insertWithOnConflict(
            TABLE_FAVORITOS,
            null,
            datos,
            SQLiteDatabase.CONFLICT_IGNORE)

        return resultado != -1L
    }

    //Metodo para eliminar un registro
    fun eliminarDinoFavorito(idDino: Long) : Boolean {
        val db = writableDatabase

        val resultado = db.delete(
            TABLE_FAVORITOS,
            "$COL_ID_DINO = ?",
            arrayOf(idDino.toString())
        )

        db.close()
        return resultado > 0
    }

    //Metodo para consular la informacion del api
    fun existeDinoFav(idDino: Long) : Boolean {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT $COL_ID_FAVORITO FROM $TABLE_FAVORITOS WHERE $COL_ID_DINO = ?",
            arrayOf(idDino.toString())
        )

        val existe = cursor.moveToFirst()

        cursor.close()

        return existe
    }

    //Metodo para consultar toda la informacion del api
    fun obtenerDinoFavorito() : List<DinoFavorito> {

        //Crear lista
        val listaFavorito = mutableListOf<DinoFavorito>()
        val db = readableDatabase

        val query_sql = """
            SELECT 
                $COL_ID_FAVORITO,
                $COL_ID_DINO,
                $COL_IMAGEN,
                $COL_NOMBRE,
                $COL_INFO
            FROM $TABLE_FAVORITOS
            ORDER BY $COL_ID_FAVORITO DESC
        """.trimIndent()

        //Crear cursor para acceder a la informacion
        val cursor = db.rawQuery(query_sql, null)

        //Verificar si hubo resultados
        if(cursor.moveToFirst()){

            //Ciclo para recorrer la informacion
            do {

                val dino = DinoFavorito(
                    dinoID = cursor.getLong(0),
                    imagen = cursor.getString(1) ?: "",
                    nombre = cursor.getString(2) ?: "Dino desconocido",
                    info = cursor.getString(3) ?: "Informacion desconocida"
                )

                //Guardar variable como un elemento mas de la lista
                listaFavorito.add(dino)

            } while (cursor.moveToNext())

        }

        cursor.close()
        return listaFavorito

    }

}