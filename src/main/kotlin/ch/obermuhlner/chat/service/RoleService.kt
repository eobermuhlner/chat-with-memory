package ch.obermuhlner.chat.service

import ch.obermuhlner.chat.repository.RoleRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoleService(
    private val roleRepository: RoleRepository,
) {

    @Transactional(readOnly = true)
    fun findAll(): List<String> = roleRepository.findAll().map { it.name }
}
