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
 * Resolve de quem é o dado que está sendo acessado.
 *
 * O vínculo entre a credencial e o cadastro do tutor é o e-mail: a linha de
 * USUARIOS e a linha de DONOS do mesmo tutor compartilham o mesmo e-mail, e
 * as duas colunas são únicas. Por isso dá para restringir os dados por usuário
 * sem chave estrangeira nova entre as tabelas.
 *
 * ADMIN é a equipe da clínica e enxerga todos os pets; TUTOR só alcança os
 * pets que estão no seu próprio cadastro.
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

    /**
     * Cadastro de tutor do usuário logado. Um ADMIN não precisa ter cadastro de
     * tutor, então este método só faz sentido no escopo de quem é TUTOR.
     */
    @Transactional(readOnly = true)
    public Dono tutorLogado() {
        String email = emailDoUsuarioLogado();
        return donoRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException(
                        "Usuário " + email + " não possui cadastro de tutor vinculado"));
    }

    /** ID do tutor a ser gravado no recurso: o ADMIN escolhe, o TUTOR é sempre ele mesmo. */
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
