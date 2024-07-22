package ch.obermuhlner.chat.tools

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import dev.langchain4j.agent.tool.Tool
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory

class News(private val newsApiKey: String) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    private val client = OkHttpClient()
    private val objectMapper = ObjectMapper()

    @JsonIgnoreProperties(ignoreUnknown = true)
    class NewsArticle() {
        var title: String? = null
        var description: String? = null
        var url: String? = null
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class NewsResponse() {
        var status: String? = null
        var articles: List<NewsArticle> = mutableListOf()
    }

    enum class NewsCategory {
        business,
        entertainment,
        general,
        health,
        science,
        sports,
        technology
    }

    @Tool("Fetch the latest news headlines")
    fun getLatestNews(category: NewsCategory, keywords: String, language: String = "en"): String {
        logger.info("Webpage request for $category, $keywords, $language")

        val url = HttpUrl.Builder()
            .scheme("https")
            .host("newsapi.org")
            .addPathSegment("v2")
            .addPathSegment("top-headlines")
            .addQueryParameter("q", keywords)
            .addQueryParameter("category", category.name)
            .addQueryParameter("language", language)
            .addQueryParameter("apiKey", newsApiKey)
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()

        return if (response.isSuccessful) {
            val responseData = response.body?.string()
            if (responseData != null) {
                val newsResponse = objectMapper.readValue(responseData, NewsResponse::class.java)
                if (newsResponse.status == "ok") {
                    newsResponse.articles.joinToString(separator = "\n\n") {
                        "Title: ${it.title}\nDescription: ${it.description}\nURL: ${it.url}"
                    }
                } else {
                    "Failed to parse news response"
                }
            } else {
                "No response data"
            }
        } else {
            "Request failed: ${response.code}"
        }
    }
}
