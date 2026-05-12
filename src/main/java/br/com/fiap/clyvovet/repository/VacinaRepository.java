package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.model.Vacina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacinaRepository extends JpaRepository<Vacina, Long> {
    List<Vacina> findByAplicadaFalse();
    List<Vacina> findByNomeContainingIgnoreCase(String nome);
    List<Vacina> findByProximaDoseLessThanEqualAndAplicadaFalse(LocalDate data);
    List<Vacina> findByPetId(Long petId);
}