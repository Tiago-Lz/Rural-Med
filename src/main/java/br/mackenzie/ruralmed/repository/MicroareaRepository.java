package br.mackenzie.ruralmed.repository;

import br.mackenzie.ruralmed.model.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Microáreas. Os métodos básicos (findAll, findById, save, delete) já vêm do JpaRepository. */
public interface MicroareaRepository extends JpaRepository<Microarea, Long> {

    List<Microarea> findAllByOrderByNome();
}
