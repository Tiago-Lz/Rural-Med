package br.mackenzie.ruralmed.repository;

import br.mackenzie.ruralmed.model.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Clínicas parceiras. Os métodos básicos (findAll, findById, save, delete) já vêm do JpaRepository. */
public interface ClinicaParceiraRepository extends JpaRepository<ClinicaParceira, Long> {

    List<ClinicaParceira> findAllByOrderByNome();
}
