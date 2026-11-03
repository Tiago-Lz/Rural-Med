package br.mackenzie.ruralmed.repository;

import br.mackenzie.ruralmed.model.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Códigos CID. Os métodos básicos (findAll, findById, save, delete) já vêm do JpaRepository. */
public interface CidRepository extends JpaRepository<Cid, String> {

    /** RF06: busca pelo código ou por parte da descrição. */
    List<Cid> findTop20ByCodigoContainingIgnoreCaseOrDescricaoContainingIgnoreCase(String codigo, String descricao);
}
