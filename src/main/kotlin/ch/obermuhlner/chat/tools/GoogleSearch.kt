package ch.obermuhlner.chat.tools;

import dev.langchain4j.agent.tool.Tool
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory

class GoogleSearch(private val apiKey: String, private val cseId: String) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    val client = OkHttpClient()

    @Tool("Web search using Google")
    fun webSearch(query: String, num: Int = 10, start: Int = 1, language: String? = null, siteSearch: String? = null): String {
        logger.info("Performing web search for query: $query")

        val urlBuilder = HttpUrl.Builder()
            .scheme("https")
            .host("www.googleapis.com")
            .addPathSegment("customsearch")
            .addPathSegment("v1")
            .addQueryParameter("q", query)
            .addQueryParameter("key", apiKey)
            .addQueryParameter("cx", cseId)
            .addQueryParameter("num", num.toString())
            .addQueryParameter("start", start.toString())

        language?.let { urlBuilder.addQueryParameter("hl", it) }
        siteSearch?.let { urlBuilder.addQueryParameter("siteSearch", it) }

        val url = urlBuilder.build()

        return simpleGetRequest(client, url)
    }

    @Tool("Image search using Google")
    fun imageSearch(query: String, num: Int = 10, start: Int = 1, language: String? = null, siteSearch: String? = null): String {
        logger.info("Performing image search for query: $query")

        val urlBuilder = HttpUrl.Builder()
            .scheme("https")
            .host("www.googleapis.com")
            .addPathSegment("customsearch")
            .addPathSegment("v1")
            .addQueryParameter("q", query)
            .addQueryParameter("key", apiKey)
            .addQueryParameter("cx", cseId)
            .addQueryParameter("num", num.toString())
            .addQueryParameter("start", start.toString())
            .addQueryParameter("searchType", "image")

        language?.let { urlBuilder.addQueryParameter("hl", it) }
        siteSearch?.let { urlBuilder.addQueryParameter("siteSearch", it) }

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
