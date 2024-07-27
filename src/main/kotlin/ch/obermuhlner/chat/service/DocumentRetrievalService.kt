package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.entity.DocumentEntity
import ch.obermuhlner.chat.model.SplitterStrategy
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParserFactory
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.File

@Service
class DocumentRetrievalService(
    private val embeddingModel: EmbeddingModel = AllMiniLmL6V2EmbeddingModel(),
    private val embeddingStore: InMemoryEmbeddingStore<TextSegment> = initializeEmbeddingStore(),
    @Value("\${openai.api-key:demo}") private val openAiApiKey: String,
) {
    companion object {
        private const val METADATA_FILENAME = "filename"
        private const val METADATA_ID = "id"
        private const val EMBEDDING_FILE = "./data/document.embeddings.json"

        private fun initializeEmbeddingStore(): InMemoryEmbeddingStore<TextSegment> {
            return if (File(EMBEDDING_FILE).exists()) {
                InMemoryEmbeddingStore.fromFile(EMBEDDING_FILE)
            } else {
                InMemoryEmbeddingStore()
            }
        }
    }

    fun addDocument(documentEntity: DocumentEntity, splitterStrategy: SplitterStrategy) {
        val parser = ApacheTikaDocumentParserFactory().create()
        val document = parser.parse(ByteArrayInputStream(documentEntity.data))
        document.metadata().put(METADATA_FILENAME, documentEntity.name)
        document.metadata().put(METADATA_ID, documentEntity.id!!)
        addDocument(document, splitterStrategy)
    }

    private fun addDocument(document: Document, splitterStrategy: SplitterStrategy) {
        val id = document.metadata().getString(METADATA_FILENAME)

        val splitter = when (splitterStrategy) {
            SplitterStrategy.Paragraph -> DocumentByParagraphSplitter(2000, 0)
            SplitterStrategy.AI -> AiDocumentSplitter(openAiApiKey)
        }

        val segments = splitter.split(document)
        for (segment in segments) {
            val embedding = embeddingModel.embed(segment).content()
            embeddingStore.add(id, embedding, segment) // FIXME id = filename ?
        }
        saveEmbeddingStore()
    }

    private fun saveEmbeddingStore() {
        embeddingStore.serializeToFile(EMBEDDING_FILE)
    }

    fun getAllTextSegments(id: Long): List<TextSegment> {
        val embedding = embeddingModel.embed("content").content()
        val request = EmbeddingSearchRequest.builder()
            .queryEmbedding(embedding)
            .filter(MetadataFilterBuilder(METADATA_ID).isEqualTo(id))
            .maxResults(9999)
            .build()
        val searchResult = embeddingStore.search(request)

        return searchResult.matches().map { it.embedded() }
    }

    fun retrieveRelevantTextSegments(text: String, ids: Set<Long>, maxResults: Int, minScore: Double): List<TextSegment> {
        if (ids.isEmpty()) return emptyList()

        val embedding = embeddingModel.embed(text).content()
        val request = EmbeddingSearchRequest.builder()
            .queryEmbedding(embedding)
            .filter(MetadataFilterBuilder(METADATA_ID).isIn(ids))
            .maxResults(maxResults)
            .minScore(minScore)
            .build()
        val searchResult = embeddingStore.search(request)

        return searchResult.matches().map { it.embedded() }
    }
}
