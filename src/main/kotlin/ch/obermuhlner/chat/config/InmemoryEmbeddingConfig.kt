package ch.obermuhlner.chat.config

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("test")
class InmemoryEmbeddingConfig {
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
