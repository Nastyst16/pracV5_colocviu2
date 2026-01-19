package com.example.pracv5_colocviu2

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pracv5_colocviu2.general.Constants
import com.example.pracv5_colocviu2.network.ClientThread
import com.example.pracv5_colocviu2.network.ServerThread

class PracV5_Colocviu2MainActivity : AppCompatActivity() {

    // 1. Declaram variabilele pentru elementele vizuale
    private lateinit var serverPortEditText: EditText
    private lateinit var serverConnectButton: Button

    private lateinit var clientAddressEditText: EditText
    private lateinit var clientPortEditText: EditText
    private lateinit var clientCityEditText: EditText
    private lateinit var clientInformationTypeEditText: EditText
    private lateinit var clientGetWeatherButton: Button
    private lateinit var clientResultTextView: TextView

    // 2. Referinta catre thread-ul Serverului (pentru a-l opri la final)
    private var serverThread: ServerThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Legam codul de fisierul XML (asigura-te ca XML-ul tau are acest nume!)
        setContentView(R.layout.activity_pracv5_colocviu2)

        // 3. Initializam controalele (le gasim dupa ID-urile din XML)
        serverPortEditText = findViewById(R.id.server_port_edit_text)
        serverConnectButton = findViewById(R.id.server_connect_button)

        clientAddressEditText = findViewById(R.id.client_address_edit_text)
        clientPortEditText = findViewById(R.id.client_port_edit_text)
        clientCityEditText = findViewById(R.id.client_city_edit_text)
        clientInformationTypeEditText = findViewById(R.id.client_information_type_edit_text)
        clientGetWeatherButton = findViewById(R.id.client_get_weather_button)
        clientResultTextView = findViewById(R.id.client_result_text_view)

        // -----------------------------------------------------------------------
        // LOGICA PENTRU SERVER (Partea de sus a ecranului)
        // -----------------------------------------------------------------------
        serverConnectButton.setOnClickListener {
            val serverPort = serverPortEditText.text.toString()

            if (serverPort.isEmpty()) {
                Toast.makeText(this, "Te rog introdu un port pentru server!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Pornim serverul doar daca nu ruleaza deja
            if (serverThread == null || !serverThread!!.isAlive) {
                serverThread = ServerThread(serverPort.toInt())
                serverThread!!.startServer()
                Toast.makeText(this, "Server pornit pe portul $serverPort", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Serverul ruleaza deja!", Toast.LENGTH_SHORT).show()
            }
        }

        // -----------------------------------------------------------------------
        // LOGICA PENTRU CLIENT (Partea de jos a ecranului)
        // -----------------------------------------------------------------------
        clientGetWeatherButton.setOnClickListener {
            val clientAddress = clientAddressEditText.text.toString()
            val clientPort = clientPortEditText.text.toString()
            val city = clientCityEditText.text.toString()
            val informationType = clientInformationTypeEditText.text.toString()

            // Validari ca sa nu crape aplicatia daca uiti un camp gol
            if (clientAddress.isEmpty() || clientPort.isEmpty() || city.isEmpty() || informationType.isEmpty()) {
                Toast.makeText(this, "Completeaza toate campurile de la Client!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Resetam textul rezultatelor
            clientResultTextView.text = "Fetching data..."

            // Pornim un thread de client care se conecteaza la server
            val clientThread = ClientThread(
                clientAddress,
                clientPort.toInt(),
                city,
                informationType,
                clientResultTextView
            )
            clientThread.start()
        }
    }

    // Aceasta metoda se apeleaza cand inchizi aplicatia de tot
    override fun onDestroy() {
        Log.i(Constants.TAG, "[MAIN] Aplicatia se inchide, oprim serverul...")
        serverThread?.stopServer()
        super.onDestroy()
    }
}