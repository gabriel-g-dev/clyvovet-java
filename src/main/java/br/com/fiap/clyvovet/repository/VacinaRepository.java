package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.model.Vacina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VacinaRepository extends JpaRepository<Vacina, Long> {
    List<Vacina> findByStatus(String status);
    List<Vacina> findByNomeContainingIgnoreCase(String nome);
    List<Vacina> findByPetId(Long petId);
    boolean existsByPetIdAndNomeAndStatusAndData(Long petId, String nome, String status, java.time.LocalDate data);

    Page<Vacina> findByPetDonoEmail(String email, Pageable pageable);
    List<Vacina> findByStatusAndPetDonoEmail(String status, String email);
    List<Vacina> findByNomeContainingIgnoreCaseAndPetDonoEmail(String nome, String email);
}
