package com.questionmaster.api.controller

import com.questionmaster.api.domain.dto.request.CreateUserSubscriptionRequest
import com.questionmaster.api.domain.dto.request.UpdateUserSubscriptionStatusRequest
import com.questionmaster.api.domain.dto.response.UserSubscriptionResponse
import com.questionmaster.api.service.UserSubscriptionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


@RestController
@RequestMapping("/api/user-subscriptions")
class UserSubscriptionController(
    private val userSubscriptionService: UserSubscriptionService
) {

    @PostMapping
    fun create(
        @RequestBody request: CreateUserSubscriptionRequest
    ): ResponseEntity<UserSubscriptionResponse> {
        val response = userSubscriptionService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID
    ): ResponseEntity<UserSubscriptionResponse> {
        val response = userSubscriptionService.getById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun listByUser(
        @RequestParam("userId") userId: UUID
    ): ResponseEntity<List<UserSubscriptionResponse>> {
        val response = userSubscriptionService.listByUser(userId)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: UUID,
        @RequestBody request: UpdateUserSubscriptionStatusRequest
    ): ResponseEntity<UserSubscriptionResponse> {
        val response = userSubscriptionService.updateStatus(id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: UUID
    ) {
        userSubscriptionService.delete(id)
    }
}