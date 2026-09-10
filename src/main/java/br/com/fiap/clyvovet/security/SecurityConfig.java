package br.com.fiap.clyvovet.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * São duas filter chains porque a API e o painel web falham de formas
 * diferentes: sem credencial, um cliente de API precisa de um 401 seco, e não
 * do redirect 302 para a página de login em HTML.
 *
 * As regras abaixo decidem o que cada perfil pode chamar; de quem é cada
 * registro é conferido depois, no EscopoUsuarioService.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String TUTOR = "TUTOR";

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http,
                                              TokenService tokenService,
                                              UsuarioDetailsService usuarioDetailsService) throws Exception {
        TokenAuthFilter tokenAuthFilter = new TokenAuthFilter(tokenService, usuarioDetailsService);

        http
            .securityMatcher("/api/**")
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Sem cookie de sessão nesta chain não há superfície para CSRF.
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()

                .requestMatchers("/api/medicamentos/**").hasRole(ADMIN)
                .requestMatchers("/api/tutores/**").hasRole(ADMIN)

                // O tutor precisa da lista de veterinários para agendar uma consulta,
                // mas quem mantém o cadastro da equipe é a clínica.
                .requestMatchers(HttpMethod.GET, "/api/funcionarios/**").hasAnyRole(ADMIN, TUTOR)
                .requestMatchers("/api/funcionarios/**").hasRole(ADMIN)

                // Registrar vacina e dar baixa na aplicação é ato clínico.
                .requestMatchers(HttpMethod.POST, "/api/vacinas/*/aplicar").hasRole(ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/vacinas").hasRole(ADMIN)
                .requestMatchers(HttpMethod.PUT, "/api/vacinas/**").hasRole(ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/api/vacinas/**").hasRole(ADMIN)

                .requestMatchers("/api/pets/**", "/api/consultas/**").hasAnyRole(ADMIN, TUTOR)

                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole(ADMIN, TUTOR)
                .anyRequest().hasRole(ADMIN)
            )
            .addFilterBefore(tokenAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(basic -> {})
            // 401 puro, sem WWW-Authenticate, para não abrir o popup do navegador.
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
            // O console do H2 posta formulários próprios, sem o token CSRF da aplicação.
            .csrf(csrf -> csrf.ignoringRequestMatchers(PathRequest.toH2Console()))
            // O console do H2 roda dentro de um <frame>.
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers(PathRequest.toH2Console()).permitAll()
                .requestMatchers("/login", "/error").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // O painel web é a ferramenta da clínica: o tutor cadastra pela API.
                .requestMatchers("/pets/novo", "/pets/salvar", "/vacinas/*/aplicar").hasRole(ADMIN)
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?erro")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/acesso-negado"));

        return http.build();
    }

    /**
     * Preparado para um cliente Expo: um app nativo não passa por CORS, mas o
     * Expo Web e o Expo Go rodam em origens http que o navegador barraria.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://192.168.*.*:*",
                "exp://*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
