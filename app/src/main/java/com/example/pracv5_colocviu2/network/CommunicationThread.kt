package com.example.pracv5_colocviu2.network

import android.util.Log
import com.example.pracv5_colocviu2.general.Constants
import com.example.pracv5_colocviu2.general.Utilities
import com.example.pracv5_colocviu2.general.WeatherForecastInformation
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.Socket

// MODIFICARE: Primim ServerThread in constructor pentru a accesa Cache-ul (HashMap-ul)
class CommunicationThread(private val serverThread: ServerThread, private val socket: Socket) : Thread() {

    override fun run() {
        try {
            // 1. Obtinem fluxurile de intrare/iesire
            val requestReader = Utilities.getReader(socket)
            val responseWriter = Utilities.getWriter(socket)

            // 2. Citim cererea de la client (Orasul si Tipul Informatiei)
            val city = requestReader.readLine()
            val informationType = requestReader.readLine()

            if (city.isNullOrEmpty() || informationType.isNullOrEmpty()) {
                Log.e(Constants.TAG, "Error receiving parameters from client")
                return
            }

            // 3. Verificam Cache-ul (HashMap din ServerThread)
            var data = serverThread.getData()[city]

            if (data == null) {
                Log.i(Constants.TAG, "Data not in cache for $city. Fetching from web...")

                // 4. Cerinta 3b: Daca nu avem date, le descarcam cu OkHttp
                val client = OkHttpClient()
                val url = "${Constants.API_URL}?q=$city&appid=${Constants.API_KEY}&units=metric"

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    // 5. Parsam JSON-ul primit de la OpenWeather
                    val content = JSONObject(responseBody)

                    // Structura JSON OpenWeather: main { temp, pressure, humidity }, wind { speed }, weather [ { main } ]
                    val main = content.getJSONObject("main")
                    val wind = content.getJSONObject("wind")
                    val weatherArray = content.getJSONArray("weather")
                    val weatherObject = weatherArray.getJSONObject(0)

                    val temperature = main.getString("temp")
                    val pressure = main.getString("pressure")
                    val humidity = main.getString("humidity")
                    val windSpeed = wind.getString("speed")
                    val condition = weatherObject.getString("main")

                    // 6. Cream obiectul si il salvam in Cache (Cerinta 3a)
                    val weatherForecastInformation = WeatherForecastInformation(
                        temperature, windSpeed, condition, pressure, humidity
                    )
                    serverThread.setData(city, weatherForecastInformation)
                    data = weatherForecastInformation // Actualizam variabila locala
                }
            } else {
                Log.i(Constants.TAG, "Data found in cache for $city!")
            }

            // 7. Cerinta 3d: Trimitem raspunsul cu tot cu ETICHETA (Label)
            if (data != null) {
                val result = when (informationType) {
                    "all" -> data.toString()
                    // MODIFICARE: Adaugam textul explicativ inainte de valoare
                    "temperature" -> "Temperature: ${data.temperature} C"
                    "wind" -> "Wind Speed: ${data.windSpeed} m/s"
                    "condition" -> "Condition: ${data.condition}"
                    "humidity" -> "Humidity: ${data.humidity} %"
                    "pressure" -> "Pressure: ${data.pressure} hPa"
                    else -> "Wrong information type"
                }
                // Trimitem rezultatul
                responseWriter.println(result)
            }

        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "Data processing error: " + ioException.message)
        } catch (jsonException: JSONException) {
            Log.e(Constants.TAG, "JSON parsing error: " + jsonException.message)
        } finally {
            try {
                socket.close()
            } catch (ioException: IOException) {
                Log.e(Constants.TAG, "Error closing socket: " + ioException.message)
            }
        }
    }
}