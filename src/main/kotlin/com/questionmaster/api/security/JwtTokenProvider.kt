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
    private val jwtSecret: String = "myDefaultSecretKeyThatIsLongEnoughForHmacSha256Algorithm"

    @Value("\${app.jwt.expiration:86400000}") // 24 hours in milliseconds
    private val jwtExpiration: Long = 86400000

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
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
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        return UUID.fromString(claims.subject)
    }

    fun getRoleFromToken(token: String): AppRole {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        val roleString = claims.get("role", String::class.java)
        return AppRole.valueOf(roleString)
    }

    fun getDisplayNameFromToken(token: String): String? {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        return claims.get("displayName", String::class.java)
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            true
        } catch (ex: SecurityException) {
            false
        } catch (ex: MalformedJwtException) {
            false
        } catch (ex: ExpiredJwtException) {
            false
        } catch (ex: UnsupportedJwtException) {
            false
        } catch (ex: IllegalArgumentException) {
            false
        }
    }
}
