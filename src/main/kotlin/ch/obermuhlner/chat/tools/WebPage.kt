package ch.obermuhlner.chat.tools

import dev.langchain4j.agent.tool.Tool
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory

class WebPage {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    val client = OkHttpClient()

    @Tool("The webpage")
    fun webpage(url: String): String {
        logger.info("Webpage request for $url")

        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val contentType = response.header("Content-Type")
            val content = response.body?.string()
            val extractedContent = when {
                contentType == null || content == null -> "No content available"
                contentType.contains("text/html") -> extractHtmlTextContent(content)
                contentType.contains("application/json") -> content
                contentType.contains("text/plain") -> content
                else -> "Unsupported content type: $contentType"
            }
            logger.info("Extracted webpage content: ${extractedContent.length} characters (original content: ${content?.length} characters")
            return extractedContent
        } else {
            return "Request failed: ${response.code}"
        }
    }

    private fun extractHtmlTextContent(html: String): String {
        val document = Jsoup.parse(html)
        return document.body().text()
    }
}
