package com.questionmaster.api.domain.repository

import com.questionmaster.api.domain.entity.Subject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SubjectRepository : JpaRepository<Subject, Long> {
    fun findByNameIgnoreCase(name: String): Subject?
    
    @Query("SELECT s FROM Subject s LEFT JOIN FETCH s.topics")
    fun findAllWithTopics(): List<Subject>
}
