package com.questionmaster.api.service

import com.questionmaster.api.domain.dto.request.CreateSubjectRequest
import com.questionmaster.api.domain.dto.response.SubjectResponse
import com.questionmaster.api.domain.entity.Subject
import com.questionmaster.api.domain.repository.SubjectRepository
import com.questionmaster.api.exception.BusinessException
import com.questionmaster.api.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SubjectService(
    private val subjectRepository: SubjectRepository
) {

    fun createSubject(request: CreateSubjectRequest): SubjectResponse {
        // Check if subject with same name already exists
        subjectRepository.findByNameIgnoreCase(request.name)?.let {
            throw BusinessException("Subject with name '${request.name}' already exists")
        }

        val subject = Subject(name = request.name)
        val savedSubject = subjectRepository.save(subject)
        return mapToResponse(savedSubject)
    }

    @Transactional(readOnly = true)
    fun getAllSubjects(): List<SubjectResponse> {
        return subjectRepository.findAll()
            .map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getSubjectById(id: Long): SubjectResponse {
        val subject = subjectRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Subject not found with id: $id") }
        return mapToResponse(subject)
    }

    fun updateSubject(id: Long, request: CreateSubjectRequest): SubjectResponse {
        val subject = subjectRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Subject not found with id: $id") }

        // Check if another subject with same name exists
        subjectRepository.findByNameIgnoreCase(request.name)?.let { existing ->
            if (existing.id != id) {
                throw BusinessException("Subject with name '${request.name}' already exists")
            }
        }

        val updatedSubject = subject.copy(name = request.name)
        val savedSubject = subjectRepository.save(updatedSubject)
        return mapToResponse(savedSubject)
    }

    fun deleteSubject(id: Long) {
        val subject = subjectRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Subject not found with id: $id") }
        
        subjectRepository.delete(subject)
    }

    private fun mapToResponse(subject: Subject): SubjectResponse {
        return SubjectResponse(
            id = subject.id,
            name = subject.name,
            createdAt = subject.createdAt,
            topicsCount = subject.topics.size
        )
    }
}
