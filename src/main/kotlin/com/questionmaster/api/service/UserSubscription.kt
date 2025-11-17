package com.questionmaster.api.service

import com.questionmaster.api.domain.dto.request.CreateUserSubscriptionRequest
import com.questionmaster.api.domain.dto.request.UpdateUserSubscriptionStatusRequest
import com.questionmaster.api.domain.dto.response.UserSubscriptionResponse
import com.questionmaster.api.domain.entity.UserSubscription
import com.questionmaster.api.domain.repository.ExamRepository
import com.questionmaster.api.domain.repository.UserRepository
import com.questionmaster.api.domain.repository.UserSubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserSubscriptionService(
    private val userSubscriptionRepository: UserSubscriptionRepository,
    private val userRepository: UserRepository,
    private val examRepository: ExamRepository
) {

    fun create(request: CreateUserSubscriptionRequest): UserSubscriptionResponse {
        val user = userRepository.findById(request.userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        val exam = examRepository.findById(request.examId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found") }

        val now = LocalDateTime.now()

        val entity = UserSubscription(
            user = user,
            exam = exam,
            plan = request.plan,
            status = "INACTIVE", // ou já deixar "ACTIVE" dependendo da regra de negócio
            stripeCustomerId = request.stripeCustomerId,
            stripeSubscriptionId = request.stripeSubscriptionId,
            createdAt = now,
            updatedAt = now
        )

        return userSubscriptionRepository.save(entity).toResponse()
    }

    fun getById(id: UUID): UserSubscriptionResponse {
        val sub = userSubscriptionRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found") }

        return sub.toResponse()
    }

    fun listByUser(userId: UUID): List<UserSubscriptionResponse> {
        return userSubscriptionRepository.findAllByUserId(userId)
            .map { it.toResponse() }
    }

    fun updateStatus(id: UUID, request: UpdateUserSubscriptionStatusRequest): UserSubscriptionResponse {
        val sub = userSubscriptionRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found") }

        val updated = UserSubscription(
            id = sub.id,
            user = sub.user,
            exam = sub.exam,
            plan = sub.plan,
            status = request.status,
            stripeCustomerId = sub.stripeCustomerId,
            stripeSubscriptionId = sub.stripeSubscriptionId,
            createdAt = sub.createdAt,
            updatedAt = LocalDateTime.now()
        )

        return userSubscriptionRepository.save(updated).toResponse()
    }

    fun delete(id: UUID) {
        if (!userSubscriptionRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found")
        }
        userSubscriptionRepository.deleteById(id)
    }
}
