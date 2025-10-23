package com.questionmaster.api.domain.dto.request

import com.questionmaster.api.domain.enums.QuestionType
import jakarta.validation.Valid
import jakarta.validation.constraints.*

data class CreateQuestionRequest(
    @field:NotBlank(message = "Question statement is required")
    @field:Size(max = 1000, message = "Question statement must not exceed 1000 characters")
    val statement: String,
    
    @field:NotNull(message = "Subject ID is required")
    @field:Positive(message = "Subject ID must be positive")
    val subjectId: Long,
    
    @field:Positive(message = "Exam ID must be positive")
    val examId: Long? = null,
    
    @field:Min(value = 1900, message = "Year must be at least 1900")
    @field:Max(value = 2100, message = "Year must not exceed 2100")
    val year: Short? = null,
    
    @field:NotNull(message = "Question type is required")
    val questionType: QuestionType,
    
    @field:NotEmpty(message = "Topic IDs are required")
    val topicIds: Set<Long>,
    
    @field:NotEmpty(message = "Alternatives are required")
    @field:Size(min = 2, max = 6, message = "Must have between 2 and 6 alternatives")
    @field:Valid
    val alternatives: List<CreateAlternativeRequest>
) {
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        // Validate that exactly one alternative is correct
        val correctAlternatives = alternatives.count { it.isCorrect }
        if (correctAlternatives != 1) {
            errors.add("Exactly one alternative must be correct")
        }
        
        // Validate TRUE_FALSE questions have exactly 2 alternatives
        if (questionType == QuestionType.TRUE_FALSE && alternatives.size != 2) {
            errors.add("TRUE_FALSE questions must have exactly 2 alternatives")
        }
        
        return errors
    }
}

data class CreateAlternativeRequest(
    @field:NotBlank(message = "Alternative body is required")
    @field:Size(max = 500, message = "Alternative body must not exceed 500 characters")
    val body: String,
    
    @field:NotNull(message = "isCorrect flag is required")
    val isCorrect: Boolean
)
