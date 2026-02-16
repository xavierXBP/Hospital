package com.example.prollecte10

import android.annotation.SuppressLint
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.prollecte10.databinding.MalatiesMainBinding
import java.io.File

class Malalties : AppCompatActivity() {

    private var myFile: File? = null
    private var idPacient: Int = 0
    private lateinit var binding: MalatiesMainBinding
    private var db: SQLiteDatabase? = null
    private var filteredCursor: android.database.Cursor? = null
    private var id_malaltia: Int = 0
    private var currentIndex: Int = 0
    private var id_medicament: Int = 0
    private var currentName: String = "" // Nombre actual de búsqueda

    companion object {
        const val DATABASE_FOLDER = "Usser"
        private const val DATABASE_NAME = "MyDB"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MalatiesMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Conectar a la base de datos
        connectDatabase()

        binding.Nombre.doOnTextChanged { text, _, _, _ ->
            val name = text.toString().trim()
            if (name.isNotEmpty()) {
                currentName = name
                searchAllByName(name)  // Buscar por nombre
                Searchmalaltia(idPacient)
                Searchmedicament(id_medicament)
            } else {
                currentName = ""  // Restablecer el nombre
            }
        }
        binding.anteriormalaltia.setOnClickListener {
            moveToPreviousMalaltia() // Moverse al registro anterior de enfermedades
        }

        binding.seguentmalaltia .setOnClickListener {
            moveToNextMalaltia() // Moverse al siguiente registro de enfermedades
        }


        binding.seguent.setOnClickListener {
            moveToNextRecord()
        }
        binding.anterior.setOnClickListener {
            moveTopreviousRecord()
        }
        binding.dadespersonals.setOnClickListener {
            val intent = Intent(this, Dadespersonals::class.java)
            startActivity(intent)
        }
        binding.hospitalitzacion.setOnClickListener {
            val intent = Intent(this, Hospitalitzacions::class.java)
            startActivity(intent)
        }
        binding.alergies.setOnClickListener {
            val intent = Intent(this, Alergies::class.java)
            startActivity(intent)
        }
        binding.medicaments.setOnClickListener {
            val intent = Intent(this, Medicaments::class.java)
            startActivity(intent)
        }




    }

    @SuppressLint("Range")
    private fun searchAllByName(name: String) {
        if (name.isEmpty()) {
            // Limpiar si el nombre está vacío
            //clearFields()
            return
        }

        // Solo realizar la búsqueda si el nombre tiene más de una letra
        if (name.length > 1) {
            try {
                // Realizamos la búsqueda por el nombre, buscando coincidencias exactas
                val query = "SELECT * FROM Pacient WHERE Nom LIKE ?"
                filteredCursor = db?.rawQuery(query, arrayOf("%$name%"))
                currentIndex = 0  // Reiniciar el índice para navegar desde el primer resultado
                showCurrentRecord()  // Mostrar los resultados de la búsqueda

            } catch (e: Exception) {
                Toast.makeText(this, "Error al buscar por nombre: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            // Si el nombre tiene solo 1 letra, no hacer búsqueda, solo limpiar
        }
    }

    private fun connectDatabase() {
        try {
            val folder =
                File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), DATABASE_FOLDER)
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

    @SuppressLint("Range")
    private fun searchAllByMalaltia(id_malaltia: Int) {
        try {
            // Consulta para obtener el nombre y los síntomas de la enfermedad
            val query = "SELECT Nom, Simptomes FROM Malaltia WHERE ID_Malaltia = ?"
            val cursor = db?.rawQuery(query, arrayOf(id_malaltia.toString()))

            cursor?.use {
                if (it.moveToFirst()) {
                    // Obtener los datos de la enfermedad
                    val nomMalaltia = it.getString(it.getColumnIndex("Nom"))
                    val simptomesMalaltia = it.getString(it.getColumnIndex("Simptomes"))

                    // Mostrar los datos en los TextView correspondientes
                    binding.malaltian.text = nomMalaltia  // Ajusta con el ID correcto del TextView
                    binding.simptomes.text = simptomesMalaltia  // Ajusta con el ID correcto del TextView
                } else {
                    Toast.makeText(this, "No se encontró la enfermedad.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al buscar la enfermedad: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("Range")
    private fun showCurrentRecord() {
        filteredCursor?.let {
            if (it.moveToPosition(currentIndex)) {
                val nom = it.getString(it.getColumnIndex("Nom"))
                val cognoms = it.getString(it.getColumnIndex("Cognoms"))
                idPacient = it.getInt(it.getColumnIndex("ID_Pacient"))
                binding.NomCom.setText("$nom $cognoms")  // Mostrar el nombre y apellidos

                // Cargar los datos de malaltia para este paciente
                Searchmalaltia(idPacient)
            } else {
                Toast.makeText(this, "No se encontraron pacientes con ese nombre.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("Range")
    private fun Searchmalaltia(idPacient: Int) {
        val query = "SELECT * FROM Tractament_malalties WHERE ID_Pacient = ?"
        val cursor = db?.rawQuery(query, arrayOf(idPacient.toString()))

        cursor?.use {
            if (it.moveToFirst()) {
                id_malaltia = it.getInt(it.getColumnIndex("ID_Malaltia"))
                val iniciMalaltia = it.getString(it.getColumnIndex("inici_malaltia"))
                val fiMalaltia = it.getString(it.getColumnIndex("fi_malaltia"))
                val hores = it.getString(it.getColumnIndex("Hores"))
                id_medicament = it.getInt(it.getColumnIndex("ID_Medicament"))
                val dataInici = it.getString(it.getColumnIndex("data_inici"))
                val dataFi = it.getString(it.getColumnIndex("data_fi"))

                // Mostrar las fechas y horas en los TextView
                binding.dataInici.text = iniciMalaltia
                binding.dataFi.text = fiMalaltia
                binding.Hores.text = hores
                binding.inicitract.text = dataInici
                binding.fitract.text = dataFi

                // Calcular los días totales entre las fechas
                val totalDaysMalaltia = calculateDaysDifference(iniciMalaltia, fiMalaltia)
                val totalDaysTractament = calculateDaysDifference(dataInici, dataFi)

                // Mostrar los días calculados en los TextView correspondientes
                binding.tempsmalalt.text = "$totalDaysMalaltia"
                binding.totalmedicament.text = "$totalDaysTractament"

                // Buscar y mostrar detalles de la enfermedad y del medicamento
                searchAllByMalaltia(id_malaltia)
                Searchmedicament(id_medicament)
            } else {
                Toast.makeText(this, "No se encontraron tratamientos para este paciente.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Calcula la diferencia en días entre dos fechas en formato "YYYY-MM-DD".
     */
    private fun calculateDaysDifference(startDate: String?, endDate: String?): Int {
        if (startDate == null || endDate == null) return 0

        return try {
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val start = formatter.parse(startDate)
            val end = formatter.parse(endDate)

            if (start != null && end != null) {
                val differenceInMillis = end.time - start.time
                (differenceInMillis / (1000 * 60 * 60 * 24)).toInt() // Convertir milisegundos a días
            } else {
                0
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al calcular días: ${e.message}", Toast.LENGTH_SHORT).show()
            0
        }
    }

    @SuppressLint("Range")
    private fun Searchmedicament(id_medicament: Int) {
        val query = "SELECT * FROM Medicament WHERE ID_Medicament = ?"
        val cursor = db?.rawQuery(query, arrayOf(id_medicament.toString()))

        cursor?.use {
            if (it.moveToFirst()) {
                val nom = it.getString(it.getColumnIndex("Nom"))
                binding.medicamentn.text = nom
            } else {
                Toast.makeText(this, "No se encontraron tratamientos para este paciente.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun moveToNextRecord() {
        filteredCursor?.let {
            if (it.moveToNext()) {
                currentIndex++
                showCurrentRecord()
            } else {
                Toast.makeText(this, "No hay más registros", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun moveTopreviousRecord() {
        filteredCursor?.let {
            if (it.moveToPrevious()) {
                currentIndex--
                showCurrentRecord()
            } else {
                Toast.makeText(this, "No hay más registros", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("Range")
    private fun moveToNextMalaltia() {
        val query = "SELECT * FROM Tractament_malalties WHERE ID_Pacient = ?"
        val cursor = db?.rawQuery(query, arrayOf(idPacient.toString()))

        cursor?.use {
            if (it.moveToPosition(++currentIndex)) {
                id_malaltia = it.getInt(it.getColumnIndex("ID_Malaltia"))
                val iniciMalaltia = it.getString(it.getColumnIndex("inici_malaltia"))
                val fiMalaltia = it.getString(it.getColumnIndex("fi_malaltia"))
                val hores = it.getString(it.getColumnIndex("Hores"))
                id_medicament = it.getInt(it.getColumnIndex("ID_Medicament"))
                val dataInici = it.getString(it.getColumnIndex("data_inici"))
                val dataFi = it.getString(it.getColumnIndex("data_fi"))

                // Mostrar datos de la enfermedad actual
                binding.dataInici.text = iniciMalaltia
                binding.dataFi.text = fiMalaltia
                binding.Hores.text = hores
                binding.inicitract.text = dataInici
                binding.fitract.text = dataFi

                // Calcular y mostrar los días totales
                val totalDaysMalaltia = calculateDaysDifference(iniciMalaltia, fiMalaltia)
                val totalDaysTractament = calculateDaysDifference(dataInici, dataFi)
                binding.tempsmalalt.text = "$totalDaysMalaltia"
                binding.totalmedicament.text = "$totalDaysTractament"

                // Buscar detalles de la enfermedad y medicamento
                searchAllByMalaltia(id_malaltia)
                Searchmedicament(id_medicament)
            } else {
                currentIndex-- // Volver al índice anterior si no hay más registros
                Toast.makeText(this, "No hay más enfermedades.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("Range")
    private fun moveToPreviousMalaltia() {
        val query = "SELECT * FROM Tractament_malalties WHERE ID_Pacient = ?"
        val cursor = db?.rawQuery(query, arrayOf(idPacient.toString()))

        cursor?.use {
            if (it.moveToPosition(--currentIndex)) {
                id_malaltia = it.getInt(it.getColumnIndex("ID_Malaltia"))
                val iniciMalaltia = it.getString(it.getColumnIndex("inici_malaltia"))
                val fiMalaltia = it.getString(it.getColumnIndex("fi_malaltia"))
                val hores = it.getString(it.getColumnIndex("Hores"))
                id_medicament = it.getInt(it.getColumnIndex("ID_Medicament"))
                val dataInici = it.getString(it.getColumnIndex("data_inici"))
                val dataFi = it.getString(it.getColumnIndex("data_fi"))

                // Mostrar datos de la enfermedad actual
                binding.dataInici.text = iniciMalaltia
                binding.dataFi.text = fiMalaltia
                binding.Hores.text = hores
                binding.inicitract.text = dataInici
                binding.fitract.text = dataFi

                // Calcular y mostrar los días totales
                val totalDaysMalaltia = calculateDaysDifference(iniciMalaltia, fiMalaltia)
                val totalDaysTractament = calculateDaysDifference(dataInici, dataFi)
                binding.tempsmalalt.text = "$totalDaysMalaltia"
                binding.totalmedicament.text = "$totalDaysTractament"

                // Buscar detalles de la enfermedad y medicamento
                searchAllByMalaltia(id_malaltia)
                Searchmedicament(id_medicament)
            } else {
                currentIndex++ // Volver al índice siguiente si no hay más registros
                Toast.makeText(this, "No hay más enfermedades.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.dadespersonals.setOnClickListener {
            val intent = Intent(this, Dadespersonals::class.java)
            startActivity(intent)

        }
        binding.hospitalitzacion.setOnClickListener {
            val intent = Intent(this, Hospitalitzacions::class.java)
            startActivity(intent)

        }
        binding.medicamentn.setOnClickListener {
            val intent = Intent(this, Medicaments::class.java)
            startActivity(intent)

        }
        binding.alergies.setOnClickListener {
            val intent = Intent(this, Alergies::class.java)
            startActivity(intent)
        }
    }


}
