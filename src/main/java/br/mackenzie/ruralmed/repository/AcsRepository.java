package br.mackenzie.ruralmed.repository;

import br.mackenzie.ruralmed.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Dados de ACS (chave = id do usuário). Os métodos básicos (findAll, findById, save, delete) já vêm do JpaRepository. */
public interface AcsRepository extends JpaRepository<Acs, Long> {
}
