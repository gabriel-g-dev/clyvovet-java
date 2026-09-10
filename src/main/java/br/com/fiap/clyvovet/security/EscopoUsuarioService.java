package br.com.fiap.clyvovet.security;

import br.com.fiap.clyvovet.model.Consulta;
import br.com.fiap.clyvovet.model.Dono;
import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.repository.DonoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * O vínculo entre a credencial (USUARIOS) e o cadastro do tutor (DONOS) é o
 * e-mail, único nas duas tabelas. É isso que permite restringir os dados por
 * usuário sem uma chave estrangeira entre elas.
 */
@Service
public class EscopoUsuarioService {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final DonoRepository donoRepository;

    public EscopoUsuarioService(DonoRepository donoRepository) {
        this.donoRepository = donoRepository;
    }

    public boolean ehAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLE_ADMIN::equals);
    }

    public String emailDoUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Nenhum usuário autenticado na requisição");
        }
        return auth.getName();
    }

    @Transactional(readOnly = true)
    public Dono tutorLogado() {
        String email = emailDoUsuarioLogado();
        return donoRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException(
                        "Usuário " + email + " não possui cadastro de tutor vinculado"));
    }

    public Long tutorIdPermitido(Long tutorIdInformado) {
        if (ehAdmin()) {
            return tutorIdInformado;
        }
        return tutorLogado().getId();
    }

    public void garantirAcessoAoPet(Pet pet) {
        if (ehAdmin()) {
            return;
        }
        String donoDoPet = pet.getDono() != null ? pet.getDono().getEmail() : null;
        if (!emailDoUsuarioLogado().equals(donoDoPet)) {
            throw new AccessDeniedException("Este pet pertence a outro tutor");
        }
    }

    public void garantirAcessoAConsulta(Consulta consulta) {
        if (ehAdmin()) {
            return;
        }
        if (consulta.getPet() == null) {
            throw new AccessDeniedException("Consulta sem pet vinculado");
        }
        garantirAcessoAoPet(consulta.getPet());
    }
}
