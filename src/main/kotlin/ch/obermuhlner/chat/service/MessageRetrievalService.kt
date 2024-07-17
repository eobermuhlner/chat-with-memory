package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.model.MessageType
import dev.langchain4j.data.document.Metadata
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import org.springframework.stereotype.Service
import java.io.File
import java.time.Instant

@Service
class MessageRetrievalService(
    private val embeddingModel: EmbeddingModel = AllMiniLmL6V2EmbeddingModel(),
    private val embeddingStore: InMemoryEmbeddingStore<TextSegment> = initializeEmbeddingStore()
) {

    companion object {
        private const val METADATA_TYPE = "Type"
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

    fun addMessage(message: Message) {
        val segment = createTextSegment(message)
        val embedding = embeddingModel.embed(segment).content()
        embeddingStore.add(embedding, segment)
        saveEmbeddingStore()
    }

    private fun createTextSegment(message: Message): TextSegment {
        return TextSegment(
            message.text,
            Metadata(
                mapOf(
                    METADATA_TYPE to message.messageType.name,
                    METADATA_TIMESTAMP to message.timestamp.toString()
                )
            )
        )
    }

    private fun saveEmbeddingStore() {
        try {
            embeddingStore.serializeToFile(EMBEDDING_FILE)
        } catch (e: Exception) {
            // Handle the exception (e.g., log it)
            println("Error saving embedding store: ${e.message}")
        }
    }

    fun retrieveMessages(text: String): List<Message> {
        val embedding = embeddingModel.embed(text).content()
        val request = EmbeddingSearchRequest.builder().queryEmbedding(embedding).build()
        val searchResult = embeddingStore.search(request)

        return searchResult.matches().map {
            Message(
                MessageType.valueOf(it.embedded().metadata().getString(METADATA_TYPE)),
                it.embedded().text(),
                Instant.parse(it.embedded().metadata().getString(METADATA_TIMESTAMP))
            )
        }
    }
}
