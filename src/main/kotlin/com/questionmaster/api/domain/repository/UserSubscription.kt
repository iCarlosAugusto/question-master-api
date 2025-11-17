package com.questionmaster.api.domain.repository

import com.questionmaster.api.domain.entity.UserSubscription
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserSubscriptionRepository : JpaRepository<UserSubscription, UUID> {

    fun findAllByUserId(userId: UUID): List<UserSubscription>
}
