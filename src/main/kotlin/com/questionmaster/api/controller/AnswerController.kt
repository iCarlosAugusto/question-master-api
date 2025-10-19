package com.questionmaster.api.controller

import com.questionmaster.api.domain.dto.response.AnswerResponse
import com.questionmaster.api.security.CurrentUser
import com.questionmaster.api.security.CustomUserDetails
import com.questionmaster.api.service.AnswerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/answers")
@Tag(name = "Answers", description = "Answer management endpoints")
@SecurityRequirement(name = "bearerAuth")
class AnswerController(
    private val answerService: AnswerService
) {

    @GetMapping("/my-answers")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user's answers", description = "Retrieve all answers submitted by the current user")
    fun getMyAnswers(@CurrentUser userDetails: CustomUserDetails): ResponseEntity<List<AnswerResponse>> {
        val answers = answerService.getUserAnswers(userDetails.getId())
        return ResponseEntity.ok(answers)
    }

    @GetMapping("/my-stats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user's statistics", description = "Retrieve statistics for the current user")
    fun getMyStats(@CurrentUser userDetails: CustomUserDetails): ResponseEntity<Map<String, Any>> {
        val stats = answerService.getUserStats(userDetails.getId())
        return ResponseEntity.ok(stats)
    }
}
