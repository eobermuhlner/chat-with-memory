package ch.obermuhlner.chat.tools

import dev.langchain4j.agent.tool.Tool
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory

class Weather {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    val client = OkHttpClient()

    @Tool("The hourly weather forecast")
    fun weatherForecastHourly(latitude: Double, longitude: Double, days: Int): String {
        logger.info("Hourly weather forecast for $latitude, $longitude, $days days")

        if (latitude == 0.0 && longitude == 0.0) {
            return "Unknown location"
        }

        val url = HttpUrl.Builder()
            .scheme("https")
            .host("api.open-meteo.com")
            .addPathSegment("v1")
            .addPathSegment("forecast")
            .addQueryParameter("latitude", latitude.toString())
            .addQueryParameter("longitude", longitude.toString())
            .addQueryParameter("timezone", "auto")
            .addQueryParameter("hourly", "temperature_2m,precipitation_probability,rain,showers,snowfall,snow_depth,weathercode,cloudcover,visibility")
            .addQueryParameter("forecast_days", "$days")
            .build()

        return simpleGetRequest(client, url)
    }

    @Tool("The hourly astrophotography forecast")
    fun astrophotographyForecastHourly(latitude: Double, longitude: Double, days: Int): String {
        logger.info("Hourly astrophotography weather forecast for $latitude, $longitude, days: $days")

        if (latitude == 0.0 && longitude == 0.0) {
            return "Unknown location"
        }

        val url = HttpUrl.Builder()
            .scheme("https")
            .host("api.open-meteo.com")
            .addPathSegment("v1")
            .addPathSegment("forecast")
            .addQueryParameter("latitude", latitude.toString())
            .addQueryParameter("longitude", longitude.toString())
            .addQueryParameter("timezone", "auto")
            .addQueryParameter("hourly", "cloudcover,visibility,precipitation_probability,temperature_2m")
            .addQueryParameter("forecast_days", "$days")
            .build()

        return simpleGetRequest(client, url)
    }

    @Tool("The daily weather forecast")
    fun weatherForecastDaily(latitude: Double, longitude: Double, days: Int): String {
        logger.info("Daily weather forecast for $latitude, $longitude, days: $days")

        if (latitude == 0.0 && longitude == 0.0) {
            return "Unknown location"
        }

        val url = HttpUrl.Builder()
            .scheme("https")
            .host("api.open-meteo.com")
            .addPathSegment("v1")
            .addPathSegment("forecast")
            .addQueryParameter("latitude", latitude.toString())
            .addQueryParameter("longitude", longitude.toString())
            .addQueryParameter("timezone", "auto")
            .addQueryParameter("daily", "temperature_2m_min,temperature_2m_max,precipitation_sum,precipitation_hours,precipitation_probability_max,precipitation_probability_min,precipitation_probability_mean,weathercode")
            .addQueryParameter("forecast_days", "$days")
            .build()

        return simpleGetRequest(client, url)
    }
}
