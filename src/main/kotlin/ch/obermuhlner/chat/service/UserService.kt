package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.entity.UserEntity
import ch.obermuhlner.chat.model.User
import ch.obermuhlner.chat.repository.AssistantRepository
import ch.obermuhlner.chat.repository.ChatRepository
import ch.obermuhlner.chat.repository.RoleRepository
import ch.obermuhlner.chat.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class UserService(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val chatRepository: ChatRepository,
    private val assistantRepository: AssistantRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional(readOnly = true)
    fun findAll(): List<User> = userRepository.findAll().map { it.toUser() }

    @Transactional(readOnly = true)
    fun findById(id: Long): User? = userRepository.findByIdOrNull(id)?.toUser()

    @Transactional(readOnly = true)
    fun currentUser(): User? {
        val user = authService.getCurrentUserEntity()
        return user.toUser()
    }

    @Transactional
    fun register(user: User): User? {
        user.roles.clear()
        return create(user)
    }

    @Transactional
    fun create(user: User): User {
        if (user.id != null) {
            throw IllegalArgumentException("Cannot create user with id")
        }
        val userEntity = user.toUserEntity(roleRepository = roleRepository)
        userEntity.password = passwordEncoder.encode(user.password)

        val templateChats = chatRepository.findAllByIsTemplate(true)
        val savedChats = chatRepository.saveAll(templateChats.map {
            it.toChat().toChatEntity().apply {
                id = null
                assistants.clear()
                documents.clear()
            }
        })

        val templateAssistants = assistantRepository.findAllByIsTemplate(true)
        val savedAssistants = assistantRepository.saveAll(templateAssistants.map {
            it.toAssistant().toAssistantEntity().apply {
                id = null
            }
        })

        savedChats.forEach {
            it.user = userEntity
            userEntity.chats.add(it)
        }
        savedAssistants.forEach {
            it.user = userEntity
            userEntity.assistants.add(it)
        }

        val savedEntity = userRepository.save(userEntity)

        return savedEntity.toUser()
    }

    @Transactional
    fun update(user: User): User {
        val existingEntity = userRepository.findById(user.id!!).getOrNull() ?: throw IllegalArgumentException("User not found: ${user.id}")
        user.toUserEntity(existingEntity, roleRepository, keepExistingPassword = true)

        val savedEntity = userRepository.save(existingEntity)

        return savedEntity.toUser()
    }

    @Transactional
    fun deleteById(id: Long) {
        val existingEntity = userRepository.findById(id).orElseThrow { EntityNotFoundException("User not found: $id") }

        userRepository.deleteById(id)
    }

    @Transactional
    fun changePassword(userId: Long, oldPassword: String, newPassword: String): Boolean {
        val userEntity = userRepository.findById(userId).getOrNull() ?: throw IllegalArgumentException("User not found: $userId")

        if (!passwordEncoder.matches(oldPassword, userEntity.password)) {
            return false
        }

        userEntity.password = passwordEncoder.encode(newPassword)
        userRepository.save(userEntity)
        return true
    }
}
