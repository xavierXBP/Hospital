package com.example.prollecte10

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.prollecte10.databinding.AlergiesMainBinding
import java.io.File

class Alergies : AppCompatActivity() {

    private lateinit var binding: AlergiesMainBinding
    private var db: SQLiteDatabase? = null
    private var filteredCursor: Cursor? = null
    private var currentIndex: Int = 0 // Índice de alergias
    private var currentPatientIndex: Int = 0 // Índice de pacientes
    private var currentName: String = ""
    private var currentIdPacient: Int = 0
    private var alergiasList: MutableList<Pair<String, Int>> = mutableListOf() // Lista de alergias (descripcion, id_medicament)
    private var medicamentNameList: MutableList<String> = mutableListOf() // Nombres de medicamentos

    companion object {
        const val DATABASE_FOLDER = "Usser"
        private const val DATABASE_NAME = "MyDB"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AlergiesMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connectDatabase()

        // Buscar pacientes por nombre
        binding.nombre.doOnTextChanged { text, _, _, _ ->
            val name = text.toString().trim()
            if (name.isNotEmpty()) {
                currentName = name
                searchPacienteByName(name)
            }
        }
        binding.btnmedicament.setOnClickListener {
            val intent = Intent(this, Medicaments::class.java)
            startActivity(intent)
        }
        binding.btnDades.setOnClickListener {
            val intent = Intent(this, Dadespersonals::class.java)
            startActivity(intent)
        }
        binding.hospitalitzacio.setOnClickListener {
            val intent = Intent(this, Hospitalitzacions::class.java)
            startActivity(intent)
        }
        binding.medicaments.setOnClickListener {
            val intent = Intent(this, Medicaments::class.java)
            startActivity(intent)
        }

        // Botones de navegación
        binding.anteriorpacient.setOnClickListener { moveToPreviousPatient() }
        binding.seguentpacient.setOnClickListener { moveToNextPatient() }
        binding.anterioralergia.setOnClickListener { moveToPreviousAlergy() }
        binding.btnDespues.setOnClickListener { moveToNextAlergy() }
    }

    private fun connectDatabase() {
        try {
            val folder = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), DATABASE_FOLDER)
            if (!folder.exists()) folder.mkdirs()

            val myFile = File(folder, DATABASE_NAME)

            if (myFile.exists()) {
                db = SQLiteDatabase.openDatabase(myFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
                Toast.makeText(this, "Conexión exitosa a la base de datos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "La base de datos no existe en la ruta: ${myFile.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (ex: Exception) {
            Toast.makeText(this, "Error al conectar con la base de datos: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("Range")
    private fun searchPacienteByName(name: String) {
        try {
            val query = "SELECT * FROM Pacient WHERE Nom LIKE ?"
            filteredCursor = db?.rawQuery(query, arrayOf("%$name%"))

            if (filteredCursor != null && filteredCursor!!.moveToFirst()) {
                currentPatientIndex = 0
                currentIdPacient = filteredCursor!!.getInt(filteredCursor!!.getColumnIndex("ID_Pacient"))
                showCurrentPatient()
                loadAlergiesForPaciente(currentIdPacient)
            } else {
                Toast.makeText(this, "No se encontraron pacientes con el nombre '$name'", Toast.LENGTH_SHORT).show()
            }
        } catch (ex: Exception) {
            Toast.makeText(this, "Error al buscar pacientes: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("Range")
    private fun showCurrentPatient() {
        filteredCursor?.let {
            if (it.moveToPosition(currentPatientIndex)) {
                val nom = it.getString(it.getColumnIndex("Nom"))
                val cognoms = it.getString(it.getColumnIndex("Cognoms"))
                binding.nomComplet.text = "$nom $cognoms"
            }
        }
    }

    @SuppressLint("Range")
    private fun loadAlergiesForPaciente(idPaciente: Int) {
        try {
            val query = "SELECT Descripci, ID_Medicament FROM Alergies WHERE ID_Pacient = ?"
            val alergiesCursor = db?.rawQuery(query, arrayOf(idPaciente.toString()))

            if (alergiesCursor != null && alergiesCursor.moveToFirst()) {
                alergiasList.clear()
                medicamentNameList.clear()

                do {
                    val descripcion = alergiesCursor.getString(alergiesCursor.getColumnIndex("Descripci"))
                    val idMedicament = alergiesCursor.getInt(alergiesCursor.getColumnIndex("ID_Medicament"))

                    alergiasList.add(Pair(descripcion, idMedicament))
                    medicamentNameList.add(getMedicamentNameById(idMedicament))
                } while (alergiesCursor.moveToNext())

                currentIndex = 0
                showCurrentAlergy()
            } else {
                binding.descripcio.text = "No hay alergias para este paciente"
                binding.medicamenName.text = ""
            }
        } catch (ex: Exception) {
            Toast.makeText(this, "Error al obtener alergias: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("Range")
    private fun getMedicamentNameById(idMedicament: Int): String {
        return try {
            val query = "SELECT Nom FROM Medicament WHERE ID_Medicament = ?"
            val cursor = db?.rawQuery(query, arrayOf(idMedicament.toString()))

            if (cursor != null && cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndex("Nom")) ?: "Desconocido"
            } else {
                "Desconocido"
            }
        } catch (ex: Exception) {
            "Desconocido"
        }
    }

    private fun showCurrentAlergy() {
        if (currentIndex in alergiasList.indices) {
            val currentAlergy = alergiasList[currentIndex]
            binding.descripcio.text = currentAlergy.first
            binding.medicamenName.text = medicamentNameList[currentIndex]
        }
    }

    @SuppressLint("Range")
    private fun moveToNextPatient() {
        filteredCursor?.let {
            if (it.moveToNext()) {
                currentPatientIndex++
                showCurrentPatient()
                currentIdPacient = it.getInt(it.getColumnIndex("ID_Pacient"))
                loadAlergiesForPaciente(currentIdPacient)
            }
        }
    }

    @SuppressLint("Range")
    private fun moveToPreviousPatient() {
        filteredCursor?.let {
            if (it.moveToPrevious()) {
                currentPatientIndex--
                showCurrentPatient()
                currentIdPacient = it.getInt(it.getColumnIndex("ID_Pacient"))
                loadAlergiesForPaciente(currentIdPacient)
            }
        }
    }

    private fun moveToNextAlergy() {
        if (currentIndex < alergiasList.size - 1) {
            currentIndex++
            showCurrentAlergy()
        } else {
            Toast.makeText(this, "No hay más alergias", Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveToPreviousAlergy() {
        if (currentIndex > 0) {
            currentIndex--
            showCurrentAlergy()
        } else {
            Toast.makeText(this, "No hay alergias anteriores", Toast.LENGTH_SHORT).show()
        }
    }

}
