package com.example.prollecte10

import android.annotation.SuppressLint
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.prollecte10.databinding.DadespersonalsMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

    class Dadespersonals : AppCompatActivity() {

        private lateinit var binding: DadespersonalsMainBinding
        private var db: SQLiteDatabase? = null
        private var myFile: File? = null
        private var cursor: android.database.Cursor? = null
        public var currentIndex: Int = -1 // Índice actual en el cursor
        private var filteredCursor: android.database.Cursor? = null // Cursor filtrado por nombre
        private var currentName: String = "" // Nombre actual de búsqueda
        private var currentDni: String = "" // DNI actual de búsqueda
        var nombreCompleto: String = ""

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // Configurar ViewBinding
            binding = DadespersonalsMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Conectar a la base de datos
            connectDatabase()

            // Configurar el listener para el campo de texto del nombre
            binding.textnom2.doOnTextChanged { text, _, _, _ ->
                val name = text.toString().trim()
                if (name.isNotEmpty()) {
                    currentName = name
                    searchAllByName(name)  // Buscar por nombre
                } else {
                    clearFields()  // Limpiar campos si el nombre está vacío
                    currentName = ""  // Restablecer el nombre
                }
            }

            binding.btnInsert.setOnClickListener {
                val intent = Intent(this, Inserirpacients::class.java)
                startActivity(intent)
            }

            // Configurar el listener para el campo de texto del DNI
            binding.txtdni.doOnTextChanged { text, _, _, _ ->
                val dni = text.toString().trim()
                if (dni.isNotEmpty()) {
                    currentDni = dni
                    searchAllByDni(dni)  // Buscar por DNI
                } else {
                    clearFields()  // Limpiar campos si el DNI está vacío
                    currentDni = ""  // Restablecer el DNI
                }
            }

            // Configurar los botones para navegar entre los resultados
            binding.btnNext.setOnClickListener { moveToNext() }
            binding.btnBefor.setOnClickListener { moveToPrevious() }

            binding.btnmalalties.setOnClickListener {
                val intent = Intent(this, Malalties::class.java)
                startActivity(intent)
            }

            binding.alergies.setOnClickListener {
                val intent = Intent(this, Alergies::class.java)
                startActivity(intent)
            }

            // Si recibimos un índice desde otra actividad, lo aplicamos
            val intent = intent
            val newIndex = intent.getIntExtra("currentIndex", -1)
            if (newIndex != -1) {
                currentIndex = newIndex
                showCurrentRecord()  // Mostrar el registro correspondiente
            }

            // Navegar a la actividad Tonto para cambiar el índice
            binding.btnospitalitzacio.setOnClickListener {
                val intent = Intent(this, Hospitalitzacions::class.java)
                intent.putExtra("currentIndex", currentIndex)
                intent.putExtra("nombreCompleto", nombreCompleto)
                startActivityForResult(intent, 1) // Esperamos un resultado
            }

            binding.btnMedicament.setOnClickListener {
                val intent = Intent(this, Medicaments::class.java)
                startActivity(intent)
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == 1 && resultCode == RESULT_OK) {
                // Recibir currentIndex y nombreCompleto actualizados
                currentIndex = data?.getIntExtra("currentIndex", -1) ?: -1
                nombreCompleto = data?.getStringExtra("nombreCompleto") ?: "Nombre no disponible"

                // Actualizar el campo de texto con el nuevo nombre completo
                binding.editTextText2.setText(nombreCompleto)

                // Llamar a la función que muestra los datos del paciente
                showCurrentRecord() // Actualizar el registro
            }
        }

        private fun connectDatabase() {
            try {
                val folder = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/Usser")
                if (!folder.exists()) {
                    folder.mkdirs()
                }
                myFile = File(folder, "MyDB")

                if (myFile?.exists() == true) {
                    db = SQLiteDatabase.openDatabase(myFile!!.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
                } else {
                    Toast.makeText(this, "La base de datos no existe", Toast.LENGTH_LONG).show()
                }
            } catch (ex: Exception) {
                Toast.makeText(this, "Error al conectar con la base de datos: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }

        @SuppressLint("Range")
        private fun showCurrentRecord() {
            filteredCursor?.let {
                if (it.moveToPosition(currentIndex)) {
                    val nom = it.getString(it.getColumnIndex("Nom"))
                    val cognoms = it.getString(it.getColumnIndex("Cognoms"))
                    val dni = it.getString(it.getColumnIndex("DNI"))
                    val dataNaixement = it.getString(it.getColumnIndex("Data_naixement"))
                    val ciutatResidencial = it.getString(it.getColumnIndex("Ciutat_Residencial"))
                    val ciutatNaixement = it.getString(it.getColumnIndex("Ciutat_Naixement"))
                    val carrer = it.getString(it.getColumnIndex("Carrer"))
                    val codiPostal = it.getString(it.getColumnIndex("Codi_Postal"))

                    // Concatenar y actualizar el nombre completo
                    nombreCompleto = "$nom $cognoms"

                    showPacientData(nom, cognoms, dni, dataNaixement, ciutatResidencial, ciutatNaixement, carrer, codiPostal, nombreCompleto)

                    // Actualizar los campos de texto si están vacíos
                    if (binding.editTextText2.text.isEmpty()) {
                        binding.editTextText2.setText("$nom $cognoms")
                    }
                    if (binding.txtcPostal.text.isEmpty()) {
                        binding.txtcPostal.setText(codiPostal)
                    }
                    if (binding.txtcarrer.text.isEmpty()) {
                        binding.txtcarrer.setText(carrer)
                    }
                }
            }
        }

        @SuppressLint("Range")
        private fun searchAllByName(name: String) {
            if (name.isEmpty()) {
                clearFields()
                return
            }

            if (name.length > 1) {
                try {
                    binding.txtdni.setText("")
                    val query = "SELECT * FROM Pacient WHERE Nom LIKE ?"
                    filteredCursor = db?.rawQuery(query, arrayOf("%$name%"))
                    currentIndex = 0
                    showCurrentRecord()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al buscar por nombre: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                clearFields()
            }
        }

        private fun clearFields() {
            binding.textnom2.hint = "Nom"
            binding.txtcognom1.text = ""
            binding.txtcognom2.text = ""
            binding.txtdni.hint = "DNI"
            binding.txtEdat.text = ""
            binding.txtdtNaixement.text = ""
            binding.txtctNeixement.text = ""
            binding.txtctResidencia.text = ""
            binding.txtcarrer.text = ""
            binding.txtcPostal.text = ""
        }

        @SuppressLint("Range")
        private fun searchAllByDni(dni: String) {
            if (dni.isEmpty()) {
                clearFields()
                return
            }

            try {
                val query = "SELECT * FROM Pacient WHERE DNI = ?"
                filteredCursor = db?.rawQuery(query, arrayOf(dni))

                if (filteredCursor != null && filteredCursor!!.moveToFirst()) {
                    binding.textnom2.setText("")
                    currentIndex = 0
                    showCurrentRecord()
                } else {
                    showToast("No se encontró el DNI")
                    clearFields()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al buscar por DNI: ${e.message}", Toast.LENGTH_LONG).show()
                clearFields()
            }
        }

        private fun showPacientData(nom: String, cognoms: String, dni: String, dataNaixement: String, ciutatResidencial: String, ciutatNaixement: String, carrer: String, codiPostal: String, nomComplet: String) {
            val apellidos = cognoms.split(" ")
            val primerApellido = apellidos.getOrNull(0) ?: ""
            val segundoApellido = apellidos.getOrNull(1) ?: ""
            binding.textnom2.hint = nom
            binding.txtcognom1.text = primerApellido
            binding.txtcognom2.text = segundoApellido
            binding.txtdni.hint = dni
            binding.txtEdat.text = calculateAge(dataNaixement).toString()
            binding.txtdtNaixement.text = dataNaixement
            binding.txtctNeixement.text = ciutatNaixement
            binding.txtctResidencia.text = ciutatResidencial
            binding.editTextText2.text = "$nom $cognoms"
        }

        private fun calculateAge(birthDate: String): Int {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val birthDateObj = sdf.parse(birthDate) ?: return 0
                val birthLocalDate = Calendar.getInstance().apply { time = birthDateObj }
                val currentDate = Calendar.getInstance()

                var age = currentDate.get(Calendar.YEAR) - birthLocalDate.get(Calendar.YEAR)
                if (currentDate.get(Calendar.DAY_OF_YEAR) < birthLocalDate.get(Calendar.DAY_OF_YEAR)) {
                    age -= 1
                }
                age
            } catch (e: Exception) {
                0
            }
        }

        private fun moveToPrevious() {
            if (filteredCursor != null && currentIndex > 0) {
                currentIndex--
                filteredCursor?.moveToPosition(currentIndex)
                showCurrentRecord()
            } else {
                showToast("No hay registros anteriores")
            }
        }

        private fun moveToNext() {
            if (filteredCursor != null && currentIndex < (filteredCursor?.count ?: 0) - 1) {
                currentIndex++
                filteredCursor?.moveToPosition(currentIndex)
                showCurrentRecord()
            } else {
                showToast("No hay más registros")
            }
        }

        private fun showToast(message: String) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        override fun onDestroy() {
            super.onDestroy()
            db?.close()
            filteredCursor?.close()
        }
    }

