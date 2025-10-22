package com.questionmaster.api.service

import com.questionmaster.api.domain.dto.request.CreateQuestionRequest
import com.questionmaster.api.domain.dto.request.UpdateQuestionRequest
import com.questionmaster.api.domain.dto.response.*
import com.questionmaster.api.domain.entity.Alternative
import com.questionmaster.api.domain.entity.Question
import com.questionmaster.api.domain.enums.QuestionType
import com.questionmaster.api.domain.repository.*
import com.questionmaster.api.exception.BusinessException
import com.questionmaster.api.exception.ResourceNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class QuestionService(
    private val questionRepository: QuestionRepository,
    private val subjectRepository: SubjectRepository,
    private val topicRepository: TopicRepository,
    private val alternativeRepository: AlternativeRepository,
    private val answerRepository: AnswerRepository,
    private val userRepository: UserRepository
) {

    fun createQuestion(request: CreateQuestionRequest, createdById: UUID): QuestionResponse {

        val validationErrors = request.validate()
        if (validationErrors.isNotEmpty()) {
            throw BusinessException("Validation failed: ${validationErrors.joinToString(", ")}")
        }

        val subject = subjectRepository.findById(request.subjectId)
            .orElseThrow { ResourceNotFoundException("Subject not found with id: ${request.subjectId}") }

        val topics = topicRepository.findByIdInWithSubject(request.topicIds)
        if (topics.size != request.topicIds.size) {
            val foundIds = topics.map { it.id }.toSet()
            val missingIds = request.topicIds - foundIds
            throw ResourceNotFoundException("Topics not found with ids: $missingIds")
        }

        val createdBy = userRepository.findById(createdById)
            .orElseThrow { ResourceNotFoundException("User not found with id: $createdById") }

        val question = Question(
            statement = request.statement,
            subject = subject,
            year = request.year,
            questionType = request.questionType,
            createdBy = createdBy
        )

        question.topics.addAll(topics)
        val savedQuestion = questionRepository.save(question)

        // Create alternatives

        request.alternatives.forEachIndexed { index, altRequest ->
                val alternative = Alternative(
                    question = savedQuestion,
                    body = altRequest.body,
                    isCorrect = altRequest.isCorrect,
                    ord = (index + 1).toShort()
                )
            savedQuestion.alternatives.add(alternative)
        }
        val finalQuestion = questionRepository.save(savedQuestion)
        return mapToResponse(finalQuestion, null)
    }

    @Transactional(readOnly = true)
    fun getQuestions(
        page: Int = 0,
        size: Int = 20,
        subjectIds: List<Long> = emptyList(),
        years: List<Short> = emptyList(),
        questionType: QuestionType? = null,
        topicIds: List<Long> = emptyList(),
        userId: UUID? = null,
        answerStatus: String? = null
    ): PagedResponse<QuestionResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        
        val questionsPage = if (userId != null && answerStatus != null) {
            questionRepository.findQuestionsWithUserStatus(userId, answerStatus, pageable)
        } else {
            questionRepository.findQuestionsWithFilters(subjectIds, years, questionType, topicIds, pageable)
        }

        val questionResponses = questionsPage.content.map { question ->
            val userAnswer = if (userId != null) {
                answerRepository.findByUserIdAndQuestionId(userId, question.id)?.let { answer ->
                    UserAnswerInfo(
                        answerId = answer.id,
                        chosenAlternativeId = answer.alternative.id,
                        isCorrect = answer.isCorrect,
                        answeredAt = answer.answeredAt
                    )
                }
            } else null

            mapToResponse(question, userAnswer)
        }

        return PagedResponse(
            items = questionResponses,
            page = page,
            pageSize = size,
            totalPages = questionsPage.totalPages,
            totalItems = questionsPage.totalElements
        )
    }

    @Transactional(readOnly = true)
    fun getQuestionById(id: UUID, userId: UUID? = null): QuestionResponse {
        val question = questionRepository.findByIdWithDetails(id)
            ?: throw ResourceNotFoundException("Question not found with id: $id")

        val userAnswer = if (userId != null) {
            answerRepository.findByUserIdAndQuestionId(userId, question.id)?.let { answer ->
                UserAnswerInfo(
                    answerId = answer.id,
                    chosenAlternativeId = answer.alternative.id,
                    isCorrect = answer.isCorrect,
                    answeredAt = answer.answeredAt
                )
            }
        } else null

        return mapToResponse(question, userAnswer)
    }

    fun updateQuestion(id: UUID, request: UpdateQuestionRequest): QuestionResponse {
        // Validate request
        val validationErrors = request.validate()
        if (validationErrors.isNotEmpty()) {
            throw BusinessException("Validation failed: ${validationErrors.joinToString(", ")}")
        }

        val question = questionRepository.findByIdWithDetails(id)
            ?: throw ResourceNotFoundException("Question not found with id: $id")

        val subject = subjectRepository.findById(request.subjectId)
            .orElseThrow { ResourceNotFoundException("Subject not found with id: ${request.subjectId}") }

        val topics = topicRepository.findByIdInWithSubject(request.topicIds)
        if (topics.size != request.topicIds.size) {
            val foundIds = topics.map { it.id }.toSet()
            val missingIds = request.topicIds - foundIds
            throw ResourceNotFoundException("Topics not found with ids: $missingIds")
        }

        // Update question properties
        val updatedQuestion = question.copy(
            statement = request.statement,
            subject = subject,
            year = request.year,
            questionType = request.questionType,
            isActive = request.isActive,
            updatedAt = LocalDateTime.now()
        )

        // Clear existing topics and add new ones
        updatedQuestion.topics.clear()
        updatedQuestion.topics.addAll(topics)

        // Remove existing alternatives
        updatedQuestion.alternatives.clear()

        val savedQuestion = questionRepository.save(updatedQuestion)

        // Create new alternatives
        request.alternatives.forEachIndexed { index, altRequest ->
            val alternative = Alternative(
                question = savedQuestion,
                body = altRequest.body,
                isCorrect = altRequest.isCorrect,
                ord = (index + 1).toShort()
            )
            savedQuestion.alternatives.add(alternative)
        }

        val finalQuestion = questionRepository.save(savedQuestion)
        return mapToResponse(finalQuestion, null)
    }

    fun deleteQuestion(id: UUID) {
        val question = questionRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Question not found with id: $id") }
        
        questionRepository.delete(question)
    }

    private fun mapToResponse(question: Question, userAnswer: UserAnswerInfo?): QuestionResponse {
        return QuestionResponse(
            id = question.id,
            statement = question.statement,
            subject = SubjectResponse(
                id = question.subject.id,
                name = question.subject.name,
                createdAt = question.subject.createdAt
            ),
            topics = question.topics.map { topic ->
                TopicResponse(
                    id = topic.id,
                    name = topic.name,
                    subjectId = topic.subject.id,
                    subjectName = topic.subject.name,
                    createdAt = topic.createdAt
                )
            },
            year = question.year,
            questionType = question.questionType,
            isActive = question.isActive,
            createdAt = question.createdAt,
            updatedAt = question.updatedAt,
            alternatives = question.alternatives.sortedBy { it.ord }.map { alternative ->
                AlternativeResponse(
                    id = alternative.id,
                    body = alternative.body,
                    label = alternative.label,
                    ord = alternative.ord
                )
            },
            userAnswer = userAnswer
        )
    }
}
