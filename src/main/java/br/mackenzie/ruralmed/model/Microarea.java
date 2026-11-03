package br.mackenzie.ruralmed.model;

import jakarta.persistence.*;

/** Território de um ACS dentro da UBS (base da RN02). Tabela: microarea. */
@Entity
@Table(name = "microarea")
public class Microarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ubs_id")
    private Ubs ubs;

    private String nome;

    // ----- getters e setters -----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ubs getUbs() { return ubs; }
    public void setUbs(Ubs ubs) { this.ubs = ubs; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
