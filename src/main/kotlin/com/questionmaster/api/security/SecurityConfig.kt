package com.questionmaster.api.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
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
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val apiKeyAuthFilter: ApiKeyAuthFilter,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authz ->
                authz
                    // Public endpoints
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/exams").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/exams/*/questions").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/subjects/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/topics/**").permitAll()
                    
                    // Swagger/OpenAPI
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                    // Admin only endpoints
                    .requestMatchers(HttpMethod.POST, "/api/questions").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/questions/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/questions/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/subjects/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/subjects/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/subjects/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/topics/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/topics/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/topics/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/exams/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/exams/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/exams/**").hasRole("ADMIN")

                    .requestMatchers("api/internal/**").permitAll()
                    // User and Admin endpoints
                    .requestMatchers("/api/answers/**").hasAnyRole("USER", "ADMIN")
                    
                    // All other requests require authentication
                    .anyRequest().authenticated()
            }
            .exceptionHandling {ex ->
                print(ex)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(apiKeyAuthFilter, JwtAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        
        // Allow frontend origins
        configuration.allowedOrigins = listOf(
            "http://localhost:3000",      // React/Next.js dev
            "http://localhost:3001",      // Alternative port
            "http://localhost:5173",      // Vite dev server
            "http://localhost:4200",      // Angular dev
            "https://your-production-domain.com"  // Your production domain
        )
        
        // Allow all HTTP methods
        configuration.allowedMethods = listOf(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        )
        
        // CRITICAL: Allow Authorization header and other common headers
        configuration.allowedHeaders = listOf(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With",
            "X-Exam-Id"
        )
        
        // Allow credentials (cookies, authorization headers)
        configuration.allowCredentials = true
        
        // Expose headers that frontend can read
        configuration.exposedHeaders = listOf(
            "Authorization",
            "Content-Type"
        )
        
        // Cache preflight requests for 1 hour
        configuration.maxAge = 3600L
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        
        return source
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
