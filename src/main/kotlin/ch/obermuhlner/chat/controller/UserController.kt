package ch.obermuhlner.chat.controller

import ch.obermuhlner.chat.model.PasswordChangeRequest
import ch.obermuhlner.chat.model.User
import ch.obermuhlner.chat.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping
    fun findAll(): List<User> {
        return userService.findAll()
    }

    @GetMapping("{id}")
    fun findById(@PathVariable id: Long): User? {
        return userService.findById(id)
    }

    @PostMapping
    fun create(@RequestBody user: User): User {
        return userService.create(user)
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody user: User): User {
        user.id = id
        return userService.update(user)
    }

    @PutMapping("{id}/password")
    fun updatePassword(
        @PathVariable id: Long,
        @RequestBody passwordChangeRequest: PasswordChangeRequest
    ): ResponseEntity<String> {
        val success = userService.changePassword(id, passwordChangeRequest.oldPassword, passwordChangeRequest.newPassword)

        return if (success) {
            ResponseEntity.ok("Password updated successfully")
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Old password is incorrect")
        }
    }

    @DeleteMapping("{id}")
    fun deleteById(@PathVariable id: Long) {
        userService.deleteById(id)
    }
}
