package com.questionmaster.api.service

import com.questionmaster.api.domain.dto.request.AnswerQuestionRequest
import com.questionmaster.api.domain.dto.response.AnswerResponse
import com.questionmaster.api.domain.entity.Answer
import com.questionmaster.api.domain.repository.*
import com.questionmaster.api.exception.BusinessException
import com.questionmaster.api.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class AnswerService(
    private val answerRepository: AnswerRepository,
    private val questionRepository: QuestionRepository,
    private val alternativeRepository: AlternativeRepository,
    private val userRepository: UserRepository
) {

    fun answerQuestion(questionId: UUID, userId: UUID, request: AnswerQuestionRequest): AnswerResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

        val question = questionRepository.findByIdWithDetails(questionId)
            ?: throw ResourceNotFoundException("Question not found with id: $questionId")

        if (!question.isActive) {
            throw BusinessException("Question is not active")
        }

        val alternative = alternativeRepository.findById(request.alternativeId)
            .orElseThrow { ResourceNotFoundException("Alternative not found with id: ${request.alternativeId}") }

        // Verify the alternative belongs to this question
        if (alternative.question.id != questionId) {
            throw BusinessException("Alternative does not belong to this question")
        }

        // Check if user has already answered this question
        answerRepository.findByUserIdAndQuestionId(userId, questionId)?.let {
            throw BusinessException("Question has already been answered by this user")
        }

        // Find the correct alternative
        val correctAlternative = alternativeRepository.findCorrectAlternativeByQuestionId(questionId)
            ?: throw BusinessException("No correct alternative found for this question")

        val isCorrect = alternative.isCorrect

        val answer = Answer(
            user = user,
            question = question,
            alternative = alternative,
            isCorrect = isCorrect
        )

        val savedAnswer = answerRepository.save(answer)

        return AnswerResponse(
            id = savedAnswer.id,
            questionId = questionId,
            alternativeId = alternative.id,
            isCorrect = isCorrect,
            answeredAt = savedAnswer.answeredAt,
            correctAlternativeId = correctAlternative.id
        )
    }

    @Transactional(readOnly = true)
    fun getUserAnswers(userId: UUID): List<AnswerResponse> {
        userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

        return answerRepository.findByUserIdWithDetails(userId).map { answer ->
            val correctAlternative = alternativeRepository.findCorrectAlternativeByQuestionId(answer.question.id)
                ?: throw BusinessException("No correct alternative found for question ${answer.question.id}")

            AnswerResponse(
                id = answer.id,
                questionId = answer.question.id,
                alternativeId = answer.alternative.id,
                isCorrect = answer.isCorrect,
                answeredAt = answer.answeredAt,
                correctAlternativeId = correctAlternative.id
            )
        }
    }

    @Transactional(readOnly = true)
    fun getUserStats(userId: UUID): Map<String, Any> {
        userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

        val totalAnswers = answerRepository.countTotalAnswersByUserId(userId)
        val correctAnswers = answerRepository.countCorrectAnswersByUserId(userId)
        val accuracy = if (totalAnswers > 0) {
            (correctAnswers.toDouble() / totalAnswers.toDouble()) * 100
        } else 0.0

        return mapOf(
            "totalAnswers" to totalAnswers,
            "correctAnswers" to correctAnswers,
            "incorrectAnswers" to (totalAnswers - correctAnswers),
            "accuracy" to String.format("%.2f", accuracy)
        )
    }
}
