package br.mackenzie.ruralmed.repository;

import br.mackenzie.ruralmed.model.*;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Usuários do sistema. Os métodos básicos (findAll, findById, save, delete) já vêm do JpaRepository. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByCpf(String cpf);

    List<Usuario> findAllByOrderByNome();
}
