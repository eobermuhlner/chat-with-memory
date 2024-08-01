package ch.obermuhlner.chat.controller

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnProperty(name = ["config.security.auth.enabled"], havingValue = "false")
class NoAuthController {

    @GetMapping("/login-required")
    fun loginRequired(): Boolean {
        return false
    }
}

