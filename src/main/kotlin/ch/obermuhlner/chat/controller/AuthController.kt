package ch.obermuhlner.chat.controller

import ch.obermuhlner.chat.auth.JwtUtil
import ch.obermuhlner.chat.model.JwtRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnProperty(name = ["config.security.auth.enabled"], havingValue = "true")
class AuthController(
    @Autowired private val authenticationManager: AuthenticationManager,
    @Autowired private val userDetailsService: UserDetailsService
) {

    @GetMapping("/login-required")
    fun loginRequired(): Boolean {
        return true
    }

    @PostMapping("/login")
    fun createAuthenticationToken(@RequestBody authenticationRequest: JwtRequest): ResponseEntity<*> {
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(authenticationRequest.username, authenticationRequest.password)
            )
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password")
        }

        val userDetails = userDetailsService.loadUserByUsername(authenticationRequest.username)
        val jwt = JwtUtil.generateToken(userDetails.username, listOf()) // FIXME roles

        return ResponseEntity.ok(mapOf("token" to jwt))
    }
}

