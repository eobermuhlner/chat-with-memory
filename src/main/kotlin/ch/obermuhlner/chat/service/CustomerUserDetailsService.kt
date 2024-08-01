package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.entity.UserEntity
import ch.obermuhlner.chat.repository.UserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val userEntity: UserEntity = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found with username: $username")

        val authorities: Set<GrantedAuthority> = userEntity.roles.map { SimpleGrantedAuthority(it.name) }.toSet()

        return User(userEntity.username, userEntity.password, authorities)
    }
}
