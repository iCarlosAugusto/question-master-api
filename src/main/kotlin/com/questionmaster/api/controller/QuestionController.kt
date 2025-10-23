package com.questionmaster.api.controller

import com.questionmaster.api.domain.dto.request.AnswerQuestionRequest
import com.questionmaster.api.domain.dto.request.CreateQuestionRequest
import com.questionmaster.api.domain.dto.request.UpdateQuestionRequest
import com.questionmaster.api.domain.dto.response.AnswerResponse
import com.questionmaster.api.domain.dto.response.PagedResponse
import com.questionmaster.api.domain.dto.response.QuestionResponse
import com.questionmaster.api.domain.enums.QuestionType
import com.questionmaster.api.security.CurrentUser
import com.questionmaster.api.security.CustomUserDetails
import com.questionmaster.api.service.AnswerService
import com.questionmaster.api.service.QuestionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Questions", description = "Question management endpoints")
class QuestionController(
    private val questionService: QuestionService,
    private val answerService: AnswerService
) {

//    @GetMapping
//    @Operation(
//        summary = "Get questions with filters",
//        description = "Retrieve questions with optional filters and pagination. Supports multiple subjectIds, topicIds, and years. Requires X-Exam-Id header."
//    )
//    fun getQuestions(
//        @RequestParam(defaultValue = "0") page: Int,
//        @RequestParam(defaultValue = "20") size: Int,
//        @RequestParam(required = false) subjectIds: List<Long>?,
//        @RequestParam(required = false) years: List<Short>?,
//        @RequestParam(required = false) questionType: QuestionType?,
//        @RequestParam(required = false) topicIds: List<Long>?,
//        @RequestParam(required = false) answerStatus: String?,
//        @CurrentUser userDetails: CustomUserDetails?
//    ): ResponseEntity<PagedResponse<QuestionResponse>> {
//        val userId = userDetails?.getId()
//        val questions = questionService.getQuestions(
//            examSlug = examSlug,
//            page = page,
//            size = size,
//            subjectIds = subjectIds ?: emptyList(),
//            years = years ?: emptyList(),
//            questionType = questionType,
//            topicIds = topicIds ?: emptyList(),
//            userId = userId,
//            answerStatus = answerStatus
//        )
//        return ResponseEntity.ok(questions)
//    }

    @GetMapping("/{id}")
    @Operation(summary = "Get question by ID", description = "Retrieve a specific question by ID")
    fun getQuestionById(
        @PathVariable id: UUID,
        @CurrentUser userDetails: CustomUserDetails?
    ): ResponseEntity<QuestionResponse> {
        val userId = userDetails?.getId()
        val question = questionService.getQuestionById(id, userId)
        return ResponseEntity.ok(question)
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create question", description = "Create a new question with alternatives (Admin only)")
    fun createQuestion(
        @Valid @RequestBody request: CreateQuestionRequest,
        @CurrentUser userDetails: CustomUserDetails
    ): ResponseEntity<Any> {
        val question = questionService.createQuestion(request, userDetails.getId())
        return ResponseEntity.status(HttpStatus.CREATED).body(question)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update question", description = "Update an existing question (Admin only)")
    fun updateQuestion(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateQuestionRequest
    ): ResponseEntity<QuestionResponse> {
        val question = questionService.updateQuestion(id, request)
        return ResponseEntity.ok(question)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete question", description = "Delete a question (Admin only)")
    fun deleteQuestion(@PathVariable id: UUID): ResponseEntity<Void> {
        questionService.deleteQuestion(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{questionId}/answer")
    @Operation(summary = "Answer a question", description = "Submit an answer to a question")
    fun answerQuestion(
        @PathVariable questionId: UUID,
        @Valid @RequestBody request: AnswerQuestionRequest,
        @CurrentUser userDetails: CustomUserDetails
    ): ResponseEntity<AnswerResponse> {
        val answer = answerService.answerQuestion(questionId, userDetails.getId(), request)
        return ResponseEntity.status(HttpStatus.CREATED).body(answer)
    }
}
