package com.questionmaster.api.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authz ->
                authz
                    // Public endpoints
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/questions/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/subjects/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/topics/**").permitAll()
                    
                    // Swagger/OpenAPI
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    
                    // Admin only endpoints
                    .requestMatchers(HttpMethod.POST, "/api/questions/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/questions/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/questions/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/subjects/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/subjects/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/subjects/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/topics/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/topics/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/topics/**").hasRole("ADMIN")
                    
                    // User and Admin endpoints
                    .requestMatchers("/api/answers/**").hasAnyRole("USER", "ADMIN")
                    
                    // All other requests require authentication
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
}
