package com.example.prollecte10

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.prollecte10.databinding.DadespersonalsMainBinding
import com.example.prollecte10.databinding.HospitalitzacionsMainBinding
import com.example.prollecte10.databinding.InsertpacientMainBinding
import java.io.File

class Inserirpacients : AppCompatActivity() {
    private var myFile: File? = null
    private var db: SQLiteDatabase? = null
    private var filteredCursor: Cursor? = null


    companion object {
        private const val DATABASE_FOLDER = "Usser"
        private const val DATABASE_NAME = "MyDB"
    }

    // Declaración de ViewBinding
    private lateinit var binding: InsertpacientMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el diseño usando ViewBinding
        binding = InsertpacientMainBinding.inflate(layoutInflater)

        // Establecer el diseño como la vista de la actividad
        setContentView(binding.root)
        connectDatabase()
        binding.btnexit.setOnClickListener {
            finish()
        }

        binding.btninserir.setOnClickListener {
            val nom = binding.txtnom.text.toString()
            val cognoms = binding.txtCognom.text.toString()
            val dni = binding.txtDNI.text.toString()
            val dataNaixement = binding.txtDAta.text.toString()
            val ciutatResidencial = binding.txtCR.text.toString()
            val ciutatNaixement = binding.txtCN.text.toString()
            val carrer = binding.txtC.text.toString()
            val codiPostal = binding.txtCDP.text.toString()

            if (nom.isNotEmpty() && cognoms.isNotEmpty() && dni.isNotEmpty()) {
                val result = insertPacient(
                    nom,
                    cognoms,
                    dni,
                    dataNaixement,
                    ciutatResidencial,
                    ciutatNaixement,
                    carrer,
                    codiPostal
                )
                if (result != -1L) {
                    Toast.makeText(this, "Datos guardados con éxito. ID: $result", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun connectDatabase() {
        try {
            val folder = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), DATABASE_FOLDER)
            if (!folder.exists()) {
                folder.mkdirs()
            }

            myFile = File(folder, DATABASE_NAME)

            if (myFile?.exists() == true) {
                db = SQLiteDatabase.openDatabase(
                    myFile!!.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READWRITE
                )
                Toast.makeText(this, "Conexión exitosa a la base de datos", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    this,
                    "La base de datos no existe en la ruta: ${myFile?.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (ex: Exception) {
            Toast.makeText(
                this,
                "Error al conectar con la base de datos: ${ex.message}\nRuta: ${myFile?.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    private fun insertPacient(
        nom: String,
        cognoms: String,
        dni: String,
        dataNaixement: String,
        ciutatResidencial: String,
        ciutatNaixement: String,
        carrer: String,
        codiPostal: String
    ): Long {
        val values = ContentValues().apply {
            put("Nom", nom)
            put("Cognoms", cognoms)
            put("DNI", dni)
            put("Data_naixement", dataNaixement)
            put("Ciutat_Residencial", ciutatResidencial)
            put("Ciutat_Naixement", ciutatNaixement)
            put("Carrer", carrer)
            put("Codi_Postal", codiPostal)
        }

        // Retorna el ID del nuevo registro o -1 si hay un error
        return db?.insert("Pacient", null, values) ?: -1L
    }
    override fun onDestroy() {
        db?.close() // Cerrar la base de datos al destruir la actividad
        super.onDestroy()
    }
}
