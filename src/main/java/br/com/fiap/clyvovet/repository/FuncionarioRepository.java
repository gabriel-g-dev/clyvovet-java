package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    List<Funcionario> findBySetor(String setor);
}
