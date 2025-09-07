package com.questionmaster.api.domain.repository

import com.questionmaster.api.domain.entity.Topic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TopicRepository : JpaRepository<Topic, Long> {
    fun findBySubjectIdAndNameIgnoreCase(subjectId: Long, name: String): Topic?
    
    fun findBySubjectId(subjectId: Long): List<Topic>
    
    @Query("SELECT t FROM Topic t JOIN FETCH t.subject WHERE t.id IN :ids")
    fun findByIdInWithSubject(ids: Set<Long>): List<Topic>
}
