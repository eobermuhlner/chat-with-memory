package ch.obermuhlner.chat.config

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@ConditionalOnProperty(name = ["config.db.h2-memory.enabled"], havingValue = "true")
class InMemoryEmbeddingConfig {
    @Bean
    fun embeddingModel(): EmbeddingModel {
        return AllMiniLmL6V2EmbeddingModel()
    }

    @Bean
    fun messageEmbeddingStore(): EmbeddingStore<TextSegment> {
        return InMemoryEmbeddingStore<TextSegment>()
    }

    @Bean
    fun documentEmbeddingStore(): EmbeddingStore<TextSegment> {
        return InMemoryEmbeddingStore<TextSegment>()
    }
}
