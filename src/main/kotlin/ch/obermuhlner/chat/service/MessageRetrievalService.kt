package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.entity.ChatMessageEntity
import dev.langchain4j.data.document.Metadata
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import dev.langchain4j.store.embedding.EmbeddingStore
import org.springframework.stereotype.Service

@Service
class MessageRetrievalService(
    private val embeddingModel: EmbeddingModel,
    private val messageEmbeddingStore: EmbeddingStore<TextSegment>,
) {

    companion object {
        private const val METADATA_MESSAGE_ID = "message_id"
        private const val METADATA_TYPE = "type"
        private const val METADATA_SENDER = "sender"
        private const val METADATA_TIMESTAMP = "Timestamp"
    }

    fun addMessage(message: ChatMessageEntity) {
        val segment = createTextSegment(message)
        val embedding = embeddingModel.embed(segment).content()
        messageEmbeddingStore.add(embedding, segment)
    }

    private fun createTextSegment(message: ChatMessageEntity): TextSegment {
        return TextSegment(
            message.text,
            Metadata(
                mapOf(
                    METADATA_MESSAGE_ID to message.id,
                    METADATA_TYPE to message.messageType.name,
                    METADATA_SENDER to (message.sender?.name ?: "User"),
                    METADATA_TIMESTAMP to message.timestamp.toString()
                )
            )
        )
    }

    fun retrieveMessageIds(text: String, maxResults: Int): List<Long> {
        val embedding = embeddingModel.embed(text).content()
        val request = EmbeddingSearchRequest.builder()
            .queryEmbedding(embedding)
            .maxResults(maxResults)
            .build()
        val searchResult = messageEmbeddingStore.search(request)

        return searchResult.matches().map { it.embedded().metadata().getLong(METADATA_MESSAGE_ID) }
    }
}
