package com.questionmaster.api.security

import com.questionmaster.api.domain.entity.User
import com.questionmaster.api.domain.enums.AppRole
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

class CustomUserDetails(
    private val user: User
) : UserDetails {

    companion object {
        fun create(user: User): CustomUserDetails {
            return CustomUserDetails(user)
        }
    }

    fun getId(): UUID = user.id

    fun getRole(): AppRole = user.role

    fun getDisplayName(): String? = user.displayName

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
    }

    override fun getPassword(): String {
        // Password is handled by Supabase, not stored in our database
        return ""
    }

    override fun getUsername(): String {
        return user.id.toString()
    }

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
