package com.questionmaster.api.security

import com.questionmaster.api.domain.enums.AppRole
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider {

    @Value("\${app.jwt.secret:mySecretKey}")
    private val jwtSecret: String = "+zpR/X0Hgds89beVkgtFrnkqx8tzt0pJ/E0EE4emvHInj8XgHI8+AFR14eLzfoGBZ92vUe7TEwox6l8OCT8EMg=="

    @Value("\${app.jwt.expiration:86400000}") // 24 hours in milliseconds
    private val jwtExpiration: Long = 86400000
    
    @Value("\${supabase.jwt.secret:}")
    private val supabaseJwtSecret: String = ""

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }
    
    private val supabaseKey: SecretKey? by lazy {
        if (supabaseJwtSecret.isNotBlank()) {
            Keys.hmacShaKeyFor(supabaseJwtSecret.toByteArray())
        } else {
            null
        }
    }

    fun generateToken(userId: UUID, role: AppRole, displayName: String?): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpiration)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("role", role.name)
            .claim("displayName", displayName)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun getUserIdFromToken(token: String): UUID {
        val claims = parseToken(token)
        return UUID.fromString(claims.subject)
    }

    fun getRoleFromToken(token: String): AppRole {
        val claims = parseToken(token)
        
        // Try to get role from custom claim (our JWT)
        val roleString = claims.get("role", String::class.java)
        if (roleString != null) {
            return AppRole.valueOf(roleString)
        }
        
        // For Supabase tokens, check user_metadata
        val userMetadata = claims.get("user_metadata", Map::class.java) as? Map<*, *>
        val supabaseRole = userMetadata?.get("role") as? String
        
        return when (supabaseRole?.uppercase()) {
            "ADMIN" -> AppRole.ADMIN
            else -> AppRole.USER
        }
    }

    fun getDisplayNameFromToken(token: String): String? {
        val claims = parseToken(token)
        
        // Try our custom claim first
        val displayName = claims.get("displayName", String::class.java)
        if (displayName != null) {
            return displayName
        }
        
        // Try Supabase user_metadata
        val userMetadata = claims.get("user_metadata", Map::class.java) as? Map<*, *>
        return (userMetadata?.get("display_name") 
            ?: userMetadata?.get("username") 
            ?: userMetadata?.get("email")) as? String
    }
    
    private fun parseToken(token: String): Claims {
        // Try with our key first
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            // If that fails, try with Supabase key
            if (supabaseKey != null) {
                try {
                    Jwts.parser()
                        .verifyWith(supabaseKey!!)
                        .build()
                        .parseSignedClaims(token)
                        .payload
                } catch (ex: Exception) {
                    throw ex
                }
            } else {
                throw e
            }
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            parseToken(token)
            true
        } catch (ex: SecurityException) {
            println("Security exception: ${ex.message}")
            false
        } catch (ex: MalformedJwtException) {
            println("Malformed JWT: ${ex.message}")
            false
        } catch (ex: ExpiredJwtException) {
            println("Expired JWT: ${ex.message}")
            false
        } catch (ex: UnsupportedJwtException) {
            println("Unsupported JWT: ${ex.message}")
            false
        } catch (ex: IllegalArgumentException) {
            println("Illegal argument: ${ex.message}")
            false
        } catch (ex: Exception) {
            println("JWT validation error: ${ex.message}")
            false
        }
    }
}
