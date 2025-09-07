package com.questionmaster.api.controller

import com.questionmaster.api.domain.entity.User
import com.questionmaster.api.domain.enums.AppRole
import com.questionmaster.api.security.CurrentUser
import com.questionmaster.api.security.CustomUserDetails
import com.questionmaster.api.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get current user", description = "Get information about the current authenticated user")
    fun getCurrentUser(@CurrentUser userDetails: CustomUserDetails): ResponseEntity<User> {
        val user = userService.getUserById(userDetails.getId())
        return ResponseEntity.ok(user)
    }

    @PutMapping("/me/display-name")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Update display name", description = "Update the current user's display name")
    fun updateDisplayName(
        @RequestBody request: UpdateDisplayNameRequest,
        @CurrentUser userDetails: CustomUserDetails
    ): ResponseEntity<User> {
        val user = userService.updateDisplayName(userDetails.getId(), request.displayName)
        return ResponseEntity.ok(user)
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Get all users (Admin only)")
    fun getAllUsers(): ResponseEntity<List<User>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users)
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role", description = "Update a user's role (Admin only)")
    fun updateUserRole(
        @PathVariable userId: UUID,
        @RequestBody request: UpdateRoleRequest
    ): ResponseEntity<User> {
        val user = userService.updateUserRole(userId, request.role)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Delete a user (Admin only)")
    fun deleteUser(@PathVariable userId: UUID): ResponseEntity<Void> {
        userService.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }
}

data class UpdateDisplayNameRequest(
    val displayName: String
)

data class UpdateRoleRequest(
    val role: AppRole
)
