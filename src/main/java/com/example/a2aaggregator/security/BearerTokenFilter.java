package com.example.a2aaggregator.security;

import com.example.a2aaggregator.a2a.A2aModels.GoogleRpcError;
import com.example.a2aaggregator.a2a.A2aModels.GoogleRpcErrorBody;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class BearerTokenFilter extends OncePerRequestFilter {
    public static final String VALID_TOKEN = "test-token-123";
    private static final MediaType A2A_JSON = MediaType.parseMediaType("application/a2a+json");
    private final ObjectMapper objectMapper;

    public BearerTokenFilter(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().equals("/.well-known/agent-card.json");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        String token = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7) : null;
        if (!VALID_TOKEN.equals(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(A2A_JSON.toString());
            response.setHeader("WWW-Authenticate", "Bearer realm=\"a2a-aggregator\"");
            var detail = Map.<String, Object>of(
                    "@type", "type.googleapis.com/google.rpc.ErrorInfo",
                    "reason", "UNAUTHENTICATED", "domain", "a2a-protocol.org");
            objectMapper.writeValue(response.getOutputStream(), new GoogleRpcErrorBody(
                    new GoogleRpcError(401, "UNAUTHENTICATED", "Неверный или отсутствующий Bearer-токен",
                            List.of(detail))));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
