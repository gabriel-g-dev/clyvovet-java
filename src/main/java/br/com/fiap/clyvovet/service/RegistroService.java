package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.RegistroRequestDTO;
import br.com.fiap.clyvovet.dto.RegistroResponseDTO;
import br.com.fiap.clyvovet.exception.RegraNegocioException;
import br.com.fiap.clyvovet.model.Dono;
import br.com.fiap.clyvovet.model.Usuario;
import br.com.fiap.clyvovet.repository.DonoRepository;
import br.com.fiap.clyvovet.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastro público do tutor (POST /api/auth/register).
 *
 * Grava duas linhas na mesma transação: a credencial em USUARIOS e o cadastro
 * em DONOS, com o mesmo e-mail nas duas. É esse e-mail compartilhado que depois
 * permite descobrir de quem são os pets ({@link br.com.fiap.clyvovet.security.EscopoUsuarioService}).
 */
@Service
public class RegistroService {

    public static final String ROLE_TUTOR = "ROLE_TUTOR";

    private final UsuarioRepository usuarioRepository;
    private final DonoRepository donoRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroService(UsuarioRepository usuarioRepository,
                           DonoRepository donoRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.donoRepository = donoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegistroResponseDTO registrarTutor(RegistroRequestDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();

        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com o e-mail " + email);
        }
        if (donoRepository.findByEmail(email).isPresent()) {
            throw new RegraNegocioException("Já existe um tutor cadastrado com o e-mail " + email);
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setRole(ROLE_TUTOR);
        Usuario credencial = usuarioRepository.save(usuario);

        Dono dono = new Dono();
        dono.setNome(dto.getNome().trim());
        dono.setEmail(email);
        dono.setTelefone(dto.getTelefone().trim());
        Dono tutor = donoRepository.save(dono);

        return new RegistroResponseDTO(
                credencial.getId(), tutor.getId(),
                tutor.getNome(), tutor.getEmail(), tutor.getTelefone(),
                credencial.getRole());
    }
}
