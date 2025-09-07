package com.questionmaster.api.domain.repository

import com.questionmaster.api.domain.entity.Answer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AnswerRepository : JpaRepository<Answer, UUID> {
    fun findByUserIdAndQuestionId(userId: UUID, questionId: UUID): Answer?
    
    fun findByUserId(userId: UUID): List<Answer>
    
    fun findByQuestionId(questionId: UUID): List<Answer>
    
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.user.id = :userId AND a.isCorrect = true")
    fun countCorrectAnswersByUserId(@Param("userId") userId: UUID): Long
    
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.user.id = :userId")
    fun countTotalAnswersByUserId(@Param("userId") userId: UUID): Long
    
    @Query("""
        SELECT a FROM Answer a 
        JOIN FETCH a.question q 
        JOIN FETCH a.alternative alt 
        WHERE a.user.id = :userId 
        ORDER BY a.answeredAt DESC
    """)
    fun findByUserIdWithDetails(@Param("userId") userId: UUID): List<Answer>
}
