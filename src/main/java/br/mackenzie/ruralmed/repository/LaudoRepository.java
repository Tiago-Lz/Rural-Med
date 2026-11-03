package br.mackenzie.ruralmed.repository;

import br.mackenzie.ruralmed.model.*;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Laudos. Os métodos básicos (findAll, findById, save, delete) já vêm do JpaRepository. */
public interface LaudoRepository extends JpaRepository<Laudo, Long> {

    List<Laudo> findByMedicoUsuarioIdOrderByIdDesc(Long medicoId);

    List<Laudo> findByStatusAndEmitidoEmBetween(StatusLaudo status, OffsetDateTime inicio, OffsetDateTime fim);
}
