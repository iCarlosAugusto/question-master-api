package com.questionmaster.api.domain.repository

import com.questionmaster.api.domain.entity.Question
import com.questionmaster.api.domain.enums.QuestionType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface QuestionRepository : JpaRepository<Question, UUID> {
    
    @Query("""
        SELECT DISTINCT q FROM Question q 
        LEFT JOIN FETCH q.subject 
        LEFT JOIN FETCH q.topics 
        LEFT JOIN FETCH q.alternatives a
        WHERE q.isActive = true
        AND (:subjectId IS NULL OR q.subject.id = :subjectId)
        AND (:year IS NULL OR q.year = :year)
        AND (:questionType IS NULL OR q.questionType = :questionType)
        AND (:topicId IS NULL OR EXISTS (SELECT 1 FROM q.topics t WHERE t.id = :topicId))
        ORDER BY q.createdAt DESC
    """)
    fun findQuestionsWithFilters(
        @Param("subjectId") subjectId: Long?,
        @Param("year") year: Short?,
        @Param("questionType") questionType: QuestionType?,
        @Param("topicId") topicId: Long?,
        pageable: Pageable
    ): Page<Question>
    
    @Query("""
        SELECT DISTINCT q FROM Question q 
        LEFT JOIN FETCH q.subject 
        LEFT JOIN FETCH q.topics 
        LEFT JOIN FETCH q.alternatives a
        WHERE q.isActive = true
        AND (:userId IS NULL OR (
            CASE 
                WHEN :answerStatus = 'ANSWERED' THEN EXISTS (SELECT 1 FROM Answer ans WHERE ans.question.id = q.id AND ans.user.id = :userId)
                WHEN :answerStatus = 'UNANSWERED' THEN NOT EXISTS (SELECT 1 FROM Answer ans WHERE ans.question.id = q.id AND ans.user.id = :userId)
                WHEN :answerStatus = 'CORRECT' THEN EXISTS (SELECT 1 FROM Answer ans WHERE ans.question.id = q.id AND ans.user.id = :userId AND ans.isCorrect = true)
                WHEN :answerStatus = 'INCORRECT' THEN EXISTS (SELECT 1 FROM Answer ans WHERE ans.question.id = q.id AND ans.user.id = :userId AND ans.isCorrect = false)
                ELSE true
            END
        ))
        ORDER BY q.createdAt DESC
    """)
    fun findQuestionsWithUserStatus(
        @Param("userId") userId: UUID?,
        @Param("answerStatus") answerStatus: String?,
        pageable: Pageable
    ): Page<Question>
    
    @Query("""
        SELECT q FROM Question q 
        LEFT JOIN FETCH q.subject 
        LEFT JOIN FETCH q.topics 
        LEFT JOIN FETCH q.alternatives 
        WHERE q.id = :id
    """)
    fun findByIdWithDetails(@Param("id") id: UUID): Question?
    
    fun findByIsActiveTrue(): List<Question>
}
