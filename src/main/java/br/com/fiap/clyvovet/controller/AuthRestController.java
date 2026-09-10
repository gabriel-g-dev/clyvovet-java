package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.LoginRequestDTO;
import br.com.fiap.clyvovet.dto.LoginResponseDTO;
import br.com.fiap.clyvovet.dto.RegistroRequestDTO;
import br.com.fiap.clyvovet.dto.RegistroResponseDTO;
import br.com.fiap.clyvovet.repository.DonoRepository;
import br.com.fiap.clyvovet.security.TokenService;
import br.com.fiap.clyvovet.service.RegistroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Cadastro de tutor, login JWT e perfil do usuário")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RegistroService registroService;
    private final DonoRepository donoRepository;

    public AuthRestController(AuthenticationManager authenticationManager,
                              TokenService tokenService,
                              RegistroService registroService,
                              DonoRepository donoRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.registroService = registroService;
        this.donoRepository = donoRepository;
    }

    @PostMapping("/register")
    @Operation(summary = "Cadastra um novo tutor e o seu login")
    public ResponseEntity<RegistroResponseDTO> registrar(@Valid @RequestBody RegistroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registroService.registrarTutor(dto));
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica o usuário e devolve o token de acesso")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha()));

        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_TUTOR");

        LoginResponseDTO resposta = new LoginResponseDTO(
                tokenService.gerarToken(auth.getName(), role),
                "Bearer",
                auth.getName(),
                role,
                tokenService.getExpiraEmSegundos());

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/me")
    @Operation(summary = "Devolve o perfil do usuário dono do token enviado")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        // A rota é permitAll, então a checagem de autenticação é feita aqui.
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensagem", "Token ausente ou inválido"));
        }

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");

        Map<String, Object> perfil = new LinkedHashMap<>();
        perfil.put("email", authentication.getName());
        perfil.put("role", role);

        // O tutor recebe aqui o próprio cadastro, e com isso o cliente não precisa
        // pedir a lista de tutores, que é restrita ao ADMIN.
        donoRepository.findByEmail(authentication.getName()).ifPresent(dono -> {
            perfil.put("tutorId", dono.getId());
            perfil.put("nome", dono.getNome());
            perfil.put("telefone", dono.getTelefone());
        });

        return ResponseEntity.ok(perfil);
    }
}
