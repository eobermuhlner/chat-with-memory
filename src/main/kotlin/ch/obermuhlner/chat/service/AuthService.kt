package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.entity.UserEntity
import ch.obermuhlner.chat.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
) {
    fun getCurrentUserId(): Long {
        return getCurrentUserEntity().id!!
    }

    fun getCurrentUserEntity(): UserEntity {
        val authentication = SecurityContextHolder.getContext().authentication
        val userDetails = authentication.principal as UserDetails
        return userRepository.findByUsername(userDetails.username)!!
    }
}