package ch.obermuhlner.chat.config

import ch.obermuhlner.chat.entity.AssistantEntity
import ch.obermuhlner.chat.entity.ChatEntity
import ch.obermuhlner.chat.entity.RoleEntity
import ch.obermuhlner.chat.entity.UserEntity
import ch.obermuhlner.chat.model.Tool
import ch.obermuhlner.chat.repository.AssistantRepository
import ch.obermuhlner.chat.repository.ChatRepository
import ch.obermuhlner.chat.repository.RoleRepository
import ch.obermuhlner.chat.repository.UserRepository
import ch.obermuhlner.chat.service.ChatService.Companion.NO_ANSWER
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class DataInitializerConfig(
    @Value("\${admin.username}") val adminUsername: String,
    @Value("\${admin.password}") val adminPassword: String,
) {

    companion object {
        val CHAT_TITLE_GENERIC = "Generic Chat"
        val CHAT_TITLE_HEALTH = "Health Chat"
        val CHAT_TITLE_SOFTWARE_DEVELOPMENT = "Software Development"
        val CHAT_TITLE_ASTRONOMY = "Astronomy"
    }

    @Bean
    fun dataInitializer(
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder,
        roleRepository: RoleRepository,
        assistantRepository: AssistantRepository,
        chatRepository: ChatRepository
    ): ApplicationRunner {
        return ApplicationRunner {
            if (roleRepository.count() == 0L) {
                roleRepository.save(RoleEntity().apply{
                    name = "ROLE_ADMIN"
                })
                roleRepository.save(RoleEntity().apply{
                    name = "ROLE_USER"
                })
            }

            val adminUser = if (userRepository.findByUsername(adminUsername) == null) {
                val adminRole = roleRepository.findByName("ROLE_ADMIN")!!
                userRepository.save(UserEntity().apply {
                    username = adminUsername
                    password = passwordEncoder.encode(adminPassword)
                    roles.add(adminRole)
                })
            } else {
                userRepository.findByUsername(adminUsername)
            }

            if (assistantRepository.count() == 0L) {
                val chatGeneric = chatRepository.save(ChatEntity().apply {
                    title = CHAT_TITLE_GENERIC
                    prompt = """
                        This is a generic chat about all topics.
                        
                        If you have no relevant answer or the answer was already given, respond with $NO_ANSWER.
                    """.trimIndent()
                    user = adminUser
                })

                val chatHealth = chatRepository.save(ChatEntity().apply {
                    title = CHAT_TITLE_HEALTH
                    prompt = """
                        This chat is about health, fitness and lifestyle.
                        
                        If you have no relevant answer or the answer was already given, respond with $NO_ANSWER.
                    """.trimIndent()
                    user = adminUser
                })

                val chatSoftwareDevelopment = chatRepository.save(ChatEntity().apply {
                    title = CHAT_TITLE_SOFTWARE_DEVELOPMENT
                    prompt = """
                        This chat is about software development.
                        If not specified otherwise, focus on the following topics:
                        - Kotlin
                        - Java
                        - Spring
                        - JPA
                        - JOOQ
                        - Kafka
                        - PostgreSQL
                        - H2
                        
                        If you have no relevant answer or the answer was already given, respond with $NO_ANSWER.
                    """.trimIndent()
                    user = adminUser
                })

                val chatAstronomy = chatRepository.save(ChatEntity().apply {
                    title = CHAT_TITLE_ASTRONOMY
                    prompt = """
                        This chat is about astronomy and astrophotgraphy.
                        
                        If you have no relevant answer or the answer was already given, respond with $NO_ANSWER.
                    """.trimIndent()
                    user = adminUser
                })


                assistantRepository.save(AssistantEntity().apply {
                    name = "Pedro"
                    description = "Software developer"
                    prompt = """
                        You are Pedro, an assistant and professional software developer.
                        Your answers are always concise and to the point.
                    """.trimIndent()
                    sortIndex = 30
                    chats.add(chatSoftwareDevelopment)
                    user = adminUser
                })

                assistantRepository.save(AssistantEntity().apply {
                    name = "Sandra"
                    description = "Health advisor"
                    prompt = """
                        You are Sandra, a professional health advisor and life coach.
                        You provide accurate and reliable health, fitness, nutrition and general life advice.
                        You communicate with empathy and understanding, ensuring your answers are clear and actionable.
                        Your answers are short but friendly.
                        You use emojis a lot.
                    """.trimIndent()
                    sortIndex = 80
                    chats.add(chatHealth)
                    user = adminUser
                })

                assistantRepository.save(AssistantEntity().apply {
                    name = "Carl"
                    description = "Fact checker"
                    prompt = """
                        You are Carl, a professional fact checker.
                        Your role is to verify the correctness of all answers.
                        Your responses are always concise and to the point.
                        You respond only under these conditions:
                        - When you detect incorrect information
                        - When you are addressed directly
                        In all other cases you will not respond.
                    """.trimIndent()
                    sortIndex = 90
                    chats.add(chatHealth)
                    user = adminUser
                })

                assistantRepository.save(AssistantEntity().apply {
                    name = "Ada"
                    description = "Code reviewer"
                    prompt = """
                        You are Ada, a professional code reviewer.
                        Your role is to do code reviews and provide constructive criticism.
                        Your responses are always concise and to the point.
                        You respond only under these conditions:
                        - When somebody asks explicitly for a code review
                        - When you are addressed directly
                        - When you see code written by other assistants that has issues
                        In all other cases you will not respond.
                    """.trimIndent()
                    sortIndex = 90
                    chats.add(chatSoftwareDevelopment)
                    user = adminUser
                })

                assistantRepository.save(AssistantEntity().apply {
                    name = "John Doe"
                    description = "Assistant"
                    prompt = """
                        You are John, a helpful assistant.
                        Your responses are always friendly, concise and to the point.
                        You use emojis sparingly.
                    """.trimIndent()
                    sortIndex = 90
                    tools = listOf(Tool.News)
                    chats.add(chatGeneric)
                    user = adminUser
                })

                assistantRepository.save(AssistantEntity().apply {
                    name = "Jane Doe"
                    description = "Assistant"
                    prompt = """
                        You are Jane, a helpful assistant.
                        Your responses are always friendly, concise and to the point.
                        You use emojis sparingly.
                    """.trimIndent()
                    sortIndex = 90
                    tools = listOf(Tool.News)
                    user = adminUser
                })

                assistantRepository.save(AssistantEntity().apply {
                    name = "Galileo"
                    description = "Astronomer"
                    prompt = """
                        You are Galileo, an astronomer and astrophotographer.
                        Your responses are always concise and to the point.
                    """.trimIndent()
                    sortIndex = 50
                    tools = listOf(Tool.Weather)
                    chats.add(chatAstronomy)
                    user = adminUser
                })

                assistantRepository.save(AssistantEntity().apply {
                    name = "Isaac"
                    description = "Librarian"
                    prompt = """
                        You are Isaac, a professional expert librarian.
                        You answer questions concisely after consulting stored documents and only provide information from the documents.
                        You always cite sources.
                    """.trimIndent()
                    sortIndex = 50
                    chats.add(chatAstronomy)
                    user = adminUser
                })
            }
        }
    }
}
