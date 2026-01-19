package com.example.pracv5_colocviu2.general

data class WeatherForecastInformation(
    val temperature: String,
    val windSpeed: String,
    val condition: String,
    val pressure: String,
    val humidity: String
) {
    // MODIFICARE: Folosim \n (New Line) in loc de virgula
    override fun toString(): String {
        return "Temperature: $temperature\n" +
                "Wind Speed: $windSpeed\n" +
                "Condition: $condition\n" +
                "Pressure: $pressure\n" +
                "Humidity: $humidity"
    }
}