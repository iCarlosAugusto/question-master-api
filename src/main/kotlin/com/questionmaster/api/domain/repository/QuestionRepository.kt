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
        LEFT JOIN FETCH q.exam
        LEFT JOIN FETCH q.topics 
        LEFT JOIN FETCH q.alternatives a
        WHERE q.isActive = true
        AND q.exam.id = :examId
        AND (:#{#subjectIds.size()} = 0 OR q.subject.id IN :subjectIds)
        AND (:#{#years.size()} = 0 OR q.year IN :years)
        AND (:questionType IS NULL OR q.questionType = :questionType)
        AND (:#{#topicIds.size()} = 0 OR EXISTS (SELECT 1 FROM q.topics t WHERE t.id IN :topicIds))
        ORDER BY q.createdAt DESC
    """)
    fun findQuestionsWithFilters(
        @Param("examId") examId: Long,
        @Param("subjectIds") subjectIds: List<Long>,
        @Param("years") years: List<Short>,
        @Param("questionType") questionType: QuestionType?,
        @Param("topicIds") topicIds: List<Long>,
        pageable: Pageable
    ): Page<Question>
    
    @Query("""
        SELECT DISTINCT q FROM Question q 
        LEFT JOIN FETCH q.subject 
        LEFT JOIN FETCH q.exam
        LEFT JOIN FETCH q.topics 
        LEFT JOIN FETCH q.alternatives a
        WHERE q.isActive = true
        AND q.exam.id = :examId
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
        @Param("examId") examId: Long,
        @Param("userId") userId: UUID?,
        @Param("answerStatus") answerStatus: String?,
        pageable: Pageable
    ): Page<Question>

    @Query("""
SELECT DISTINCT q FROM Question q
LEFT JOIN FETCH q.subject
LEFT JOIN FETCH q.exam
LEFT JOIN FETCH q.topics
LEFT JOIN FETCH q.alternatives a
WHERE q.isActive = true
  AND q.exam.id = :examId
  AND (:#{#subjectIds.size()} = 0 OR q.subject.id IN :subjectIds)
  AND (:#{#years.size()} = 0 OR q.year IN :years)
  AND (:questionType IS NULL OR q.questionType = :questionType)
  AND (:#{#topicIds.size()} = 0 OR EXISTS (
        SELECT 1 FROM q.topics t WHERE t.id IN :topicIds
  ))
  AND (
    :userId IS NULL
    OR :answerStatus IS NULL
    OR (
      :answerStatus = 'UNANSWERED'
      AND NOT EXISTS (
        SELECT 1 FROM Answer a
        WHERE a.question.id = q.id
          AND a.user.id = :userId
      )
    )
    OR (
      :answerStatus IN ('ANSWERED', 'CORRECT', 'INCORRECT')
      AND EXISTS (
        SELECT 1 FROM Answer ans
        WHERE ans.question.id = q.id
          AND ans.user.id = :userId
          AND ans.answeredAt = (
            SELECT MAX(a2.answeredAt)
            FROM Answer a2
            WHERE a2.question.id = q.id
              AND a2.user.id = :userId
          )
          AND (
            (:answerStatus = 'ANSWERED')
            OR (:answerStatus = 'CORRECT' AND ans.isCorrect = true)
            OR (:answerStatus = 'INCORRECT' AND ans.isCorrect = false)
          )
      )
    )
  )
ORDER BY q.createdAt DESC
""")
    fun findQuestionsWithFiltersAndUserStatus(
        @Param("examId") examId: Long,
        @Param("subjectIds") subjectIds: List<Long>,
        @Param("years") years: List<Short>,
        @Param("questionType") questionType: QuestionType?,
        @Param("topicIds") topicIds: List<Long>,
        @Param("userId") userId: UUID?,
        @Param("answerStatus") answerStatus: String?,
        pageable: Pageable
    ): Page<Question>



    @Query("""
        SELECT q FROM Question q 
        LEFT JOIN FETCH q.subject 
        LEFT JOIN FETCH q.exam
        LEFT JOIN FETCH q.topics 
        LEFT JOIN FETCH q.alternatives 
        WHERE q.id = :id
    """)
    fun findByIdWithDetails(@Param("id") id: UUID): Question?
    
    fun findByIsActiveTrue(): List<Question>
}
