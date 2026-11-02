package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.model.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    @Query("select distinct p from Pet p join fetch p.dono left join fetch p.vacinas order by p.id")
    List<Pet> buscarParaPainel();

    @Query("select distinct p from Pet p join fetch p.dono d left join fetch p.vacinas where d.email = :email order by p.id")
    List<Pet> buscarParaPainelDoTutor(@Param("email") String email);

    List<Pet> findByEspecieIgnoreCase(String especie);
    List<Pet> findByRacaIgnoreCase(String raca);
    List<Pet> findByDonoId(Long donoId);

    Page<Pet> findByDonoEmail(String email, Pageable pageable);
    List<Pet> findByDonoEmail(String email);
    List<Pet> findByEspecieIgnoreCaseAndDonoEmail(String especie, String email);
    List<Pet> findByRacaIgnoreCaseAndDonoEmail(String raca, String email);
}
