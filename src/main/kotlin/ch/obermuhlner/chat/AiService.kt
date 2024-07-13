package ch.obermuhlner.chat

import dev.langchain4j.model.openai.OpenAiChatModel

class AiService(val apiKey: String) {
    private val model = OpenAiChatModel.withApiKey(apiKey)

    fun generate(prompt: String): String {
        return model.generate(prompt)
    }
}