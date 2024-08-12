package ch.obermuhlner.chat.tools

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dev.langchain4j.agent.tool.Tool
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Headers
import org.slf4j.LoggerFactory
import java.io.IOException

class GitHubFiles(val githubApiKey: String) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    private val client = OkHttpClient()
    private val objectMapper = ObjectMapper()

    @Tool("List all files in a github repository")
    fun listFiles(owner: String, repo: String, branch: String = "main"): String {
        logger.info("Github list all files for $owner/$repo on branch $branch")

        val url = "https://api.github.com/repos/$owner/$repo/git/trees/$branch?recursive=1"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body?.string() ?: throw IOException("Response body is null")

            // Parse JSON using Jackson
            val rootNode: JsonNode = objectMapper.readTree(responseBody)
            val files = mutableListOf<String>()

            val treeNode: JsonNode = rootNode["tree"]
            if (treeNode.isArray) {
                for (fileNode in treeNode) {
                    val type = fileNode["type"].asText()
                    if (type == "blob") {
                        val filePath = fileNode["path"].asText()
                        files.add(filePath)
                    }
                }
            }

            return files.joinToString(separator = "\n")
        }
    }

    @Tool("Read file from a github repository")
    fun readFile(owner: String, repo: String, branch: String, path: String): String {
        logger.info("Github read file $owner $repo $branch $path")
        val url = "https://raw.githubusercontent.com/$owner/$repo/$branch/$path"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            return response.body?.string() ?: "no content"
        }
    }

    private fun buildHeaders(): Headers {
        return Headers.Builder()
            .add("Authorization", "Bearer $githubApiKey")
            .add("Accept", "application/vnd.github.v3+json")
            .build()
    }
}

fun main() {
    val github = GitHubFiles("")
    github.readFile("eobermuhlner", "chat-with-memory", "master", "src/main/kotlin/ch/obermuhlner/chat/service/CustomerUserDetailsService.kt")
}