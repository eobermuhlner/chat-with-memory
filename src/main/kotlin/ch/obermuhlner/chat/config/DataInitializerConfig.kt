package ch.obermuhlner.chat.config

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ch.obermuhlner.chat.entity.AssistantEntity
import ch.obermuhlner.chat.entity.ChatEntity
import ch.obermuhlner.chat.repository.AssistantRepository
import ch.obermuhlner.chat.repository.ChatRepository

@Configuration
class DataInitializerConfig {

    companion object {
        val INITIAL_CHAT_TITLE = "Chat"
    }

    @Bean
    fun dataInitializer(assistantRepository: AssistantRepository, chatRepository: ChatRepository): ApplicationRunner {
        return ApplicationRunner {
            if (assistantRepository.count() == 0L) {
                val savedChat = chatRepository.save(ChatEntity().apply {
                    title = INITIAL_CHAT_TITLE
                })

                assistantRepository.save(AssistantEntity().apply {
                    name = "Pedro"
                    description = "Software developer"
                    prompt = """
                        You are Pedro, an assistant and professional software developer.
                        You communicate naturally and informally.
                        Your answers are always concise and to the point.
                    """.trimIndent()
                    sortIndex = 30
                    chats.add(savedChat)
                })

                assistantRepository.save(AssistantEntity().apply {
                    name = "Sandra"
                    description = "Health advisor"
                    prompt = """
                        You are a Sandra, a professional health advisor and life coach.
                        You provide accurate and reliable health, fitness, nutrition and general life advice.
                        You communicate with empathy and understanding, ensuring your answers are clear and actionable.
                        Your answers are short but friendly.
                        You use emojis a lot.
                        I nobody answered the User question, you will give an answer.
                    """.trimIndent()
                    sortIndex = 80
                    chats.add(savedChat)
                })

                assistantRepository.save(AssistantEntity().apply {
                    name = "Carl"
                    description = "Fact checker"
                    prompt = """
                        You are Carl, a professional fact checker.
                        Your role is to verify the correctness of all answers.
                        You respond only under these conditions:
                        - When you detect incorrect information
                        - When addressed directly
                        - When messages are directed at everyone
                        Your responses are always concise and to the point.
                    """.trimIndent()
                    sortIndex = 90
                    chats.add(savedChat)
                })

            }
        }
    }
}