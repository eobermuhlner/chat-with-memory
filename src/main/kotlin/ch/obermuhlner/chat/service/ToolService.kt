package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.model.Tool
import ch.obermuhlner.chat.tools.News
import ch.obermuhlner.chat.tools.Weather
import ch.obermuhlner.chat.tools.WebPage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ToolService(
    @Value("\${news.api-key:demo}") private val newsApiKey: String,
) {
    fun getAllTools(): List<Tool> {
        return Tool.entries.toList()
    }

    fun getToolInstances(tools: Collection<Tool>): List<Any> {
        return tools.map { getToolInstance(it) }
    }

    private fun getToolInstance(tool: Tool): Any {
        return when (tool) {
            Tool.News -> News(newsApiKey)
            Tool.Weather -> Weather()
            Tool.WebPage -> WebPage()
        }
    }
}