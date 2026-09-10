package br.com.fiap.clyvovet.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Token ausente ou inválido apenas deixa o SecurityContext vazio: quem decide
 * o 401/403 é a filter chain do SecurityConfig.
 */
public class TokenAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIXO = "Bearer ";

    private final TokenService tokenService;
    private final UsuarioDetailsService usuarioDetailsService;

    public TokenAuthFilter(TokenService tokenService, UsuarioDetailsService usuarioDetailsService) {
        this.tokenService = tokenService;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            extrairToken(request)
                    .flatMap(tokenService::validarToken)
                    .ifPresent(email -> autenticar(email, request));
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> extrairToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIXO)) {
            return Optional.of(header.substring(PREFIXO.length()).trim());
        }
        return Optional.empty();
    }

    private void autenticar(String email, HttpServletRequest request) {
        try {
            UserDetails userDetails = usuarioDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (UsernameNotFoundException e) {
            // Token assinado, mas o usuário foi removido depois da emissão.
            SecurityContextHolder.clearContext();
        }
    }
}
