package com.questionmaster.api.service

import com.questionmaster.api.domain.dto.request.CreateExamRequest
import com.questionmaster.api.domain.dto.request.UpdateExamRequest
import com.questionmaster.api.domain.dto.response.ExamResponse
import com.questionmaster.api.domain.dto.response.ExamSummaryResponse
import com.questionmaster.api.domain.entity.Exam
import com.questionmaster.api.domain.repository.ExamRepository
import com.questionmaster.api.exception.ResourceNotFoundException
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ExamService(
    private val examRepository: ExamRepository
) {

    @Transactional(readOnly = true)
    fun getExams(): List<ExamResponse> {
        
        val exams = examRepository.findAll()
        
        val examResponses = exams.map { exam ->
            ExamResponse(
                id = exam.id,
                name = exam.name,
                slug = exam.slug,
                institution = exam.institution,
                description = exam.description,
                isActive = exam.isActive,
                createdAt = exam.createdAt,
                updatedAt = exam.updatedAt,
                questionCount = exam.questions.size,
            )
        }

        return examResponses
    }

    @Transactional(readOnly = true)
    fun getExamById(id: Long): ExamResponse {
        val exam = examRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Exam not found with id: $id") }
        
        return ExamResponse(
            id = exam.id,
            name = exam.name,
            slug = exam.slug,
            institution = exam.institution,
            description = exam.description,
            isActive = exam.isActive,
            createdAt = exam.createdAt,
            updatedAt = exam.updatedAt,
            questionCount = exam.questions.size
        )
    }

    @Transactional(readOnly = true)
    fun getAllExamsSummary(): List<ExamSummaryResponse> {
        val exams = examRepository.findAll(Sort.by(Sort.Direction.DESC, "year", "name"))
        
        return exams.map { exam ->
            ExamSummaryResponse(
                id = exam.id,
                name = exam.name,
                institution = exam.institution,
            )
        }
    }

    @Transactional
    fun createExam(request: CreateExamRequest): ExamResponse {
        val exam = Exam(
            name = request.name,
            slug = request.slug,
            institution = request.institution,
            description = request.description
        )
        
        val savedExam = examRepository.save(exam)
        
        return ExamResponse(
            id = savedExam.id,
            name = savedExam.name,
            slug = savedExam.slug,
            institution = savedExam.institution,
            description = savedExam.description,
            isActive = savedExam.isActive,
            createdAt = savedExam.createdAt,
            updatedAt = savedExam.updatedAt,
            questionCount = 0
        )
    }

    @Transactional
    fun updateExam(id: Long, request: UpdateExamRequest): ExamResponse {
        val exam = examRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Exam not found with id: $id") }
        
        val updatedExam = exam.copy(
            name = request.name ?: exam.name,
            institution = request.institution ?: exam.institution,
            description = request.description ?: exam.description,
            isActive = request.isActive ?: exam.isActive,
            updatedAt = LocalDateTime.now()
        )
        
        val savedExam = examRepository.save(updatedExam)
        
        return ExamResponse(
            id = savedExam.id,
            name = savedExam.name,
            slug = savedExam.slug,
            institution = savedExam.institution,
            description = savedExam.description,
            isActive = savedExam.isActive,
            createdAt = savedExam.createdAt,
            updatedAt = savedExam.updatedAt,
            questionCount = savedExam.questions.size
        )
    }

    @Transactional
    fun deleteExam(id: Long) {
        val exam = examRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Exam not found with id: $id") }
        
        examRepository.delete(exam)
    }
}

