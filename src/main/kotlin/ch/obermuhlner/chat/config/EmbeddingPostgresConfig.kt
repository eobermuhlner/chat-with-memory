package ch.obermuhlner.chat.config

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore.PgVectorEmbeddingStoreBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("postgres")
class EmbeddingPostgresConfig {
    @Autowired
    private lateinit var postgresProperties: PostgresProperties

    @Bean
    fun embeddingModel(): EmbeddingModel {
        return AllMiniLmL6V2EmbeddingModel()
    }

    @Bean
    fun messageEmbeddingStore(): EmbeddingStore<TextSegment> {
        return PgVectorEmbeddingStore.builder()
            .host(postgresProperties.host)
            .port(postgresProperties.port)
            .database(postgresProperties.database)
            .user(postgresProperties.username)
            .password(postgresProperties.password)
            .table("message_embedding")
            .dimension(384)
            .build()
    }

    @Bean
    fun documentEmbeddingStore(): EmbeddingStore<TextSegment> {
        return PgVectorEmbeddingStore.builder()
            .host(postgresProperties.host)
            .port(postgresProperties.port)
            .database(postgresProperties.database)
            .user(postgresProperties.username)
            .password(postgresProperties.password)
            .table("document_embedding")
            .dimension(384)
            .build();
    }
}