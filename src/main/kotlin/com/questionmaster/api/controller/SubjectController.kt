package com.questionmaster.api.controller

import com.questionmaster.api.domain.dto.request.CreateSubjectRequest
import com.questionmaster.api.domain.dto.response.SubjectResponse
import com.questionmaster.api.domain.dto.response.SubjectsWithTopicsWrapper
import com.questionmaster.api.service.SubjectService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/subjects")
@Tag(name = "Subjects", description = "Subject management endpoints")
class SubjectController(
    private val subjectService: SubjectService
) {

    @GetMapping
    @Operation(
        summary = "Get subjects by exam", 
        description = "Retrieve subjects filtered by exam slug. Requires examSlug query parameter."
    )
    fun getSubjectsByExam(
        @RequestParam examSlug: String
    ): ResponseEntity<List<SubjectResponse>> {
        val subjects = subjectService.getSubjectsByExamSlug(examSlug)
        return ResponseEntity.ok(subjects)
    }

    @GetMapping("/with-topics")
    @Operation(
        summary = "Get subjects with topics by exam", 
        description = "Retrieve subjects with their related topics filtered by exam slug. Requires examSlug query parameter."
    )
    fun getSubjectsWithTopicsByExam(
        @RequestParam examSlug: String
    ): ResponseEntity<SubjectsWithTopicsWrapper> {
        val result = subjectService.getSubjectsWithTopicsByExamSlug(examSlug)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subject by ID", description = "Retrieve a specific subject by ID")
    fun getSubjectById(@PathVariable id: Long): ResponseEntity<SubjectResponse> {
        val subject = subjectService.getSubjectById(id)
        return ResponseEntity.ok(subject)
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create subject", description = "Create a new subject (Admin only)")
    fun createSubject(@Valid @RequestBody request: CreateSubjectRequest): ResponseEntity<SubjectResponse> {
        val subject = subjectService.createSubject(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(subject)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update subject", description = "Update an existing subject (Admin only)")
    fun updateSubject(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateSubjectRequest
    ): ResponseEntity<SubjectResponse> {
        val subject = subjectService.updateSubject(id, request)
        return ResponseEntity.ok(subject)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete subject", description = "Delete a subject (Admin only)")
    fun deleteSubject(@PathVariable id: Long): ResponseEntity<Void> {
        subjectService.deleteSubject(id)
        return ResponseEntity.noContent().build()
    }
}
