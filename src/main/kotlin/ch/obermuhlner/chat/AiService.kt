package ch.obermuhlner.chat

import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName

class AiService(val apiKey: String) {
    private val model = OpenAiChatModel.builder()
        .apiKey(apiKey)
        .modelName(OpenAiChatModelName.GPT_3_5_TURBO)
        .build()

    fun generate(prompt: String): String {
        return model.generate(prompt)
    }
}