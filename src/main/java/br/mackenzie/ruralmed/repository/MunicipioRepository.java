package br.mackenzie.ruralmed.repository;

import br.mackenzie.ruralmed.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Municípios (só leitura nas telas). Os métodos básicos (findAll, findById, save, delete) já vêm do JpaRepository. */
public interface MunicipioRepository extends JpaRepository<Municipio, Long> {
}
