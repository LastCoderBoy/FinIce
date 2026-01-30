package com.JK.FinIce.authservice.config.security;

import com.JK.FinIce.authservice.config.redis.RedisService;
import com.JK.FinIce.authservice.entity.UserPrincipal;
import com.JK.FinIce.commonlibrary.exception.InvalidTokenException;
import com.JK.FinIce.commonlibrary.utils.TokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.JK.FinIce.commonlibrary.constants.AppConstants.AUTHORIZATION_HEADER;
import static com.JK.FinIce.commonlibrary.constants.AppConstants.PUBLIC_PATHS;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        // Skip public paths
        if (isPublicPath(path)) {
            log.debug("[JWT-FILTER] Public path, skipping authentication: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        try{
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            String token = TokenUtils.validateAndExtractToken(authHeader);

            if(!jwtProvider.validateToken(token)){
                log.warn("[JWT-FILTER] Invalid JWT token");
                throw new InvalidTokenException("Invalid or expired token");
            }

            // TODO: check if token is blacklisted or not (implement the method by checking Redis)
            if(redisService.isTokenBlacklisted(token)){
                log.warn("[JWT-FILTER] Token is blacklisted");
                throw new InvalidTokenException("Token is blacklisted");
            }

            String username = jwtProvider.getUsernameFromToken(token);
            Long userId = jwtProvider.getUserIdFromToken(token);
            String email = jwtProvider.getEmailFromToken(token);  // Add this method
            List<String> roles = jwtProvider.getRolesFromToken(token);

            // Create UserPrincipal from JWT claims
            UserPrincipal userPrincipal = UserPrincipal.fromJwtClaims(userId, username, email, roles);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal,
                    null,
                    userPrincipal.getAuthorities()
            );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("[JWT-FILTER] Authentication successful for user: {}", username);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("[JWT-FILTER] Error while processing request: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()
                .anyMatch(publicPath -> pathMatcher.match(publicPath, path));
    }
}
