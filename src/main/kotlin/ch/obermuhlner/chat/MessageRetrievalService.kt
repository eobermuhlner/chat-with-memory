package ch.obermuhlner.chat

import dev.langchain4j.data.document.Metadata
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore

class MessageRetrievalService {

    val MetadataType = "Type"

    val embeddingStore = InMemoryEmbeddingStore<TextSegment>()
    val embeddingModel = AllMiniLmL6V2EmbeddingModel()

    fun addMessage(message: Message) {
        val segment = TextSegment(message.text, Metadata(mapOf(MetadataType to message.messageType.name)))
        val embedding = embeddingModel.embed(segment).content()
        embeddingStore.add(embedding, segment)
    }

    fun retrieveMessages(text: String): List<Message> {
        val embedding = embeddingModel.embed(text).content()
        val request = EmbeddingSearchRequest.builder().queryEmbedding(embedding).build()
        val searchResult = embeddingStore.search(request)
        val result = searchResult.matches()
            .map { Message(MessageType.valueOf(it.embedded().metadata().getString(MetadataType)), it.embedded().text()) }
        return result
    }
}