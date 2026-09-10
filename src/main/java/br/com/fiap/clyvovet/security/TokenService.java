package br.com.fiap.clyvovet.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    public static final String CLAIM_ROLE = "role";

    private final SecretKey chave;
    private final String issuer;
    private final long expiracaoMinutos;

    public TokenService(@Value("${clyvovet.jwt.secret}") String secretBase64,
                        @Value("${clyvovet.jwt.issuer}") String issuer,
                        @Value("${clyvovet.jwt.expiration-minutes}") long expiracaoMinutos) {
        this.chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
        this.issuer = issuer;
        this.expiracaoMinutos = expiracaoMinutos;
    }

    public String gerarToken(String email, String role) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plusSeconds(expiracaoMinutos * 60);

        return Jwts.builder()
                .issuer(issuer)
                .subject(email)
                .claim(CLAIM_ROLE, role)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .signWith(chave, Jwts.SIG.HS256)
                .compact();
    }

    public long getExpiraEmSegundos() {
        return expiracaoMinutos * 60;
    }

    public Optional<String> validarToken(String token) {
        return lerClaims(token).map(Claims::getSubject);
    }

    public Optional<String> extrairRole(String token) {
        return lerClaims(token).map(claims -> claims.get(CLAIM_ROLE, String.class));
    }

    private Optional<Claims> lerClaims(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(chave)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
