package com.questionmaster.api.service

import com.questionmaster.api.domain.dto.request.CreateTopicRequest
import com.questionmaster.api.domain.dto.response.TopicResponse
import com.questionmaster.api.domain.entity.Topic
import com.questionmaster.api.domain.repository.SubjectRepository
import com.questionmaster.api.domain.repository.TopicRepository
import com.questionmaster.api.exception.BusinessException
import com.questionmaster.api.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TopicService(
    private val topicRepository: TopicRepository,
    private val subjectRepository: SubjectRepository
) {

    fun createTopic(request: CreateTopicRequest): TopicResponse {
        val subject = subjectRepository.findById(request.subjectId)
            .orElseThrow { ResourceNotFoundException("Subject not found with id: ${request.subjectId}") }

        // Check if topic with same name already exists in this subject
        topicRepository.findBySubjectIdAndNameIgnoreCase(request.subjectId, request.name)?.let {
            throw BusinessException("Topic with name '${request.name}' already exists in this subject")
        }

        val topic = Topic(
            subject = subject,
            name = request.name
        )
        val savedTopic = topicRepository.save(topic)
        return mapToResponse(savedTopic)
    }

    @Transactional(readOnly = true)
    fun getAllTopics(): List<TopicResponse> {
        return topicRepository.findAll()
            .map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getTopicsBySubject(subjectId: Long): List<TopicResponse> {
        // Verify subject exists
        subjectRepository.findById(subjectId)
            .orElseThrow { ResourceNotFoundException("Subject not found with id: $subjectId") }

        return topicRepository.findBySubjectId(subjectId)
            .map { mapToResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getTopicById(id: Long): TopicResponse {
        val topic = topicRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Topic not found with id: $id") }
        return mapToResponse(topic)
    }

    fun updateTopic(id: Long, request: CreateTopicRequest): TopicResponse {
        val topic = topicRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Topic not found with id: $id") }

        val subject = subjectRepository.findById(request.subjectId)
            .orElseThrow { ResourceNotFoundException("Subject not found with id: ${request.subjectId}") }

        // Check if another topic with same name exists in this subject
        topicRepository.findBySubjectIdAndNameIgnoreCase(request.subjectId, request.name)?.let { existing ->
            if (existing.id != id) {
                throw BusinessException("Topic with name '${request.name}' already exists in this subject")
            }
        }

        val updatedTopic = topic.copy(
            subject = subject,
            name = request.name
        )
        val savedTopic = topicRepository.save(updatedTopic)
        return mapToResponse(savedTopic)
    }

    fun deleteTopic(id: Long) {
        val topic = topicRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Topic not found with id: $id") }
        
        topicRepository.delete(topic)
    }

    private fun mapToResponse(topic: Topic): TopicResponse {
        return TopicResponse(
            id = topic.id,
            name = topic.name,
            subjectId = topic.subject.id,
            subjectName = topic.subject.name,
            createdAt = topic.createdAt
        )
    }
}
