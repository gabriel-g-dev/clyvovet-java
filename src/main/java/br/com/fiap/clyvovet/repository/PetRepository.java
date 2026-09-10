package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.model.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByEspecieIgnoreCase(String especie);
    List<Pet> findByRacaIgnoreCase(String raca);
    List<Pet> findByDonoId(Long donoId);

    Page<Pet> findByDonoEmail(String email, Pageable pageable);
    List<Pet> findByDonoEmail(String email);
    List<Pet> findByEspecieIgnoreCaseAndDonoEmail(String especie, String email);
    List<Pet> findByRacaIgnoreCaseAndDonoEmail(String raca, String email);
}
