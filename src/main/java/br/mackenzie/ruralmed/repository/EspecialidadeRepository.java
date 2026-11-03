package br.mackenzie.ruralmed.repository;

import br.mackenzie.ruralmed.model.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Especialidades. Os métodos básicos (findAll, findById, save, delete) já vêm do JpaRepository. */
public interface EspecialidadeRepository extends JpaRepository<Especialidade, Long> {

    List<Especialidade> findAllByOrderByNome();
}
