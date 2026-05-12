package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByEspecieIgnoreCase(String especie);
    List<Pet> findByRacaIgnoreCase(String raca);
    List<Pet> findByTutorId(Long tutorId);
}