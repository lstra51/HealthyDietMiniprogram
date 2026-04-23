package com.cupk.healthy_diet.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cupk.healthy_diet.common.Result;
import com.cupk.healthy_diet.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthTokenManager authTokenManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (isPublicRequest(request)) {
            return true;
        }

        try {
            String token = extractToken(request);
            AuthTokenManager.AuthUser user = authTokenManager.parseToken(token);
            request.setAttribute(AuthContext.USER_ID, user.id());
            request.setAttribute(AuthContext.USER_ROLE, user.role());
            return true;
        } catch (BusinessException e) {
            writeError(response, e.getCode(), e.getMessage());
            return false;
        } catch (Exception e) {
            writeError(response, 401, "登录状态无效，请重新登录");
            return false;
        }
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method) || path.startsWith("/api/auth/")) {
            return true;
        }
        return "GET".equalsIgnoreCase(method)
                && ("/api/recipes".equals(path) || path.matches("/api/recipes/\\d+"));
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    private void writeError(HttpServletResponse response, Integer code, String message) throws IOException {
        response.setStatus(code == 403 ? HttpServletResponse.SC_FORBIDDEN : HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code, message)));
    }
}
