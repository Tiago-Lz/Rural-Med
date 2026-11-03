package br.mackenzie.ruralmed.model;

import jakarta.persistence.*;

/** Tabela de códigos de doença (CID-10/11). Tabela: cid. */
@Entity
@Table(name = "cid")
public class Cid {

    @Id
    private String codigo;

    private String descricao;

    private String versao;

    // ----- getters e setters -----

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getVersao() { return versao; }
    public void setVersao(String versao) { this.versao = versao; }
}
