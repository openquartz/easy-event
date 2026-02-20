package com.openquartz.easyevent.admin.interceptor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.openquartz.easyevent.admin.annotation.Permission;
import com.openquartz.easyevent.admin.annotation.RateLimit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Admin Interceptor for Rate Limiting and Permission Check
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Value("${easyevent.admin.token:admin}")
    private String adminToken;

    private final Cache<String, AtomicInteger> rateLimitCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // Rate Limit Check
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit != null) {
            String key = request.getRemoteAddr() + ":" + handlerMethod.getMethod().getName();
            AtomicInteger count = rateLimitCache.get(key, k -> new AtomicInteger(0));
            if (count.incrementAndGet() > rateLimit.limit()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many requests");
                return false;
            }
        }

        // Permission Check
        Permission permission = handlerMethod.getMethodAnnotation(Permission.class);
        if (permission != null) {
            String token = request.getHeader("Authorization");
            if (token == null || !token.equals(adminToken)) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.getWriter().write("Permission denied");
                return false;
            }
        }

        return true;
    }
}
