package ch.obermuhlner.chat.service

import dev.langchain4j.data.document.Metadata
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import java.io.File
import java.time.Instant

class MessageRetrievalService {

    val METADATA_TYPE = "Type"
    val METADATA_TIMESTAMP = "Timestamp"

    val EMBEDDING_FILE = "./data/embeddings.json"

    val embeddingStore = if (File(EMBEDDING_FILE).exists()) {
        InMemoryEmbeddingStore.fromFile(EMBEDDING_FILE)
    } else {
        InMemoryEmbeddingStore()
    }
    val embeddingModel = AllMiniLmL6V2EmbeddingModel()

    fun addMessage(message: Message) {
        val segment = TextSegment(message.text, Metadata(mapOf(METADATA_TYPE to message.messageType.name, METADATA_TIMESTAMP to message.timestamp.toString())))
        val embedding = embeddingModel.embed(segment).content()
        embeddingStore.add(embedding, segment)
        embeddingStore.serializeToFile(EMBEDDING_FILE)
    }

    fun retrieveMessages(text: String): List<Message> {
        val embedding = embeddingModel.embed(text).content()
        val request = EmbeddingSearchRequest.builder().queryEmbedding(embedding).build()
        val searchResult = embeddingStore.search(request)
        val result = searchResult.matches()
            .map {
                Message(
                    MessageType.valueOf(it.embedded().metadata().getString(METADATA_TYPE)),
                    it.embedded().text(),
                    Instant.parse(it.embedded().metadata().getString(METADATA_TIMESTAMP)),
                )
            }
        return result
    }
}