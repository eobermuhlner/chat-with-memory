package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.model.Tool
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName
import dev.langchain4j.service.AiServices
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AiService(
    @Value("\${openai.api-key:demo}") private val openAiApiKey: String,
    private val toolService: ToolService,
) {
    interface AiChat {
        fun generate(prompt: String): String
    }

    private val model = OpenAiChatModel.builder()
        .apiKey(openAiApiKey)
        //.modelName(OpenAiChatModelName.GPT_3_5_TURBO)
        .modelName(OpenAiChatModelName.GPT_4_O)
        .build()

    fun generate(prompt: String): String {
        println("=================================================================")
        println("PROMPT:")
        println(prompt)
        println("TOKENS: ${model.estimateTokenCount(prompt)}")
        println()

        return model.generate(prompt)
    }

    fun generateWithTools(prompt: String, tools: Collection<Tool>): String {
        val toolInstances = toolService.getToolInstances(tools)

        val aiChat = AiServices.builder(AiChat::class.java)
            .chatLanguageModel(model)
            .tools(toolInstances)
            .build()

        return aiChat.generate(prompt)
    }
}