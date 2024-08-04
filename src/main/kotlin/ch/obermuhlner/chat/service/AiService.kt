package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.model.Tool
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName
import dev.langchain4j.service.AiServices
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AiService(
    private val toolService: ToolService,
) {
    interface AiChat {
        fun generate(prompt: String): String
    }

    fun generate(prompt: String, openAiApiKey: String): String {
        val model = getModel(openAiApiKey)

        println("=================================================================")
        println("PROMPT:")
        println(prompt)
        println("TOKENS: ${model.estimateTokenCount(prompt)}")
        println()

        return model.generate(prompt)
    }

    fun generateWithTools(prompt: String, tools: Collection<Tool>, openAiApiKey: String): String {
        val toolInstances = toolService.getToolInstances(tools)

        val aiChat = AiServices.builder(AiChat::class.java)
            .chatLanguageModel(getModel(openAiApiKey))
            .tools(toolInstances)
            .build()

        return aiChat.generate(prompt)
    }

    private fun getModel(openAiApiKey: String): OpenAiChatModel {
        val key = openAiApiKey.ifBlank { "demo" }

        val name = if (key == "demo") {
            OpenAiChatModelName.GPT_3_5_TURBO
        } else {
            OpenAiChatModelName.GPT_4_O
        }

        return OpenAiChatModel.builder()
            .apiKey(key)
            .modelName(name)
            .build()
    }
}
