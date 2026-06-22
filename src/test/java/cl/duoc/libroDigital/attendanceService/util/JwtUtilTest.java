package cl.duoc.libroDigital.attendanceService.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET = "librodigital2026SecretKeyForJWTTokenGenerationAndValidation12345";

    private JwtUtil jwtUtil;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    @Test
    void extractUsernameUserIdAndRoles() {
        String token = Jwts.builder()
                .setSubject("demo")
                .claim("userId", 42L)
                .claim("roles", "DOCENTE,APODERADO")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(secretKey)
                .compact();

        assertEquals("demo", jwtUtil.extractUsername(token));
        assertEquals(42L, jwtUtil.extractUserId(token));
        assertEquals(List.of("DOCENTE", "APODERADO"), jwtUtil.extractRoles(token));
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_rejectsInvalidToken() {
        assertFalse(jwtUtil.validateToken("token.invalido"));
    }
}
