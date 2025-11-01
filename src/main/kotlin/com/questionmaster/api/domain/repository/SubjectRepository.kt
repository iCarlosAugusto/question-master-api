package com.questionmaster.api.domain.repository

import com.questionmaster.api.domain.entity.Subject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SubjectRepository : JpaRepository<Subject, Long> {
    fun findByNameIgnoreCase(name: String): Subject?
    
    @Query("SELECT s FROM Subject s LEFT JOIN FETCH s.topics")
    fun findAllWithTopics(): List<Subject>
    
    @Query("""
        SELECT s FROM Subject s 
        WHERE s.exam.id = :examId
    """)
    fun findAllByExamId(@Param("examId") examId: Long): List<Subject>
    
    @Query("""
        SELECT s FROM Subject s 
        WHERE s.exam.slug = :examSlug
    """)
    fun findAllByExamSlug(@Param("examSlug") examSlug: String): List<Subject>
    
    @Query("""
        SELECT s FROM Subject s 
        LEFT JOIN FETCH s.topics
        WHERE s.exam.slug = :examSlug
    """)
    fun findAllByExamSlugWithTopics(@Param("examSlug") examSlug: String): List<Subject>
}
