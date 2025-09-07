package com.questionmaster.api.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        println("Error in application: ${ex.message}")
        ex.printStackTrace()
        val error = ErrorResponse(
            message = ex.message ?: "Resource not found",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value()
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ErrorResponse> {
        println("Error in application: ${ex.message}")
        ex.printStackTrace()
        val error = ErrorResponse(
            message = ex.message ?: "Business rule violation",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity.badRequest().body(error)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        println("Error in application: ${ex.message}")
        ex.printStackTrace()
        val errors = ex.bindingResult.allErrors.associate { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage ?: "Invalid value"
            fieldName to errorMessage
        }

        val validationError = ValidationErrorResponse(
            message = "Validation failed",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            errors = errors
        )
        return ResponseEntity.badRequest().body(validationError)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        println("Error in application: ${ex.message}")
        ex.printStackTrace()
        val error = ErrorResponse(
            message = "Access denied",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.FORBIDDEN.value()
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<ErrorResponse> {
        println("Error in application: ${ex.message}")
        ex.printStackTrace()
        val error = ErrorResponse(
            message = "Invalid credentials",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value()
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        println("Error in application: ${ex.message}")
        ex.printStackTrace()
        val error = ErrorResponse(
            message = "Internal server error: ${ex.message}",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}

data class ErrorResponse(
    val message: String,
    val timestamp: LocalDateTime,
    val status: Int
)

data class ValidationErrorResponse(
    val message: String,
    val timestamp: LocalDateTime,
    val status: Int,
    val errors: Map<String, String>
)
