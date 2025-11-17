package com.questionmaster.api.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ApiKeyAuthFilter(
    @Value("\${internal.api.key}") private val internalApiKey: String
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return !request.requestURI.startsWith("/internal/")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val key = request.getHeader("X-API-Key")

        if (key == null) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            response.writer.write("""{"error":"Missing API Key"}""")
            return
        }

        if (key != internalApiKey) {
            response.status = HttpServletResponse.SC_FORBIDDEN
            response.contentType = "application/json"
            response.writer.write("""{"error":"Invalid API Key"}""")
            return
        }

        // Se a chave estiver correta, segue o fluxo normal
        filterChain.doFilter(request, response)
    }
}
