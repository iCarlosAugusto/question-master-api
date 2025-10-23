package com.questionmaster.api.domain.repository

import com.questionmaster.api.domain.entity.Exam
import com.questionmaster.api.domain.enums.ExamType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ExamRepository : JpaRepository<Exam, Long> {

    fun findBySlug(slug: String): Optional<Exam>
    
    fun findByIsActiveTrue(pageable: Pageable): Page<Exam>

    fun findByInstitutionContainingIgnoreCaseAndIsActiveTrue(institution: String, pageable: Pageable): Page<Exam>
    
//    @Query("""
//        SELECT e FROM Exam e
//        WHERE e.isActive = true
//        AND (:institution IS NULL OR LOWER(e.institution) LIKE LOWER(CONCAT('%', :institution, '%')))
//    """)
//    fun findExamsWithFilters(
//        @Param("institution") institution: String?
//    ): List<Exam>
}

