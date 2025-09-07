package com.questionmaster.api.controller

import com.questionmaster.api.domain.dto.request.CreateTopicRequest
import com.questionmaster.api.domain.dto.response.TopicResponse
import com.questionmaster.api.service.TopicService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/topics")
@Tag(name = "Topics", description = "Topic management endpoints")
class TopicController(
    private val topicService: TopicService
) {

    @GetMapping
    @Operation(summary = "Get all topics", description = "Retrieve all topics")
    fun getAllTopics(): ResponseEntity<List<TopicResponse>> {
        val topics = topicService.getAllTopics()
        return ResponseEntity.ok(topics)
    }

    @GetMapping("/subject/{subjectId}")
    @Operation(summary = "Get topics by subject", description = "Retrieve all topics for a specific subject")
    fun getTopicsBySubject(@PathVariable subjectId: Long): ResponseEntity<List<TopicResponse>> {
        val topics = topicService.getTopicsBySubject(subjectId)
        return ResponseEntity.ok(topics)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get topic by ID", description = "Retrieve a specific topic by ID")
    fun getTopicById(@PathVariable id: Long): ResponseEntity<TopicResponse> {
        val topic = topicService.getTopicById(id)
        return ResponseEntity.ok(topic)
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create topic", description = "Create a new topic (Admin only)")
    fun createTopic(@Valid @RequestBody request: CreateTopicRequest): ResponseEntity<TopicResponse> {
        val topic = topicService.createTopic(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(topic)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update topic", description = "Update an existing topic (Admin only)")
    fun updateTopic(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateTopicRequest
    ): ResponseEntity<TopicResponse> {
        val topic = topicService.updateTopic(id, request)
        return ResponseEntity.ok(topic)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete topic", description = "Delete a topic (Admin only)")
    fun deleteTopic(@PathVariable id: Long): ResponseEntity<Void> {
        topicService.deleteTopic(id)
        return ResponseEntity.noContent().build()
    }
}
