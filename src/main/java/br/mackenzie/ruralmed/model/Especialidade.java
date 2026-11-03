package br.mackenzie.ruralmed.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/** Especialidade médica (Cardiologia, Dermatologia...). Tabela: especialidade. */
@Entity
@Table(name = "especialidade")
public class Especialidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o nome")
    private String nome;

    // ----- getters e setters -----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
