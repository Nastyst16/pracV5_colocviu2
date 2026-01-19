package com.example.pracv5_colocviu2.network

import android.util.Log
import com.example.pracv5_colocviu2.general.Constants
import com.example.pracv5_colocviu2.general.WeatherForecastInformation // Asigura-te ca ai creat clasa asta
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

// MODIFICARE 1: Constructorul primeste acum portul (Int), nu EditText-ul
class ServerThread(private val port: Int) : Thread() {

    private var serverSocket: ServerSocket? = null

    // MODIFICARE 2 (Cerinta 3a): Cache-ul local
    // Cheia este orasul (String), Valoarea este obiectul meteo
    private val data = HashMap<String, WeatherForecastInformation>()

    fun startServer() {
        start()
        Log.v(Constants.TAG, "startServer() invoked on port $port")
    }

    fun stopServer() {
        try {
            serverSocket?.close()
        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.message)
        }
    }

    // MODIFICARE 3: Metode sincronizate pentru lucrul cu Cache-ul
    // Este vital sa fie @Synchronized pentru ca mai multi clienti pot scrie/citi simultan
    @Synchronized
    fun setData(city: String, weatherForecastInformation: WeatherForecastInformation) {
        this.data[city] = weatherForecastInformation
    }

    @Synchronized
    fun getData(): HashMap<String, WeatherForecastInformation> {
        return this.data
    }

    override fun run() {
        try {
            // MODIFICARE 4: Deschidem socket-ul pe portul primit in constructor
            serverSocket = ServerSocket(port)

            while (!Thread.currentThread().isInterrupted) {
                Log.v(Constants.TAG, "Waiting for clients...")
                val socket = serverSocket!!.accept()
                Log.v(Constants.TAG, "A client has connected: " + socket.inetAddress)

                // MODIFICARE 5: Instantiem CommunicationThread
                // Ii dam socket-ul SI o referinta catre acest ServerThread ("this")
                // Asta va da eroare pana modificam si CommunicationThread, e normal!
                val communicationThread = CommunicationThread(this, socket)
                communicationThread.start()
            }
        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.message)
        }
    }
}