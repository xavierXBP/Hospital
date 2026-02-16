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
import com.example.prollecte10.databinding.HospitalitzacionsMainBinding
import java.io.File

class Hospitalitzacions : AppCompatActivity() {

    private lateinit var binding: HospitalitzacionsMainBinding
    private var db: SQLiteDatabase? = null
    private var filteredCursor: Cursor? = null
    private var currentIndex: Int = 0
    private var currentIdPacient: Int = 0
    private var Registremax: Int = 0

    // Variables para manejar la navegación entre hospitalizaciones
    private var currentHospitalizationIndex: Int = 0
    private var maxHospitalizationRecords: Int = 0
    private var hospitalRecords: Cursor? = null

    // Variables para manejar la navegación entre tratamientos
    private var currentTreatmentIndex: Int = 0
    private var maxTreatmentRecords: Int = 0

    companion object {
        private const val DATABASE_FOLDER = "Usser"
        private const val DATABASE_NAME = "MyDB"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HospitalitzacionsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connectDatabase()

        // Buscar pacientes por nombre
        binding.nombre.doOnTextChanged { text, _, _, _ ->
            val nombre = text.toString().trim()
            if (nombre.isNotEmpty()) {
                searchPatientByName(nombre)
            }
        }

        // Navegación entre pacientes
        binding.anteriorpacient.setOnClickListener { moveToPreviousPatient() }
        binding.seguentpacient.setOnClickListener { moveToNextPatient() }

        // Navegación entre hospitalizaciones
        binding.anteriorhospitalitzacio.setOnClickListener { moveToPreviousHospitalization(); moveToPreviousTreatment() }
        binding.btnDespues.setOnClickListener { moveToNextTreatment(); moveToNextHospitalization() }

        // Ir a otras actividades
        binding.btnDades.setOnClickListener { finish() }


        binding.btnMedicament.setOnClickListener {
            val intent = Intent(this, Medicaments::class.java)
            startActivity(intent)
        }
        binding.btnDades.setOnClickListener {
            val intent = Intent(this, Dadespersonals::class.java)
            startActivity(intent)
        }
        binding.alergies.setOnClickListener {
            val intent = Intent(this, Alergies::class.java)
            startActivity(intent)
        }
        binding.btnmedicament.setOnClickListener {
            val intent = Intent(this, Medicaments::class.java)
            startActivity(intent)
        }
        binding.btnMedicament.setOnClickListener {
            val intent = Intent(this, Medicaments::class.java)
            startActivity(intent)
        }
    }


    private fun connectDatabase() {
        try {
            val folder = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), DATABASE_FOLDER)
            if (!folder.exists()) folder.mkdirs()

            val myFile = File(folder, DATABASE_NAME)

            if (myFile.exists()) {
                db = SQLiteDatabase.openDatabase(
                    myFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READWRITE
                )
                Toast.makeText(this, "Conexión exitosa a la base de datos", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    this,
                    "La base de datos no existe en la ruta: ${myFile.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (ex: Exception) {
            Toast.makeText(
                this,
                "Error al conectar con la base de datos: ${ex.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @SuppressLint("Range")
    private fun searchPatientByName(name: String) {
        try {
            // Consulta para buscar pacientes por nombre completo (Nom y Cognoms)
            val query = "SELECT * FROM Pacient WHERE Nom LIKE ? OR Cognoms LIKE ?"
            filteredCursor = db?.rawQuery(query, arrayOf("%$name%", "%$name%"))

            if (filteredCursor != null && filteredCursor!!.moveToFirst()) {
                currentIndex = 0
                currentIdPacient =
                    filteredCursor!!.getInt(filteredCursor!!.getColumnIndex("ID_Pacient"))
                showCurrentPatient()
                loadHospitalizationRecords(currentIdPacient)  // Cargar los registros de hospitalización del paciente
            } else {
                Toast.makeText(
                    this,
                    "No se encontraron pacientes con el nombre '$name'",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (ex: Exception) {
            Toast.makeText(this, "Error al buscar pacientes: ${ex.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

    @SuppressLint("Range")
    private fun showCurrentPatient() {
        filteredCursor?.let {
            if (it.moveToPosition(currentIndex)) {
                val nom = it.getString(it.getColumnIndex("Nom"))
                val cognoms = it.getString(it.getColumnIndex("Cognoms"))
                binding.nomComplet.text = "$nom $cognoms"
            }
        }
    }

    // Cargar los registros de hospitalización para un paciente
    @SuppressLint("Range")
    private fun loadHospitalizationRecords(idPacient: Int) {
        try {
            val query = "SELECT * FROM Hospitalitzacio WHERE ID_Pacient = ?"
            hospitalRecords = db?.rawQuery(query, arrayOf(idPacient.toString()))

            // Obtener la cantidad de registros de hospitalización
            maxHospitalizationRecords = hospitalRecords?.count ?: 0
            hospitalRecords?.moveToFirst() // Mover el cursor al primer registro

            // Si hay registros, mostrar el primero
            if (maxHospitalizationRecords > 0) {
                showHospitalizationDetails()  // Mostrar los detalles del primer registro
            } else {
                Toast.makeText(
                    this,
                    "Este paciente no tiene registros de hospitalización",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (ex: Exception) {
            Toast.makeText(
                this,
                "Error al cargar registros de hospitalización: ${ex.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Mostrar los detalles de la hospitalización actual
    @SuppressLint("Range")
    private fun showHospitalizationDetails() {
        hospitalRecords?.let {
            // Asegúrate de mover el cursor a la posición correcta
            if (it.moveToPosition(currentHospitalizationIndex)) {
                val nhospitalitzacio = it.getInt(it.getColumnIndex("Nhospitalitzacio"))
                val habitacio = it.getInt(it.getColumnIndex("Habitacio"))
                val dataInici = it.getString(it.getColumnIndex("Data_inici"))
                val dataFi = it.getString(it.getColumnIndex("Data_fi"))
                val duracioHospitalitzacio = it.getInt(it.getColumnIndex("Duracio_hospitalitzacio"))
                val motiu = it.getString(it.getColumnIndex("Motiu"))
                val idHospital = it.getInt(it.getColumnIndex("ID_Hospital"))
                val idMalaltia = it.getInt(it.getColumnIndex("ID_Malaltia"))

                // Mostrar los datos en los TextViews de la UI
                binding.txtNhospitalitzacio.text = nhospitalitzacio.toString()
                binding.txtH2.text = habitacio.toString()
                binding.txtDi.text = dataInici.toString()
                binding.txtDF.text = dataFi.toString()
                binding.txtTT.text = duracioHospitalitzacio.toString()
                binding.motiu.text = motiu

                // Obtener nombre del hospital
                val hospitalQuery = "SELECT Nom_Hospital FROM Hospital WHERE ID_Hospital = ?"
                val hospitalCursor = db?.rawQuery(hospitalQuery, arrayOf(idHospital.toString()))

                hospitalCursor?.use {
                    if (it.moveToFirst()) {
                        val nombreHospital = it.getString(it.getColumnIndex("Nom_Hospital"))
                        binding.txtH.text = nombreHospital
                    }
                }

                // Cargar los tratamientos relacionados con esta hospitalización
                loadTreatmentRecords(nhospitalitzacio)
            }
        }
    }

    // Cargar los registros de tratamiento para una hospitalización
    @SuppressLint("Range")
    private fun loadTreatmentRecords(idHospitalitzacio: Int) {
        try {
            // Consulta para obtener tratamientos relacionados con la hospitalización
            val query = "SELECT * FROM Tractament WHERE ID_Hospitalitzacio = ?"
            val treatmentCursor = db?.rawQuery(query, arrayOf(idHospitalitzacio.toString()))

            if (treatmentCursor != null && treatmentCursor.moveToFirst()) {
                // Número total de tratamientos
                maxTreatmentRecords = treatmentCursor.count

                // Mover el cursor al tratamiento actual
                treatmentCursor.moveToPosition(currentTreatmentIndex)

                // Obtener datos del tratamiento actual
                val idTractament = treatmentCursor.getInt(treatmentCursor.getColumnIndex("ID_Tractament"))
                val idMedicament = treatmentCursor.getInt(treatmentCursor.getColumnIndex("Medicament"))
                val horari = treatmentCursor.getString(treatmentCursor.getColumnIndex("Horari"))
                val datainici = treatmentCursor.getString(treatmentCursor.getColumnIndex("Data_inici"))
                val datafi = treatmentCursor.getString(treatmentCursor.getColumnIndex("Data_fi"))

                // Actualizar la UI con los datos del tratamiento
                binding.dataInici.text = datainici
                binding.dataFi.text = datafi
                binding.horari.text = horari
               // binding.medicament.text = "Tratamiento: $idTractament"

                // Consulta para obtener el nombre del medicamento desde la tabla Medicament
                val medicamentQuery = "SELECT Nom FROM Medicament WHERE id_Medicament = ?"
                val medicamentCursor = db?.rawQuery(medicamentQuery, arrayOf(idMedicament.toString()))

                medicamentCursor?.use {
                    if (it.moveToFirst()) {
                        // Obtener el nombre del medicamento y mostrarlo en la UI
                        val nomMedicament = it.getString(it.getColumnIndex("Nom"))
                        binding.medicament.text = "$nomMedicament"
                    } else {
                        // Manejo si no se encuentra el medicamento
                        binding.medicament.text = "Medicamento no encontrado"
                    }
                }
            } else {
                // Mensaje si no se encuentran tratamientos
                Toast.makeText(
                    this,
                    "No se encontraron tratamientos para esta hospitalización",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (ex: Exception) {
            // Manejo de errores en el proceso
            Toast.makeText(this, "Error al cargar tratamientos: ${ex.message}", Toast.LENGTH_LONG)
                .show()
        }
    }


    // Función para mover al siguiente tratamiento
    private fun moveToNextTreatment() {
        if (currentTreatmentIndex < maxTreatmentRecords - 1) {
            currentTreatmentIndex++  // Incrementamos el índice del tratamiento
            loadTreatmentRecords(
                hospitalRecords?.getInt(
                    hospitalRecords?.getColumnIndex("Nhospitalitzacio") ?: 0
                ) ?: 0
            )  // Cargamos el siguiente tratamiento
        } else {
            Toast.makeText(this, "Ya estás en el último tratamiento", Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveToPreviousTreatment() {
        if (currentTreatmentIndex > 0) {
            currentTreatmentIndex--  //
            loadTreatmentRecords(
                hospitalRecords?.getInt(
                    hospitalRecords?.getColumnIndex("Nhospitalitzacio") ?: 0
                ) ?: 0
            )  // Cargamos el siguiente tratamiento
        } else {
            Toast.makeText(this, "Ya estás en el último tratamiento", Toast.LENGTH_SHORT).show()
        }
    }

    // Navegación entre pacientes
    @SuppressLint("Range")
    private fun moveToNextPatient() {
        filteredCursor?.let {
            if (it.moveToNext()) {
                currentIndex++
                showCurrentPatient()
                currentIdPacient = it.getInt(it.getColumnIndex("ID_Pacient"))
                loadHospitalizationRecords(currentIdPacient)  // Cargar los registros de hospitalización del siguiente paciente
            }
        }
    }

    @SuppressLint("Range")
    private fun moveToPreviousPatient() {
        filteredCursor?.let {
            if (currentIndex > 0) {
                if (it.moveToPrevious()) {
                    currentIndex--
                    showCurrentPatient()
                    currentIdPacient = it.getInt(it.getColumnIndex("ID_Pacient"))
                    loadHospitalizationRecords(currentIdPacient)  // Cargar los registros de hospitalización del paciente anterior
                }
            }
        }
    }

    // Navegación entre hospitalizaciones
    private fun moveToNextHospitalization() {
        if (currentHospitalizationIndex < maxHospitalizationRecords - 1) {
            currentHospitalizationIndex++
            showHospitalizationDetails()  // Actualizar la vista con el siguiente registro
        } else {
            Toast.makeText(
                this,
                "Ya estás en el último registro de hospitalización",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun moveToPreviousHospitalization() {
        if (currentHospitalizationIndex > 0) {
            currentHospitalizationIndex--
            showHospitalizationDetails()  // Actualizar la vista con el registro anterior
        } else {
            Toast.makeText(
                this,
                "Ya estás en el primer registro de hospitalización",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
