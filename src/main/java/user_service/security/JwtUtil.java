package user_service.security;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import user_service.exception.JwtTokenInvalidException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {
    @Value("${JWT_KEY}")
    private String jwtKey;
    private SecretKey key;

    @PostConstruct
    public void setJwtKey() {
        this.key = Keys.hmacShaKeyFor(jwtKey.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isTokenValid(String accessToken) {
        JwtParser jwtParser = Jwts.parser().
                verifyWith(key)
                .build();
        try {
            jwtParser.parse(accessToken);
            return true;
        } catch (Exception e) {
            throw new JwtTokenInvalidException(e.getMessage());
        }
    }

    public long getUserIdFromToken(String accessToken) {
            String idSubject = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload()
                    .getSubject();
            return Long.parseLong(idSubject);
    }
}
