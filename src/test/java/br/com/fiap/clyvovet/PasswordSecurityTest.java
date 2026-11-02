package br.com.fiap.clyvovet;

import br.com.fiap.clyvovet.dto.RegistroRequestDTO;
import br.com.fiap.clyvovet.security.SecurityConfig;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordSecurityTest {
    @Test
    void bcryptNaoAceitaSufixosApos72Bytes() {
        var encoder = new SecurityConfig().passwordEncoder();
        String senha = "a".repeat(72);
        String hash = encoder.encode(senha);
        assertThat(encoder.matches(senha, hash)).isTrue();
        assertThat(encoder.matches(senha + "x", hash)).isFalse();
    }

    @Test
    void cadastroValidaBytesEmSenhasUnicode() {
        RegistroRequestDTO dto = new RegistroRequestDTO();
        dto.setSenha("á".repeat(40));
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validateProperty(dto, "senhaCompativelComBCrypt")).isNotEmpty();
            dto.setSenha("á".repeat(30));
            assertThat(factory.getValidator().validateProperty(dto, "senhaCompativelComBCrypt")).isEmpty();
        }
    }
}
