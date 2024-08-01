package ch.obermuhlner.chat.controller

import ch.obermuhlner.chat.model.PasswordChangeRequest
import ch.obermuhlner.chat.model.User
import ch.obermuhlner.chat.service.RoleService
import ch.obermuhlner.chat.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/roles")
class RoleController(
    private val roleService: RoleService
) {

    @GetMapping
    fun findAll(): List<String> {
        return roleService.findAll()
    }
}
