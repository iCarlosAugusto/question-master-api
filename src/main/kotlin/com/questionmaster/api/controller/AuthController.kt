package com.questionmaster.api.controller

import com.questionmaster.api.domain.dto.request.LoginRequest
import com.questionmaster.api.domain.dto.response.AuthResponse
import com.questionmaster.api.domain.enums.AppRole
import com.questionmaster.api.security.JwtTokenProvider
import com.questionmaster.api.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
class AuthController(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder
) {

    @PostMapping("/login")
    @Operation(summary = "Login with credentials", description = "Authenticate user and return JWT token")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        // In a real application, you would validate credentials against Supabase
        // For this demo, we'll create a mock authentication
        
        // Mock user creation - in real app this would come from Supabase
        val userId = UUID.randomUUID()
        val role = if (request.email.contains("admin")) AppRole.ADMIN else AppRole.USER
        val displayName = request.email.substringBefore("@")
        
        // Create or get user
        val user = try {
            userService.getUserById(userId)
        } catch (e: Exception) {
            userService.createUser(userId, displayName, role)
        }
        
        val token = jwtTokenProvider.generateToken(user.id, user.role, user.displayName)
        
        val authResponse = AuthResponse(
            token = token,
            userId = user.id,
            role = user.role,
            displayName = user.displayName
        )
        
        return ResponseEntity.ok(authResponse)
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register new user and return JWT token")
    fun register(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        // In a real application, you would create user in Supabase
        // For this demo, we'll create a mock registration
        
        val userId = UUID.randomUUID()
        val displayName = request.email.substringBefore("@")
        val email = request.email;
        
        val user = userService.createUser(userId, displayName, AppRole.USER, email)
        val token = jwtTokenProvider.generateToken(user.id, user.role, user.displayName)
        
        val authResponse = AuthResponse(
            token = token,
            userId = user.id,
            role = user.role,
            displayName = user.displayName
        )
        
        return ResponseEntity.ok(authResponse)
    }

    @PostMapping("/admin")
    @Operation(summary = "Create admin user", description = "Create admin user for testing purposes")
    fun createAdmin(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val userId = UUID.randomUUID()
        val displayName = request.email.substringBefore("@")
        
        val user = userService.createUser(userId, displayName, AppRole.ADMIN, request.email)
        val token = jwtTokenProvider.generateToken(user.id, user.role, user.displayName)
        
        val authResponse = AuthResponse(
            token = token,
            userId = user.id,
            role = user.role,
            displayName = user.displayName
        )
        
        return ResponseEntity.ok(authResponse)
    }
}
