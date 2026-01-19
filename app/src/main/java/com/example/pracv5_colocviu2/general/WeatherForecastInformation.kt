package com.example.pracv5_colocviu2.general

// Aceasta clasa va tine toate datele cerute in modelul de colocviu
data class WeatherForecastInformation(
    val temperature: String,
    val windSpeed: String,
    val condition: String,
    val pressure: String,
    val humidity: String
) {
    // Metoda utila pentru cerinta cu parametrul "all"
    override fun toString(): String {
        return "Temp: $temperature, Wind: $windSpeed, Cond: $condition, Press: $pressure, Hum: $humidity"
    }
}