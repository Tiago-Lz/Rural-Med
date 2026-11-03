package br.mackenzie.ruralmed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RuralMed - telessaúde para triagem em áreas rurais.
 * Rode esta classe e abra http://localhost:8080
 *
 * Organização do código (camadas):
 *   controller  -> recebe as requisições HTTP do navegador (a API REST)
 *   service     -> executa os casos de uso e chama as regras de negócio
 *   regras      -> RegrasDeNegocio.java: todas as regras do sistema
 *   repository  -> conversa com o PostgreSQL (Spring Data JPA)
 *   model       -> as tabelas do banco representadas como classes Java
 */
@SpringBootApplication
public class RuralMedApplication {

    public static void main(String[] args) {
        SpringApplication.run(RuralMedApplication.class, args);
    }
}
