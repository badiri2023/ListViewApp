package com.example.listviewapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.FileNotFoundException

class MainActivity : AppCompatActivity() {

    // Modelo de datos
    class Record(var intents: Int, var nom: String, var imageName: String)
    var records: ArrayList<Record> = ArrayList()
    // array para imagenes de icono
    var imageAssetNames: ArrayList<String> = ArrayList()
    // Adaptador personalizado
    lateinit var adapter: ArrayAdapter<Record>
    // variables switch para los botones de ordenacion
    private var isSortedByNameAsc = true
    private var isSortedByIntentsAsc = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // LOAD IMAGES IN FROM ASSETS
        try {
            val allAssetFiles = assets.list("")
            if (allAssetFiles != null) {
                for (fileName in allAssetFiles) {
                    if (fileName.endsWith(".png")) {
                        imageAssetNames.add(fileName)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // --- MODELO ---
        // Datos iniciales
        records.add(Record(33, "Manolo", imageAssetNames.randomOrNull() ?: ""))
        records.add(Record(12, "Pepe", imageAssetNames.randomOrNull() ?: ""))
        records.add(Record(42, "Laura", imageAssetNames.randomOrNull() ?: ""))

        // --- ADAPTADOR ---
        adapter = object : ArrayAdapter<Record>(this, R.layout.list_item, records) {
            override fun getView(pos: Int, convertView: View?, container: ViewGroup): View {
                // Lógica de reciclaje de vistas
                var view = convertView
                if (view == null) {
                    view = layoutInflater.inflate(R.layout.list_item, container, false)
                }
                val recordActual = getItem(pos)
                // 2. ---CHARGE IMAGE ASIGNED ---
                try {
                    if (recordActual != null && recordActual.imageName.isNotEmpty()) {

                        // CHARGE THE BITMAP SAVED
                        val bitmap = BitmapFactory.decodeStream(assets.open(recordActual.imageName))
                        view!!.findViewById<ImageView>(R.id.imageView).setImageBitmap(bitmap)

                    } else {
                        // IF NO IMAGE, WE SET ONE BY DEFAULT
                        view!!.findViewById<ImageView>(R.id.imageView)
                            .setImageResource(R.mipmap.ic_launcher)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    view!!.findViewById<ImageView>(R.id.imageView)
                        .setImageResource(R.mipmap.ic_launcher)
                }

                view!!.findViewById<TextView>(R.id.nom).text = recordActual?.nom
                view.findViewById<TextView>(R.id.intents).text = recordActual?.intents.toString()

                return view
            }
        }

        // ------------ VISTA ------------
        // Conectar ListView con el Adaptador
        val lv = findViewById<ListView>(R.id.recordsView)
        lv.adapter = adapter

        // ------------ BUTTONS ------------

        // ADD BUTTON
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            showAddRecordDialog()
        }

        // SHORT NAME BUTTON
        val sortByNameButton = findViewById<Button>(R.id.sortByNameButton)
        sortByNameButton.text = "NAMES <"

        sortByNameButton.setOnClickListener {
            if (isSortedByNameAsc) {
                // ---- ESTADO ASCENDENTE (A-Z) -> CAMBIAR A DESCENDENTE (Z-A) ----
                records.sortByDescending { it.nom.lowercase() }
                sortByNameButton.text = "NAMES >"
                isSortedByNameAsc = false

            } else {
                // ---- ESTADO DESCENDENTE (Z-A) -> CAMBIAR A ASCENDENTE (A-Z) ----
                records.sortBy { it.nom.lowercase() }
                sortByNameButton.text = "NAMES <"
                isSortedByNameAsc = true
            }

            adapter.notifyDataSetChanged()
        }
        //SORT TRYS BUTTON
        val sortByIntentsButton = findViewById<Button>(R.id.sortByIntentsButton)
        sortByIntentsButton.text = "TRYS <"
        sortByIntentsButton.setOnClickListener {
            if (isSortedByIntentsAsc) {
                // ---- ESTADO ASCENDENTE (0-9) -> CAMBIAR A DESCENDENTE (9-0) ----
                records.sortByDescending { it.intents }
                sortByIntentsButton.text = "TRYS >"
                isSortedByIntentsAsc = false
            } else {
                records.sortBy { it.intents }
                sortByIntentsButton.text = "TRYS <"
                isSortedByIntentsAsc = true
            }

            adapter.notifyDataSetChanged()
        }
    }
    /**
     * FUNCION NEW USSER
     */
    private fun showAddRecordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_record, null)
        val editNom = dialogView.findViewById<EditText>(R.id.editNom)
        val editIntents = dialogView.findViewById<EditText>(R.id.editIntents)

        // BUTTONS TO ADD OR CANCEL
        val builder = AlertDialog.Builder(this)
        builder.setTitle("NEW RECORD")
        builder.setView(dialogView)

        builder.setNegativeButton("CANCEL") { dialog, _ ->
            dialog.cancel()
        }
        builder.setPositiveButton("ADD NEW", null)

        //Crear el DIALOG
        val dialog = builder.create()

        // COMPROBACIONES DEL POSSTIVE BUTTON ADD NEW ACTUALIZA CADA QUE SE PULSA
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                editNom.error = null
                editIntents.error = null

                val nom = editNom.text.toString()
                val intentsStr = editIntents.text.toString()
                var isValid = true
                var intents = 0
                // COMPROBACION DE NOMBRE VACÍO O DUPLICADO
                if (nom.isEmpty()) {
                    editNom.error = "MISSING NAME"
                    isValid = false
                }else{
                    val nameExists=records.any {it.nom.equals(nom,ignoreCase=true)}
                    if (nameExists){
                        editNom.error = "DUPLICATED NAME EXISTING"
                        isValid = false
                    }
                }
                // COMPROBACION INPUT TRYS
                if (intentsStr.isEmpty()) {
                    editIntents.error = "MISSING TRYS"
                    isValid = false
                } else {
                    try {
                        intents = intentsStr.toInt()
                        if (intents <= 0) {
                            editIntents.error = "ONLY POSITIVE NUMBERS"
                            isValid = false
                        }
                    } catch (e: NumberFormatException) {
                        editIntents.error = "ONLY NUMBERS"
                        isValid = false
                    }
                }
                // IF VALID, AÑADIMOS
                if (isValid) {
                    val newImageName = imageAssetNames.randomOrNull() ?: ""
                    records.add(Record(intents, nom, newImageName))
                    adapter.notifyDataSetChanged()
                    dialog.dismiss()
                } else {
                    // ELSE TO LET THE DIALOG OPEN
                }
            }
        }
        dialog.show()
    }
}
