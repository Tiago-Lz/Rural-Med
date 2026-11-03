package br.mackenzie.ruralmed.repository;

import br.mackenzie.ruralmed.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Receitas. Os métodos básicos (findAll, findById, save, delete) já vêm do JpaRepository. */
public interface ReceitaRepository extends JpaRepository<Receita, Long> {
}
