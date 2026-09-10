package br.com.fiap.clyvovet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Separada de {@link Dono} porque nem todo usuário do sistema é tutor de um
 * pet (o ADMIN da clínica, por exemplo), e nem todo tutor precisa ter login.
 */
@Entity
@Table(name = "USUARIOS", uniqueConstraints = {@UniqueConstraint(name = "UQ_USUARIO_EMAIL", columnNames = {"usuario_email"})})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long id;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Column(name = "usuario_email", length = 100, nullable = false)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Column(name = "usuario_senha", length = 100, nullable = false)
    private String senha;

    @NotBlank(message = "Role é obrigatória")
    @Column(name = "usuario_role", length = 20, nullable = false)
    private String role;

    public Usuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
