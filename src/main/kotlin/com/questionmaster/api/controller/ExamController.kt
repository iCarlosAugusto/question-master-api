package com.questionmaster.api.controller

import com.questionmaster.api.domain.dto.request.CreateExamRequest
import com.questionmaster.api.domain.dto.request.UpdateExamRequest
import com.questionmaster.api.domain.dto.response.ExamResponse
import com.questionmaster.api.domain.dto.response.ExamSummaryResponse
import com.questionmaster.api.domain.dto.response.PagedResponse
import com.questionmaster.api.domain.dto.response.QuestionResponse
import com.questionmaster.api.domain.dto.response.SubjectResponse
import com.questionmaster.api.domain.dto.response.SubjectsWithTopicsWrapper
import com.questionmaster.api.domain.enums.ExamType
import com.questionmaster.api.domain.enums.QuestionType
import com.questionmaster.api.security.CurrentUser
import com.questionmaster.api.security.CustomUserDetails
import com.questionmaster.api.service.ExamService
import com.questionmaster.api.service.QuestionService
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
@RequestMapping("/api/exams")
@Tag(name = "Exams", description = "Exam management endpoints")
class ExamController(
    private val examService: ExamService,
    private val questionService: QuestionService,
    private val subjectService: SubjectService
) {

    @GetMapping("/{examSlug}/subjects")
    @Operation(
        summary = "Get subjects by exam",
        description = "Retrieve all subjects linked to a specific exam by slug"
    )
    fun getSubjectsByExam(@PathVariable examSlug: String): ResponseEntity<List<SubjectResponse>> {
        val subjects = subjectService.getSubjectsByExamSlug(examSlug)
        return ResponseEntity.ok(subjects)
    }

    @GetMapping("/{examSlug}/subjects/with-topics")
    @Operation(
        summary = "Get subjects with topics by exam",
        description = "Retrieve subjects with their related topics filtered by exam slug. Requires examSlug query parameter."
    )
    fun getSubjectsWithTopicsByExam(
        @PathVariable examSlug: String
    ): ResponseEntity<SubjectsWithTopicsWrapper> {
        val result = subjectService.getSubjectsWithTopicsByExamSlug(examSlug)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{examSlug}/questions")
    @Operation(
        summary = "Get questions with filters",
        description = "Retrieve questions with optional filters and pagination. Supports multiple subjectIds, topicIds, and years. Requires X-Exam-Id header."
    )
    fun getQuestions(
        @PathVariable examSlug: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) subjectIds: List<Long>?,
        @RequestParam(required = false) years: List<Short>?,
        @RequestParam(required = false) questionType: QuestionType?,
        @RequestParam(required = false) topicIds: List<Long>?,
        @RequestParam(required = false) answerStatus: String?,
        @CurrentUser userDetails: CustomUserDetails?
    ): ResponseEntity<PagedResponse<QuestionResponse>> {
        val userId = userDetails?.getId()
        val questions = questionService.getQuestions(
            examSlug = examSlug,
            page = page,
            size = size,
            subjectIds = subjectIds ?: emptyList(),
            years = years ?: emptyList(),
            questionType = questionType,
            topicIds = topicIds ?: emptyList(),
            userId = userId,
            answerStatus = answerStatus
        )
        return ResponseEntity.ok(questions)
    }
    @GetMapping
    @Operation(
        summary = "Get exams with filters",
        description = "Retrieve exams with optional filters and pagination"
    )
    fun getExams(): List<ExamResponse> {
        val exams = examService.getExams()
        return exams
    }

    @GetMapping("/summary")
    @Operation(
        summary = "Get all exams summary",
        description = "Retrieve a summary list of all exams (id, name, type, institution, year)"
    )
    fun getAllExamsSummary(): ResponseEntity<List<ExamSummaryResponse>> {
        val exams = examService.getAllExamsSummary()
        return ResponseEntity.ok(exams)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get exam by ID",
        description = "Retrieve a specific exam by ID"
    )
    fun getExamById(@PathVariable id: Long): ResponseEntity<ExamResponse> {
        val exam = examService.getExamById(id)
        return ResponseEntity.ok(exam)
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Create exam",
        description = "Create a new exam (Admin only)"
    )
    fun createExam(
        @Valid @RequestBody request: CreateExamRequest
    ): ResponseEntity<ExamResponse> {
        val exam = examService.createExam(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(exam)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Update exam",
        description = "Update an existing exam (Admin only)"
    )
    fun updateExam(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateExamRequest
    ): ResponseEntity<ExamResponse> {
        val exam = examService.updateExam(id, request)
        return ResponseEntity.ok(exam)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Delete exam",
        description = "Delete an exam (Admin only)"
    )
    fun deleteExam(@PathVariable id: Long): ResponseEntity<Void> {
        examService.deleteExam(id)
        return ResponseEntity.noContent().build()
    }
}

