package ch.obermuhlner.chat.config

import ch.obermuhlner.chat.auth.JwtRequestFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.AuthenticationEntryPoint

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@ConditionalOnProperty(name = ["config.security.auth.enabled"], havingValue = "true")
class AuthSecurityConfig(
    @Autowired private val userDetailsService: UserDetailsService,
    @Autowired private val environment: Environment,
    @Autowired private val accessDeniedHandler: AccessDeniedHandler,
    @Autowired private val authenticationEntryPoint: AuthenticationEntryPoint
) {
    @Bean
    fun authenticationManager(): AuthenticationManager {
        val provider = daoAuthenticationProvider()
        return ProviderManager(provider)
    }

    @Bean
    fun daoAuthenticationProvider(): DaoAuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(userDetailsService)
        provider.setPasswordEncoder(passwordEncoder())
        return provider
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors(Customizer.withDefaults())
            .csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/login", "/login-required", "/register").permitAll()
                    .requestMatchers("/roles").hasRole("ADMIN")
                    .requestMatchers("/users/**").hasRole("ADMIN")
                    if (environment.activeProfiles.contains("dev")) {
                        authz.requestMatchers("/actuator/**").permitAll()
                    } else {
                        authz.requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    }
                    .anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(jwtRequestFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling { exceptionHandling ->
                exceptionHandling.accessDeniedHandler(accessDeniedHandler)
                exceptionHandling.authenticationEntryPoint(authenticationEntryPoint)
            }

        return http.build()
    }

    @Bean
    fun jwtRequestFilter(): JwtRequestFilter {
        return JwtRequestFilter(userDetailsService)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
