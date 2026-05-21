package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
}
