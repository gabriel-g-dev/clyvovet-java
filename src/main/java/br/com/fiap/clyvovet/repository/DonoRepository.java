package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.model.Dono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DonoRepository extends JpaRepository<Dono, Long> {
    Optional<Dono> findByEmail(String email);
}
