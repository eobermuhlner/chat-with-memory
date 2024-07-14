package ch.obermuhlner.chat

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter


@Configuration
class CorsConfig {
    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowCredentials = false // Do not allow credentials
        config.addAllowedOrigin("*") // Allow all origins
        config.addAllowedHeader("*") // Allow all headers
        config.addAllowedMethod("*") // Allow all methods
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }
}
