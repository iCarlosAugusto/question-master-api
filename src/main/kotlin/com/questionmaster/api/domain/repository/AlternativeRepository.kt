package com.questionmaster.api.domain.repository

import com.questionmaster.api.domain.entity.Alternative
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AlternativeRepository : JpaRepository<Alternative, UUID> {
    fun findByQuestionIdOrderByOrd(questionId: UUID): List<Alternative>
    
    @Query("SELECT a FROM Alternative a WHERE a.question.id = :questionId AND a.isCorrect = true")
    fun findCorrectAlternativeByQuestionId(@Param("questionId") questionId: UUID): Alternative?
}
