package br.mackenzie.ruralmed.regras;

import java.util.List;

/** Campos do formulário de triagem preenchido pelo ACS. */
public record DadosTriagem(
        String queixaPrincipal,
        Double temperatura,
        Integer pressaoSistolica,
        Integer pressaoDiastolica,
        Integer frequenciaCardiaca,
        Integer saturacaoO2,
        Integer dor,
        List<String> comorbidades) {
}
