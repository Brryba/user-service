package user_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            send401UnauthorizedResponse(response, "Bearer authorization token is not found");
            return;
        }

        try {
            String jwt = authHeader.substring(7);

            if (jwtUtil.isTokenValid(jwt)) {
                Long userId = jwtUtil.getUserIdFromToken(jwt);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId, null, null);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                send401UnauthorizedResponse(response, "Jwt token is invalid");
                return;
            }

            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            send401UnauthorizedResponse(response, exception.getMessage());
        }
    }

    private void send401UnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message +"\"}");
    }
}