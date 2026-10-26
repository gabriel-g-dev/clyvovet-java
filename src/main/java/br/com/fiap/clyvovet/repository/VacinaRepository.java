package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.model.Vacina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VacinaRepository extends JpaRepository<Vacina, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vacina v where v.id = :id")
    Optional<Vacina> buscarParaAplicacao(@Param("id") Long id);

    List<Vacina> findByStatus(String status);
    List<Vacina> findByNomeContainingIgnoreCase(String nome);
    List<Vacina> findByPetId(Long petId);
    boolean existsByPetIdAndNomeAndStatusAndData(Long petId, String nome, String status, java.time.LocalDate data);

    Page<Vacina> findByPetDonoEmail(String email, Pageable pageable);
    List<Vacina> findByStatusAndPetDonoEmail(String status, String email);
    List<Vacina> findByNomeContainingIgnoreCaseAndPetDonoEmail(String nome, String email);
}
