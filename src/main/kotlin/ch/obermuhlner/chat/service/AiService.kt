package ch.obermuhlner.chat.service

import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AiService(
    @Value("\${openai.api-key:demo}") private val apiKey: String
) {
    private val model = OpenAiChatModel.builder()
        .apiKey(apiKey)
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
}