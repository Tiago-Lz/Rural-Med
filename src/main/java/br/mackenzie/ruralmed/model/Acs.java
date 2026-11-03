package br.mackenzie.ruralmed.model;

import jakarta.persistence.*;

/** Dados extras do usuário com perfil ACS: a microárea onde ele atua. Tabela: acs. */
@Entity
@Table(name = "acs")
public class Acs {

    @Id
    private Long usuarioId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "microarea_id")
    private Microarea microarea;

    // ----- getters e setters -----

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Microarea getMicroarea() { return microarea; }
    public void setMicroarea(Microarea microarea) { this.microarea = microarea; }
}
