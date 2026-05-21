package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.model.ConsultaMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultaMedicamentoRepository extends JpaRepository<ConsultaMedicamento, Long> {
}
