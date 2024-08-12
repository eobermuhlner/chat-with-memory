package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.model.Tool
import ch.obermuhlner.chat.tools.GitHubFiles
import ch.obermuhlner.chat.tools.GoogleSearch
import ch.obermuhlner.chat.tools.News
import ch.obermuhlner.chat.tools.PublicTransportSwitzerland
import ch.obermuhlner.chat.tools.Weather
import ch.obermuhlner.chat.tools.WebPage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ToolService(
    @Value("\${news.api-key:demo}") private val newsApiKey: String,
    @Value("\${google.api-key:demo}") private val googleApiKey: String,
    @Value("\${google.cse-id:demo}") private val googleCseId: String,
) {
    fun getAllTools(): List<Tool> {
        return Tool.entries.toList()
    }

    fun getToolInstances(tools: Collection<Tool>, githubApiKey: String): List<Any> {
        return tools.map { getToolInstance(it, githubApiKey) }
    }

    private fun getToolInstance(tool: Tool, githubApiKey: String): Any {
        return when (tool) {
            Tool.News -> News(newsApiKey)
            Tool.Weather -> Weather()
            Tool.WebPage -> WebPage()
            Tool.GitHubFiles -> GitHubFiles(githubApiKey)
            Tool.PublicTransportSwitzerland -> PublicTransportSwitzerland()
            Tool.GoogleSearch -> GoogleSearch(googleApiKey, googleCseId)
        }
    }
}