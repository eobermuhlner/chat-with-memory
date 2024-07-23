package ch.obermuhlner.chat.tools

import dev.langchain4j.agent.tool.Tool
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory

class PublicTransportSwitzerland {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    val client = OkHttpClient()

    @Tool("Get locations of public transport")
    fun getLocations(query: String? = null, x: Double? = null, y: Double? = null, type: String? = null): String {
        logger.info("Getting locations with query: $query, coordinates: ($x, $y), type: $type")

        val urlBuilder = HttpUrl.Builder()
            .scheme("https")
            .host("transport.opendata.ch")
            .addPathSegment("v1")
            .addPathSegment("locations")

        query?.let { urlBuilder.addQueryParameter("query", it) }
        x?.let { urlBuilder.addQueryParameter("x", it.toString()) }
        y?.let { urlBuilder.addQueryParameter("y", it.toString()) }
        type?.let { urlBuilder.addQueryParameter("type", it) }

        val url = urlBuilder.build()

        return simpleGetRequest(client, url)
    }

    @Tool("Get public transport connections between two stations")
    fun getConnections(from: String, to: String, via: List<String>? = null, date: String? = null, time: String? = null, isArrivalTime: Int? = null, transportations: List<String>? = null, limit: Int? = null, page: Int? = null): String {
        logger.info("Getting connections from: $from, to: $to, via: $via, date: $date, time: $time, isArrivalTime: $isArrivalTime, transportations: $transportations, limit: $limit, page: $page")

        val urlBuilder = HttpUrl.Builder()
            .scheme("https")
            .host("transport.opendata.ch")
            .addPathSegment("v1")
            .addPathSegment("connections")
            .addQueryParameter("from", from)
            .addQueryParameter("to", to)

        via?.forEach { urlBuilder.addQueryParameter("via[]", it) }
        date?.let { urlBuilder.addQueryParameter("date", it) }
        time?.let { urlBuilder.addQueryParameter("time", it) }
        isArrivalTime?.let { urlBuilder.addQueryParameter("isArrivalTime", it.toString()) }
        transportations?.forEach { urlBuilder.addQueryParameter("transportations[]", it) }
        limit?.let { urlBuilder.addQueryParameter("limit", it.toString()) }
        page?.let { urlBuilder.addQueryParameter("page", it.toString()) }

        val url = urlBuilder.build()

        return simpleGetRequest(client, url)
    }

    @Tool("Get public transport leaving a specific station")
    fun getStationBoard(station: String, id: String? = null, limit: Int? = null, transportations: List<String>? = null, datetime: String? = null, type: String? = null): String {
        logger.info("Getting stationboard for station: $station, id: $id, limit: $limit, transportations: $transportations, datetime: $datetime, type: $type")

        val urlBuilder = HttpUrl.Builder()
            .scheme("https")
            .host("transport.opendata.ch")
            .addPathSegment("v1")
            .addPathSegment("stationboard")
            .addQueryParameter("station", station)

        id?.let { urlBuilder.addQueryParameter("id", it) }
        limit?.let { urlBuilder.addQueryParameter("limit", it.toString()) }
        transportations?.forEach { urlBuilder.addQueryParameter("transportations[]", it) }
        datetime?.let { urlBuilder.addQueryParameter("datetime", it) }
        type?.let { urlBuilder.addQueryParameter("type", it) }

        val url = urlBuilder.build()

        return simpleGetRequest(client, url)
    }

    private fun simpleGetRequest(client: OkHttpClient, url: HttpUrl): String {
        val request = okhttp3.Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            return if (response.isSuccessful) {
                response.body?.string() ?: "No response body"
            } else {
                "Request failed with code ${response.code}"
            }
        }
    }
}
