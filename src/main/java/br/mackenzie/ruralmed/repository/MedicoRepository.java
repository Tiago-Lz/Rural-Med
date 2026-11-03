package br.mackenzie.ruralmed.repository;

import br.mackenzie.ruralmed.model.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Dados de médicos (chave = id do usuário). Os métodos básicos (findAll, findById, save, delete) já vêm do JpaRepository. */
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    /** Médicos que podem receber casos: da especialidade e com CRM ativo (RN04). */
    List<Medico> findByEspecialidadeIdAndCrmAtivoTrue(Long especialidadeId);
}
