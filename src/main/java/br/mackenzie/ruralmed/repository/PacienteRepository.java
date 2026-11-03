package br.mackenzie.ruralmed.repository;

import br.mackenzie.ruralmed.model.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Pacientes. Os métodos básicos (findAll, findById, save, delete) já vêm do JpaRepository. */
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    List<Paciente> findAllByOrderByNome();

    /** RN02: pacientes de uma microárea. */
    List<Paciente> findByMicroareaIdAndAtivoTrueOrderByNome(Long microareaId);
}
