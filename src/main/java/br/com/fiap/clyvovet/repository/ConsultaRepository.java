package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByVeterinarioContainingIgnoreCase(String veterinario);
    List<Consulta> findByDataBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Consulta> findByPetId(Long petId);
}