package ch.obermuhlner.chat.tools

import dev.langchain4j.agent.tool.Tool
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.slf4j.LoggerFactory
import java.io.IOException

class GitHubFiles {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    val client = OkHttpClient()

    @Tool("List all files in a github repository")
    fun listFiles(owner: String, repo: String): String {
        logger.info("Github list files for $owner $repo")
        return listFilesRecursive(owner, repo, "").joinToString(separator = "\n")
    }

    private fun listFilesRecursive(owner: String, repo: String, path: String): List<String> {
        val url = if (path.isEmpty()) {
            "https://api.github.com/repos/$owner/$repo/contents"
        } else {
            "https://api.github.com/repos/$owner/$repo/contents/$path"
        }
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body?.string() ?: throw IOException("Response body is null")
            val jsonArray = JSONArray(responseBody)
            val files = mutableListOf<String>()

            for (i in 0 until jsonArray.length()) {
                val fileObject = jsonArray.getJSONObject(i)
                val type = fileObject.getString("type")
                val filePath = fileObject.getString("path")

                if (type == "file") {
                    files.add(filePath)
                } else if (type == "dir") {
                    files.addAll(listFilesRecursive(owner, repo, filePath))
                }
            }

            return files
        }
    }

    @Tool("Read file from a github repository")
    fun readFile(owner: String, repo: String, branch: String, path: String): String {
        logger.info("Github read file $owner $repo $path")
        val url = "https://raw.githubusercontent.com/$owner/$repo/$branch/$path"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            return response.body?.string() ?: throw IOException("Response body is null")
        }
    }
}
