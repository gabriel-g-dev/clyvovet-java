package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.model.Consulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    boolean existsByPetIdAndStatus(Long petId, String status);

    @Override
    @EntityGraph(attributePaths = {"pet", "funcionario"})
    Page<Consulta> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"pet", "funcionario"})
    List<Consulta> findByStatus(String status);
    @EntityGraph(attributePaths = {"pet", "funcionario"})
    List<Consulta> findByDataBetween(LocalDateTime inicio, LocalDateTime fim);
    @EntityGraph(attributePaths = {"pet", "funcionario"})
    List<Consulta> findByPetId(Long petId);

    @EntityGraph(attributePaths = {"pet", "funcionario"})
    Page<Consulta> findByPetDonoEmail(String email, Pageable pageable);
    @EntityGraph(attributePaths = {"pet", "funcionario"})
    List<Consulta> findByStatusAndPetDonoEmail(String status, String email);
    @EntityGraph(attributePaths = {"pet", "funcionario"})
    List<Consulta> findByDataBetweenAndPetDonoEmail(LocalDateTime inicio, LocalDateTime fim, String email);
}
