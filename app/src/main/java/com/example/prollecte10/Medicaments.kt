package com.example.prollecte10

import android.annotation.SuppressLint
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prollecte10.databinding.MedicamentsMainBinding
import java.io.File

class Medicaments : AppCompatActivity() {

    private lateinit var binding: MedicamentsMainBinding
    private var db: SQLiteDatabase? = null
    private var medicamentsCursor: Cursor? = null
    private var currentIndex: Int = 1
    private var totalRecords: Int = 0

    companion object {
        const val DATABASE_FOLDER = "Usser"
        private const val DATABASE_NAME = "MyDB"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MedicamentsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Conectar a la base de datos
        connectDatabase()

        // Contar registros
        if (db != null) {
            countRecords()
        }

        // Mostrar el primer registro
        if (medicamentsCursor != null && medicamentsCursor!!.moveToFirst()) {
            showCurrentMedicament()
        }

        // Configurar los botones de navegación
        binding.anteriorhospitalitzacio.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                medicamentsCursor?.moveToPosition(currentIndex)
                showCurrentMedicament()
            } else {
                Toast.makeText(this, "No hay registros anteriores", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDespues.setOnClickListener {
            if (currentIndex < totalRecords - 1) {
                currentIndex++
                medicamentsCursor?.moveToPosition(currentIndex)
                showCurrentMedicament()
            } else {
                Toast.makeText(this, "No hay más registros", Toast.LENGTH_SHORT).show()
            }
        }
        showCurrentMedicament()
    }

    private fun connectDatabase() {
        try {
            val folder = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), DATABASE_FOLDER)
            if (!folder.exists()) {
                folder.mkdirs()
            }

            val dbFile = File(folder, DATABASE_NAME)
            if (dbFile.exists()) {
                db = SQLiteDatabase.openDatabase(
                    dbFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READWRITE
                )
                Toast.makeText(this, "Conexión exitosa a la base de datos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "La base de datos no existe en la ruta: ${dbFile.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (ex: Exception) {
            Toast.makeText(this, "Error al conectar con la base de datos: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun countRecords() {
        val query = "SELECT COUNT(*) AS count FROM Medicament"
        try {
            val cursor = db?.rawQuery(query, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    totalRecords = it.getInt(it.getColumnIndexOrThrow("count"))
                    Toast.makeText(this, "Número de registros en Medicament: $totalRecords", Toast.LENGTH_LONG).show()
                }
            }
        } catch (ex: Exception) {
            Toast.makeText(this, "Error al contar los registros: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("Range")
    private fun showCurrentMedicament() {
        val query = "SELECT * FROM Medicament WHERE ID_Medicament = ?"
        val cursor = db?.rawQuery(query, arrayOf(currentIndex.toString()))

        cursor?.use {
            if (it.moveToFirst()) {
                // Obtener los datos del primer registro
                val nom = it.getString(it.getColumnIndex("Nom"))
                val marca = it.getString(it.getColumnIndex("Marca"))
                val formapresent = it.getString(it.getColumnIndex("Forma_presentacio"))
                val efects = it.getString(it.getColumnIndex("Efectes_Secundaris"))

                binding.tvNom.text = nom
                binding.tvMarca.text = marca
                binding.tvFormaPresentacio.text = formapresent
                binding.tvEfectesSecundaris.text = efects


            }
        }
        binding.tancar.setOnClickListener {
            finish()
        }
    }

}
