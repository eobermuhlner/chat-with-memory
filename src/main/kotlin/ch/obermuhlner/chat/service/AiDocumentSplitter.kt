package ch.obermuhlner.chat.service

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentSplitter
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName


class AiDocumentSplitter(
    val openAiApiKey: String = "demo",
    val modelName: String = if (openAiApiKey == "demo") OpenAiChatModelName.GPT_3_5_TURBO.name else "gpt-4o-mini",
    val batchSize: Int = 100
): DocumentSplitter {
    private val model = OpenAiChatModel.builder()
        .apiKey(openAiApiKey)
        .modelName(modelName)
        .build()

    val systemPrompt = """
        Separate the following text into logical segments.
        Make the segments as long as possible but still consistent.
        Keep the line structure of the text intact.
        Use the separator === SEGMENT === between the segments.
    """.trimIndent()

    override fun split(document: Document): MutableList<TextSegment> {
        val result = mutableListOf<TextSegment>()

        val lines = document.text().lines()

        var start = 0
        var carryOver = ""
        var segmentIndex = 0

        while (start < lines.size) {
            val end = (start + batchSize).coerceAtMost(lines.size)
            println("Splitting $start to $end out of ${lines.size} lines")
            val subText = (listOf(carryOver) + lines.subList(start, end)).joinToString("\n")
            val answer = model.generate(systemPrompt + "\n\n" + subText)
            val splitText = answer.split("=== SEGMENT ===")

            for (segment in splitText.dropLast(1)) {
                if (segment.isNotBlank()) {
                    println(" adding segment $segmentIndex - ${segment.length} characters")
                    result.add(createTextSegment(document, segment, segmentIndex++))
                }
            }

            carryOver = splitText.last().trim()

            start += batchSize
        }

        if (carryOver.isNotBlank()) {
            println(" adding segment $segmentIndex - ${carryOver.length} characters")
            result.add(createTextSegment(document, carryOver, segmentIndex))
        }
        return result
    }

    private fun createTextSegment(
        document: Document,
        segment: String,
        segmentIndex: Int,
    ): TextSegment {
        val metadata = document.metadata().copy().put("index", segmentIndex)
        return TextSegment.from(segment.trim(), metadata)
    }
}