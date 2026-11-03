package br.mackenzie.ruralmed.repository;

import br.mackenzie.ruralmed.model.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Dossiês de triagem. Os métodos básicos (findAll, findById, save, delete) já vêm do JpaRepository. */
public interface DossieRepository extends JpaRepository<Dossie, Long> {

    /** Usado na sincronização para não duplicar triagens (RNF02). */
    Optional<Dossie> findByIdLocal(UUID idLocal);

    List<Dossie> findAllByOrderByColetadoEmDesc();

    List<Dossie> findByStatusOrderByEscoreDesc(StatusDossie status);

    List<Dossie> findByAcsUsuarioIdOrderByColetadoEmDesc(Long acsId);

    /** RN05: fila do médico, do maior escore para o menor e, no empate, quem chegou primeiro. */
    List<Dossie> findByStatusAndEspecialidadeIdOrderByEscoreDescEncaminhadoEmAsc(StatusDossie status, Long especialidadeId);

    /** RF08: histórico do paciente em ordem cronológica. */
    List<Dossie> findByPacienteIdOrderByColetadoEmAsc(Long pacienteId);

    List<Dossie> findByColetadoEmBetween(OffsetDateTime inicio, OffsetDateTime fim);
}
