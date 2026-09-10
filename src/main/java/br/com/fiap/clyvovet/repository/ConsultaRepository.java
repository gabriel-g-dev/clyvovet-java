package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.model.Consulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByStatus(String status);
    List<Consulta> findByDataBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Consulta> findByPetId(Long petId);

    Page<Consulta> findByPetDonoEmail(String email, Pageable pageable);
    List<Consulta> findByStatusAndPetDonoEmail(String status, String email);
    List<Consulta> findByDataBetweenAndPetDonoEmail(LocalDateTime inicio, LocalDateTime fim, String email);
}
