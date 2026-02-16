package com.example.prollecte10

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prollecte10.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var db: SQLiteDatabase? = null
    private var myFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createDatabase()
        createTableUssers()
        createTableAlergies()
        createTablesPacient()
        createTableTractamentMalaltia()
        createTableHospital()
        createTableHospitalitzacio()
        createTableMalaltia()
        createTableMedicament()
        createTableTractament()


        // Configurar el listener del botón
        binding.btnIniciSesio.setOnClickListener {
            verificarUsuario()
        }
    }

    private fun createDatabase() {
        try {
            val folder =
                File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/Usser")
            if (!folder.exists()) {
                folder.mkdir()
            }

            myFile = File(folder, "MyDB")
            ConnectionClass.myfile = myFile

            db = SQLiteDatabase.openOrCreateDatabase(myFile!!.absolutePath, null, null)
        } catch (ex: Exception) {
            Toast.makeText(
                this,
                "Error al crear la base de datos: ${ex.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun createTableUssers() {
        try {
            val testQuery = "SELECT 1 FROM usser LIMIT 1"
            db!!.rawQuery(testQuery, null).close()

            Toast.makeText(this, "La tabla 'usser' ya existe.", Toast.LENGTH_SHORT).show()
        } catch (ex: Exception) {
            // Si ocurre un error, asumimos que la tabla no existe y la creamos
            if (ex.message?.contains("no such table") == true) {
                val createTableQuery = """
            CREATE TABLE usser (
                usserId INTEGER PRIMARY KEY AUTOINCREMENT,
                userName TEXT, 
                pasword TEXT, 
                Gmail TEXT
            )
            """
                db!!.execSQL(createTableQuery)
                Toast.makeText(this, "Tabla 'usser' creada exitosamente.", Toast.LENGTH_SHORT)
                    .show()

                // Insertar valores por defecto (nombre: admin, contraseña: admin)
                val insertDefaultUserQuery = """
            INSERT INTO usser (userName, pasword, Gmail) 
            VALUES ('admin', 'admin', 'admin@example.com')
            """
                db!!.execSQL(insertDefaultUserQuery)
                Toast.makeText(this, "Usuario por defecto 'admin' insertado.", Toast.LENGTH_SHORT)
                    .show()

            } else {
                Toast.makeText(
                    this,
                    "Error desconocido: ${ex.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun verificarUsuario() {
        val usName = binding.txtUser.text.toString().trim()
        val usPassword = binding.txtPasword.text.toString().trim()

        // Validar que los campos no estén vacíos
        if (usName.isEmpty() || usPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT)
                .show()
            return
        }
        try {
            // Consulta parametrizada para evitar inyección SQL
            val query = "SELECT * FROM usser WHERE userName = ? AND pasword = ?"
            val cursor = db?.rawQuery(query, arrayOf(usName, usPassword))

            if (cursor != null && cursor.moveToFirst()) {
                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Dadespersonals::class.java)
                intent.putExtra("msg", "add")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos23", Toast.LENGTH_SHORT)
                    .show()
            }
            cursor?.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al verificar usuario: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        db?.close()
    }
    private fun createTablesPacient() {
        try {
            val testQuery = "SELECT 1 FROM Pacient LIMIT 1"
            db!!.rawQuery(testQuery, null).close()

            Toast.makeText(this, "La tabla 'Pacient' ya existe.", Toast.LENGTH_SHORT).show()
        } catch (ex: Exception) {
            if (ex.message?.contains("no such table") == true) {
                // Crear la tabla "Pacient"
                val createPacientTable = """
                CREATE TABLE Pacient (
                    ID_Pacient INTEGER PRIMARY KEY AUTOINCREMENT,
                    Nom TEXT NOT NULL,
                    Cognoms TEXT NOT NULL,
                    DNI TEXT UNIQUE NOT NULL,
                    Data_naixement DATE,
                    Ciutat_Residencial TEXT,
                    Ciutat_Naixement TEXT,
                    Carrer TEXT,
                    Codi_Postal TEXT
--                   FOREIGN KEY (Ciutat_Residencial) REFERENCES Ciutat(Nom),
--                    FOREIGN KEY (Ciutat_Naixement) REFERENCES Ciutat(Nom)
                )
            """
                db!!.execSQL(createPacientTable)
                Toast.makeText(this, "Tabla 'Pacient' creada exitosamente.", Toast.LENGTH_SHORT).show()

                val insertInitialData = """
                INSERT INTO Pacient (Nom, Cognoms, DNI, Data_naixement, Ciutat_Residencial, Ciutat_Naixement, Carrer, Codi_Postal) 
                VALUES
                -- Pacientes con nombres únicos
                ('Maria', 'Lopez Garcia', '12345678A', '1990-05-12', 'Barcelona', 'Madrid', 'Calle Gran Via', '08001'),
                ('Juan', 'Perez Sanchez', '87654321B', '1985-09-20', 'Valencia', 'Sevilla', 'Avenida del Puerto', '46023'),
                ('Ana', 'Martinez Ruiz', '45678912C', '1978-12-05', 'Bilbao', 'Barcelona', 'Calle de la Amistad', '48001'),
                ('Luis', 'Hernandez Torres', '23456789D', '1992-03-15', 'Sevilla', 'Valencia', 'Calle Luna', '41001'),
                ('Carmen', 'Gomez Fernandez', '34567891E', '1980-07-30', 'Madrid', 'Bilbao', 'Calle Mayor', '28001'),
                -- Pacientes con el mismo nombre pero distinto apellido
                ('Maria', 'Gonzalez Perez', '65432178F', '1991-08-25', 'Barcelona', 'Madrid', 'Calle Nueva', '08002'),
                ('Maria', 'Lopez Fernandez', '78965412G', '1993-10-10', 'Valencia', 'Barcelona', 'Calle Principal', '46001'),
                ('Juan', 'Hernandez Lopez', '14725836H', '1988-12-22', 'Bilbao', 'Sevilla', 'Avenida del Sol', '48002'),
                ('Juan', 'Martinez Perez', '36925814J', '1986-04-11', 'Sevilla', 'Madrid', 'Calle Alegria', '41002'),
                ('Luis', 'Garcia Torres', '95175328K', '1995-06-18', 'Madrid', 'Valencia', 'Calle Esperanza', '28002'),
                -- Más pacientes con nombres únicos
                ('Clara', 'Rodriguez Gomez', '85274196L', '1982-11-14', 'Granada', 'Cordoba', 'Calle Verde', '18001'),
                ('David', 'Morales Ruiz', '96385274M', '1990-01-30', 'Zaragoza', 'Pamplona', 'Calle Azul', '50001'),
                ('Lucia', 'Ramirez Sanchez', '74185296N', '1987-03-09', 'Malaga', 'Sevilla', 'Avenida Blanca', '29001'),
                ('Pablo', 'Lopez Torres', '85296374O', '1984-07-07', 'Toledo', 'Madrid', 'Calle Larga', '45001'),
                ('Laura', 'Martinez Garcia', '95148632P', '1998-12-20', 'Oviedo', 'Santander', 'Calle Estrecha', '33001')
            """
                db!!.execSQL(insertInitialData)
                Toast.makeText(
                    this,
                    "Datos iniciales insertados en la tabla 'Pacient'.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Error desconocido al verificar la tabla 'Pacient': ${ex.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun createTableHospital() {
        try {
            val tableExistsQuery = """
            SELECT name 
            FROM sqlite_master 
            WHERE type='table' AND name='Hospital'
        """
            val cursor = db!!.rawQuery(tableExistsQuery, null)
            val tableExists = cursor.count > 0
            cursor.close()

            if (!tableExists) {
                val createHospitalTable = """
                CREATE TABLE Hospital (
                    ID_Hospital INTEGER PRIMARY KEY AUTOINCREMENT,
                    Nom_Hospital TEXT,
                    Universitari INTEGER CHECK (Universitari IN (0, 1)),
                    Ciutat TEXT,
                    Nombre_Habitacions INTEGER,
                    FOREIGN KEY (Ciutat) REFERENCES Ciutat(Nom)
                )
            """
                db!!.execSQL(createHospitalTable)
                Toast.makeText(this, "Tabla 'Hospital' creada exitosamente.", Toast.LENGTH_SHORT)
                    .show()
            }
            val dataCheckCursor = db!!.rawQuery("SELECT COUNT(*) FROM Hospital", null)
            dataCheckCursor.moveToFirst()
            val count = dataCheckCursor.getInt(0)
            dataCheckCursor.close()

            if (count == 0) {
                val insertData = """
                INSERT INTO Hospital (Nom_Hospital, Universitari, Ciutat, Nombre_Habitacions) 
                VALUES 
                ('Hospital General', 1, 'Barcelona', 250),
                ('Hospital Infantil', 0, 'Madrid', 120),
                ('Hospital Universitario', 1, 'Valencia', 300),
                ('Hospital Regional', 0, 'Sevilla', 200),
                ('Hospital de la Mujer', 0, 'Bilbao', 150)
            """
                db!!.execSQL(insertData)
                Toast.makeText(
                    this,
                    "Datos iniciales insertados en la tabla 'Hospital'.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "La tabla 'Hospital' ya contiene datos.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (ex: Exception) {
            Toast.makeText(
                this,
                "Error al crear la tabla 'Hospital': ${ex.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    private fun createTableHospitalitzacio() {
        try {
            val tableExistsQuery = """
        SELECT name 
        FROM sqlite_master 
        WHERE type='table' AND name='Hospitalitzacio'
    """
            val cursor = db!!.rawQuery(tableExistsQuery, null)
            val tableExists = cursor.count > 0
            cursor.close()

            if (!tableExists) {
                val createHospitalitzacioTable = """
            CREATE TABLE Hospitalitzacio (
                ID_Hospitalitzacio INTEGER PRIMARY KEY AUTOINCREMENT,
                ID_Pacient INTEGER,
                ID_Hospital INTEGER,
                Habitacio INTEGER,
                Duracio_hospitalitzacio INTEGER,
                Data_inici DATE,
                Data_fi DATE,
                Motiu TEXT,
                ID_Malaltia INTEGER,
                Pronostic_dies INTEGER,
                Nhospitalitzacio INTEGER,
                FOREIGN KEY (ID_Pacient) REFERENCES Pacient(ID_Pacient),
                FOREIGN KEY (ID_Hospital) REFERENCES Hospital(ID_Hospital),
                FOREIGN KEY (ID_Malaltia) REFERENCES Malaltia(ID_Malaltia)
            )
        """
                db!!.execSQL(createHospitalitzacioTable)
                Toast.makeText(
                    this,
                    "Tabla 'Hospitalitzacio' creada exitosamente.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            val dataCheckCursor = db!!.rawQuery("SELECT COUNT(*) FROM Hospitalitzacio", null)
            dataCheckCursor.moveToFirst()
            val count = dataCheckCursor.getInt(0)
            dataCheckCursor.close()

            if (count == 0) {
                // Insertar datos iniciales en la tabla "Hospitalitzacio"
                val insertData = """
            INSERT INTO Hospitalitzacio 
            (ID_Pacient, ID_Hospital, Habitacio, Duracio_hospitalitzacio, Data_inici, Data_fi, Motiu, ID_Malaltia, Pronostic_dies, Nhospitalitzacio) 
            VALUES 
            (1, 1, 101, 5, '2025-01-01', '2025-01-06', 'Cirugía de rodilla', 2, 7, 1),
            (2, 2, 202, 10, '2025-01-10', '2025-01-20', 'Recuperación pulmonar', 3, 14, 1),
            (3, 3, 303, 3, '2025-01-15', '2025-01-18', 'Observación post-quirúrgica', 1, 4, 1),
            (4, 4, 404, 7, '2025-01-05', '2025-01-12', 'Tratamiento de hipertensión severa', 5, 10, 1),
            (5, 5, 505, 15, '2025-01-20', '2025-02-04', 'Rehabilitación post-accidente', 4, 20, 1),
            (6, 2, 606, 4, '2025-02-01', '2025-02-05', 'Chequeo por complicaciones respiratorias', 6, 6, 1),
            (7, 3, 707, 8, '2025-02-10', '2025-02-18', 'Tratamiento intensivo para diabetes', 7, 12, 1),
            (8, 4, 808, 12, '2025-02-15', '2025-02-27', 'Recuperación de fractura de cadera', 8, 15, 1),
            (9, 5, 909, 5, '2025-03-01', '2025-03-06', 'Infección urinaria complicada', 9, 5, 1),
            (10, 2, 1001, 14, '2025-03-10', '2025-03-24', 'Tratamiento de migrañas crónicas', 10, 16, 1),
            
            -- Nuevos registros con ID_Pacient repetido:
            (1, 1, 1101, 6, '2025-03-05', '2025-03-11', 'Tratamiento de ansiedad', 2, 8, 2),
            (2, 3, 1202, 8, '2025-03-15', '2025-03-22', 'Recuperación de cirugía abdominal', 3, 15, 2),
            (3, 4, 1303, 7, '2025-03-20', '2025-03-27', 'Revisión post-quirúrgica', 1, 10, 2),
            (4, 5, 1404, 9, '2025-04-01', '2025-04-07', 'Control de presión arterial', 5, 5, 2),
            (5, 2, 1505, 5, '2025-04-10', '2025-04-15', 'Tratamiento de diabetes tipo 2', 6, 12, 2)
    """
                db!!.execSQL(insertData)
                Toast.makeText(
                    this,
                    "Datos iniciales insertados en la tabla 'Hospitalitzacio'.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "La tabla 'Hospitalitzacio' ya contiene datos.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } catch (ex: Exception) {
            Toast.makeText(
                this,
                "Error al crear la tabla 'Hospitalitzacio': ${ex.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    private fun createTableMalaltia() {
        try {
            // Verificar si la tabla "Malaltia" existe
            val tableExistsQuery = """
            SELECT name 
            FROM sqlite_master 
            WHERE type='table' AND name='Malaltia'
        """
            val cursor = db!!.rawQuery(tableExistsQuery, null)

            val tableExists = cursor.count > 0
            cursor.close()  // Asegurarse de cerrar el cursor para evitar fugas de memoria

            if (!tableExists) {
                val createMalaltiaTable = """
                CREATE TABLE Malaltia (
                    ID_Malaltia INTEGER PRIMARY KEY AUTOINCREMENT,
                    Nom TEXT,
                    Simptomes TEXT,
                    Reaccions TEXT
                )
            """
                db!!.execSQL(createMalaltiaTable)
                Toast.makeText(this, "Tabla 'Malaltia' creada exitosamente.", Toast.LENGTH_SHORT)
                    .show()
            }

            val dataCheckCursor = db!!.rawQuery("SELECT COUNT(*) FROM Malaltia", null)
            dataCheckCursor.moveToFirst()
            val count = dataCheckCursor.getInt(0)
            dataCheckCursor.close()

            if (count == 0) {
                // Insertar datos iniciales en la tabla "Malaltia"
                val insertData = """
                INSERT INTO Malaltia (Nom, Simptomes, Reaccions) 
                VALUES 
                ('Gripe', 'Fiebre, Tos, Dolor de cabeza', 'Ninguna'),
    ('COVID-19', 'Fiebre, Tos seca, Dificultad para respirar', 'Pérdida del gusto/olfato'),
    ('Alergia', 'Estornudos, Picazón, Congestión nasal', 'Reacciones a antihistamínicos'),
    ('Asma', 'Dificultad para respirar, Opresión en el pecho, Sibilancias', 'Reacción a inhaladores'),
    ('Diabetes', 'Sed excesiva, Fatiga, Pérdida de peso', 'Hipoglucemia si no se controla'),
    ('Hipertensión', 'Dolor de cabeza, Mareos, Visión borrosa', 'Efectos secundarios de antihipertensivos'),
    ('Migraña', 'Dolor de cabeza severo, Náuseas, Sensibilidad a la luz', 'Reacción a analgésicos fuertes'),
    ('Gastritis', 'Dolor abdominal, Náuseas, Ardor de estómago', 'Irritación por medicamentos como ibuprofeno')
"""
                db!!.execSQL(insertData)
                Toast.makeText(
                    this,
                    "Datos iniciales insertados en la tabla 'Malaltia'.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "La tabla 'Malaltia' ya contiene datos.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } catch (ex: Exception) {
            Toast.makeText(
                this,
                "Error al crear la tabla 'Malaltia' o insertar datos: ${ex.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun createTableMedicament() {
        try {
            // Verificar si la tabla "Medicament" existe
            val tableExistsQuery = """
            SELECT name 
            FROM sqlite_master 
            WHERE type='table' AND name='Medicament'
        """
            val cursor = db!!.rawQuery(tableExistsQuery, null)
            val tableExists = cursor.count > 0
            cursor.close()  // Asegurarse de cerrar el cursor para evitar fugas de memoria

            if (!tableExists) {
                val createMedicamentTable = """
                CREATE TABLE Medicament (
                    ID_Medicament INTEGER PRIMARY KEY AUTOINCREMENT,
                    Nom TEXT,
                    Marca TEXT,
                    Forma_presentacio TEXT,
                    Efectes_Secundaris TEXT
                )
            """
                db!!.execSQL(createMedicamentTable)
                Toast.makeText(this, "Tabla 'Medicament' creada exitosamente.", Toast.LENGTH_SHORT)
                    .show()
            }

            val dataCheckCursor = db!!.rawQuery("SELECT COUNT(*) FROM Medicament", null)
            dataCheckCursor.moveToFirst()
            val count = dataCheckCursor.getInt(0)
            dataCheckCursor.close()

            if (count == 0) {
                val insertQuery = """
                INSERT INTO Medicament (Nom, Marca, Forma_presentacio, Efectes_Secundaris) 
                VALUES 
                ('Paracetamol', 'Tylenol', 'Comprimidos', 'Náuseas, mareos, reacciones alérgicas'),
                ('Ibuprofeno', 'Advil', 'Cápsulas', 'Dolor estomacal, náuseas, mareos'),
                ('Amoxicilina', 'Amoxil', 'Suspensión líquida', 'Diarrea, erupciones cutáneas'),
                ('Loratadina', 'Claritin', 'Tabletas', 'Sequedad bucal, somnolencia'),
                ('Paracetamol', 'Tylenol', 'Comprimidos', 'Náuseas, mareos, reacciones alérgicas'),
                ('Ibuprofeno', 'Advil', 'Cápsulas', 'Dolor estomacal, náuseas, mareos'),
                ('Amoxicilina', 'Amoxil', 'Suspensión líquida', 'Diarrea, erupciones cutáneas'),
                ('Loratadina', 'Claritin', 'Tabletas', 'Sequedad bucal, somnolencia')
            """
                db!!.execSQL(insertQuery)
                Toast.makeText(
                    this,
                    "Datos iniciales insertados en la tabla 'Medicament'.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } catch (ex: Exception) {
            Toast.makeText(
                this,
                "Error al crear tabla 'Medicament': ${ex.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun createTableTractament() {
        try {
            val tableExistsQuery = """
            SELECT name 
            FROM sqlite_master 
            WHERE type='table' AND name='Tractament'
        """
            val cursor = db!!.rawQuery(tableExistsQuery, null)
            val tableExists = cursor.count > 0
            cursor.close()

            if (!tableExists) {
                val createTractamentTable = """
                CREATE TABLE Tractament (
                    ID_Tractament INTEGER PRIMARY KEY AUTOINCREMENT,
                    Medicament INTEGER,
                    Horari TEXT,
                    Data_inici DATE,
                    Data_fi DATE,
                    ID_Hospitalitzacio INTEGER,
                    ID_Malaltia INTEGER,
                    FOREIGN KEY (Medicament) REFERENCES Medicament(ID_Medicament),
                    FOREIGN KEY (ID_Hospitalitzacio) REFERENCES Hospitalitzacio(ID_Hospitalitzacio),
                    FOREIGN KEY (ID_Malaltia) REFERENCES Malaltia(ID_Malaltia)
                )
            """
                db!!.execSQL(createTractamentTable)
                Toast.makeText(this, "Tabla 'Tractament' creada exitosamente.", Toast.LENGTH_SHORT)
                    .show()
            }

            val dataCheckCursor = db!!.rawQuery("SELECT COUNT(*) FROM Tractament", null)
            dataCheckCursor.moveToFirst()
            val count = dataCheckCursor.getInt(0)
            dataCheckCursor.close()

            if (count == 0) {
                val insertQuery = """
                INSERT INTO Tractament (Medicament, Horari, Data_inici, Data_fi, ID_Hospitalitzacio, ID_Malaltia) 
                VALUES 
                (1, '12:00, 20:00', '2022-02-01', '2022-02-12', 2, 1),
                (1, '12:00, 20:00', '2022-02-03', '2022-02-13', 3, 1),
                (3, '12:00, 20:00', '2022-04-04', '2022-04-14', 4, 1),
                (4, '12:00, 20:00', '2022-05-05', '2022-05-15', 5, 1),
                (1, '12:00, 20:00', '2022-05-01', '2022-05-21', 6, 1),
                (3, '12:00, 20:00', '2022-05-01', '2022-05-21', 7, 1),
                (4, '07:00, 19:00', '2024-04-03', '2024-04-23', 3, 3),
                (4, '08:00, 18:00', '2024-03-27', '2024-04-14', 4, 3),
                (1, '08:00, 18:00', '2024-03-27', '2024-04-14', 4, 3),
                (1, '08:00, 18:00', '2024-03-27', '2024-04-14', 1, 2)
            """
                db!!.execSQL(insertQuery)
                Toast.makeText(
                    this,
                    "Datos iniciales insertados en la tabla 'Tractament'.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } catch (ex: Exception) {
            Toast.makeText(
                this,
                "Error al crear la tabla 'Tractament': ${ex.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun createTableTractamentMalaltia() {
        try {
            val tableExistsQuery = """
            SELECT name 
            FROM sqlite_master 
            WHERE type='table' AND name='Tractament_malalties'
        """
            val cursor = db!!.rawQuery(tableExistsQuery, null)
            val tableExists = cursor.count > 0
            cursor.close()

            if (!tableExists) {
                val createTableQuery = """
                CREATE TABLE Tractament_malalties (
                    ID_Tractamentmalalties INTEGER PRIMARY KEY AUTOINCREMENT,
                    Hores TEXT,
                    data_inici DATE,
                    data_fi DATE,
                    ID_Malaltia INTEGER,
                    ID_Medicament INTEGER,
                    ID_Pacient INTEGER,
                    inici_malaltia DATE,
                    fi_malaltia DATE,
                    FOREIGN KEY (ID_Pacient) REFERENCES Pacient(ID_Pacient),
                    FOREIGN KEY (ID_Malaltia) REFERENCES Malaltia(ID_Malaltia),
                    FOREIGN KEY (ID_Medicament) REFERENCES Medicament(ID_Medicament)
                )
            """
                db!!.execSQL(createTableQuery)
                Toast.makeText(
                    this,
                    "Tabla 'Tractament_malalties' creada exitosamente.",
                    Toast.LENGTH_SHORT
                ).show()

                // Insertar datos iniciales
                val insertDataQuery = """
                INSERT INTO Tractament_malalties (Hores, data_inici, data_fi, ID_Malaltia, ID_Medicament, ID_Pacient, inici_malaltia, fi_malaltia)
                VALUES
                    ('12:00', '2025-01-01', '2025-01-10', 1, 1, 1, '2025-01-01', '2025-01-16'),
                    ('21:00', '2025-01-02', '2025-01-12', 2, 2, 2, '2025-01-02', '2025-02-19'),
                    ('08:00', '2025-01-03', '2025-01-15', 3, 3, 3, '2025-01-03', '2025-01-25'),
                    ('14:00', '2025-01-04', '2025-01-16', 4, 4, 4, '2025-01-04', '2025-03-21'),
                    ('19:00', '2025-01-05', '2025-01-20', 5, 5, 5, '2025-01-05', '2025-02-25'),
                    ('19:00', '2025-01-05', '2025-01-20', 5, 1, 5, '2025-01-05', '2025-02-25'),
                    ('09:00', '2025-01-06', '2025-01-18', 6, 2, 3, '2025-01-06', '2025-01-22'),
                    ('17:00', '2025-01-07', '2025-01-22', 7, 1, 4, '2025-01-07', '2025-02-10'),
                    ('11:00', '2025-01-08', '2025-01-19', 3, 4, 5, '2025-01-08', '2025-02-05'),
                    ('20:00', '2025-01-09', '2025-01-23', 2, 5, 3, '2025-01-09', '2025-02-28'),
                    ('15:00', '2025-01-10', '2025-01-25', 1, 4, 4, '2025-01-10', '2025-02-20')
            """
                db!!.execSQL(insertDataQuery)
                Toast.makeText(
                    this,
                    "Datos iniciales insertados correctamente.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "La tabla 'Tractament_malalties' ya existe. No se realizaron cambios.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (ex: Exception) {
            Toast.makeText(
                this,
                "Error al crear la tabla o insertar los datos: ${ex.message}",
                Toast.LENGTH_LONG
            ).show()

        }
    }

    private fun createTableAlergies() {
        try {
            val tableExistsQuery = """
        SELECT name 
        FROM sqlite_master 
        WHERE type='table' AND name='Alergies'
        """
            val cursor = db!!.rawQuery(tableExistsQuery, null)
            val tableExists = cursor.count > 0
            cursor.close() // Asegúrate de cerrar el cursor para evitar fugas de memoria

            if (!tableExists) {
                val createAlergiesTable = """
            CREATE TABLE Alergies (
                ID_Alergies INTEGER PRIMARY KEY AUTOINCREMENT,
                ID_Pacient INTEGER,
                ID_Medicament INTEGER,
                Descripci TEXT,
                FOREIGN KEY (ID_Pacient) REFERENCES Pacient(ID_Pacient),
                FOREIGN KEY (ID_Medicament) REFERENCES Medicament(ID_Medicament)
            )
            """
                db!!.execSQL(createAlergiesTable)

                // Agregar algunos valores de ejemplo
                val insertValues = """
            INSERT INTO Alergies (ID_Pacient, ID_Medicament, Descripci) VALUES
            ( 1, 1, 'Alergia al polen'),
            ( 2, 2, 'Alergia a la fruita(pomes)'),
            ( 3, 3, 'Alergia a los mariscos'),
            ( 4, 5, 'Alergia als pels de gat'),
            ( 5, 6, 'Alergia als acars'),
            ( 3, 2, 'Alergia als fruits secs'),
            ( 4, 6, 'Alergia al polen'),
            ( 3, 5, 'Alergia a lalactosa '),
            ( 4, 7, 'Alergia als mariscs')
            """
                db!!.execSQL(insertValues)

                Toast.makeText(
                    this,
                    "Tabla 'Alergies' creada y valores insertados exitosamente.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, "La tabla 'Alergies' ya existe.", Toast.LENGTH_SHORT).show()
            }
        } catch (ex: Exception) {
            Toast.makeText(
                this,
                "Error al crear la tabla 'Alergies' o insertar datos: ${ex.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
