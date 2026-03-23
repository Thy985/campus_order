package com.xingchen.backend.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class SpaFilter implements Filter {

    private static final Set<String> STATIC_EXTENSIONS = Set.of(
            ".html", ".css", ".js", ".json", ".xml",
            ".png", ".jpg", ".jpeg", ".gif", ".svg", ".ico", ".webp",
            ".woff", ".woff2", ".ttf", ".eot", ".otf",
            ".map"
    );

    private String indexHtmlContent;
    private volatile boolean initialized = false;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        loadIndexHtml();
    }

    private synchronized void loadIndexHtml() {
        if (initialized) return;
        try {
            Resource resource = new ClassPathResource("static/index.html");
            if (resource.exists()) {
                indexHtmlContent = StreamUtils.copyToString(
                        resource.getInputStream(),
                        StandardCharsets.UTF_8
                );
            }
            initialized = true;
        } catch (IOException e) {
            initialized = true;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        if (shouldForwardToSpa(requestURI)) {
            if (indexHtmlContent == null) {
                loadIndexHtml();
            }
            if (indexHtmlContent != null) {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write(indexHtmlContent);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean shouldForwardToSpa(String requestURI) {
        if (requestURI.startsWith("/api/")) {
            return false;
        }

        if (requestURI.startsWith("/files/")) {
            return false;
        }

        if (requestURI.startsWith("/swagger-ui/") ||
            requestURI.startsWith("/v3/api-docs") ||
            requestURI.startsWith("/actuator/")) {
            return false;
        }

        if (requestURI.startsWith("/ws/")) {
            return false;
        }

        for (String ext : STATIC_EXTENSIONS) {
            if (requestURI.toLowerCase().endsWith(ext)) {
                return false;
            }
        }

        return true;
    }
}
