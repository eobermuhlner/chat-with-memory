package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.entity.ChatMessageEntity
import dev.langchain4j.data.document.Metadata
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import org.springframework.stereotype.Service
import java.io.File

@Service
class MessageRetrievalService(
    private val embeddingModel: EmbeddingModel = AllMiniLmL6V2EmbeddingModel(),
    private val embeddingStore: InMemoryEmbeddingStore<TextSegment> = initializeEmbeddingStore(),
) {

    companion object {
        private const val METADATA_TYPE = "Type"
        private const val METADATA_SENDER = "Sender"
        private const val METADATA_TIMESTAMP = "Timestamp"
        private const val EMBEDDING_FILE = "./data/embeddings.json"

        private fun initializeEmbeddingStore(): InMemoryEmbeddingStore<TextSegment> {
            return if (File(EMBEDDING_FILE).exists()) {
                InMemoryEmbeddingStore.fromFile(EMBEDDING_FILE)
            } else {
                InMemoryEmbeddingStore()
            }
        }
    }

    fun addMessage(message: ChatMessageEntity) {
        val segment = createTextSegment(message)
        val embedding = embeddingModel.embed(segment).content()
        embeddingStore.add(message.id.toString(), embedding, segment)
        saveEmbeddingStore()
    }

    private fun createTextSegment(message: ChatMessageEntity): TextSegment {
        return TextSegment(
            message.text,
            Metadata(
                mapOf(
                    METADATA_TYPE to message.messageType.name,
                    METADATA_SENDER to (message.sender?.name ?: "User"),
                    METADATA_TIMESTAMP to message.timestamp.toString()
                )
            )
        )
    }

    private fun saveEmbeddingStore() {
        embeddingStore.serializeToFile(EMBEDDING_FILE)
    }

    fun retrieveMessageIds(text: String, maxResults: Int): List<Long> {
        val embedding = embeddingModel.embed(text).content()
        val request = EmbeddingSearchRequest.builder().queryEmbedding(embedding).maxResults(maxResults).build()
        val searchResult = embeddingStore.search(request)

        return searchResult.matches().map { it.embeddingId().toLong() }
    }
}
