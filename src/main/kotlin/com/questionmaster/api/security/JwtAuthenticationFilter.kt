package com.questionmaster.api.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val customUserDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = getJwtFromRequest(request)
            
            logger.debug("🔍 [JWT Filter] Request: ${request.method} ${request.requestURI}")
            logger.debug("🔍 [JWT Filter] Has Authorization header: ${request.getHeader("Authorization") != null}")
            logger.debug("🔍 [JWT Filter] JWT extracted: ${if (jwt != null) "Yes (${jwt.take(20)}...)" else "No"}")
            
            if (StringUtils.hasText(jwt)) {
                logger.debug("🔍 [JWT Filter] Validating token...")
                val isValid = jwtTokenProvider.validateToken(jwt!!)
                logger.debug("🔍 [JWT Filter] Token valid: $isValid")
                
                if (isValid) {
                    val userId = jwtTokenProvider.getUserIdFromToken(jwt)
                    logger.debug("🔍 [JWT Filter] User ID from token: $userId")
                    
                    val userDetails = customUserDetailsService.loadUserByUsername(userId.toString())
                    logger.debug("🔍 [JWT Filter] User loaded: ${userDetails.username}")
                    logger.debug("🔍 [JWT Filter] Authorities: ${userDetails.authorities}")
                    
                    val authentication = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    )
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    
                    SecurityContextHolder.getContext().authentication = authentication
                    logger.debug("✅ [JWT Filter] Authentication set successfully!")
                } else {
                    logger.warn("⚠️ [JWT Filter] Token validation FAILED")
                }
            } else {
                logger.debug("ℹ️ [JWT Filter] No JWT token in request")
            }
        } catch (ex: Exception) {
            logger.error("❌ [JWT Filter] Error: ${ex.message}", ex)
        }
        
        filterChain.doFilter(request, response)
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}
