package br.mackenzie.ruralmed.regras;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Testes das regras de negócio. Rodar com: mvn test (ou "Run Tests" na IDE). */
class RegrasDeNegocioTest {

    @Test
    void casoLeveTemEscoreBaixoENaoEPrioritario() {
        DadosTriagem leve = new DadosTriagem("coceira no braço", 36.6, 120, 80, 78, 98, 2, List.of());
        RegrasDeNegocio.Escore escore = RegrasDeNegocio.calcularEscore(leve, 40, "mancha vermelha");
        assertEquals(0, escore.pontos());
        assertFalse(escore.prioritario());
    }

    @Test
    void casoGraveEPrioritarioEMostraOsMotivos() {
        DadosTriagem grave = new DadosTriagem("Dor no peito", 37.0, 185, 112, 125, 91, 9, List.of("diabetes"));
        RegrasDeNegocio.Escore escore = RegrasDeNegocio.calcularEscore(grave, 70, "falta de ar ao caminhar");
        assertTrue(escore.prioritario());
        assertTrue(escore.pontos() >= RegrasDeNegocio.LIMITE_PRIORITARIO);
        assertTrue(escore.fatores().contains("relato: falta de ar (+15)"));
    }

    @Test
    void saturacaoCriticaTornaPrioritarioMesmoComPoucosPontos() {
        DadosTriagem d = new DadosTriagem("cansaço", 36.5, 120, 80, 80, 88, 1, List.of());
        assertTrue(RegrasDeNegocio.calcularEscore(d, 30, null).prioritario());
    }

    @Test
    void rn03RecusaTriagemIncompleta() {
        DadosTriagem incompleta = new DadosTriagem("dor de cabeça", null, 120, 80, null, 98, 3, List.of());
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> RegrasDeNegocio.validarTriagem(incompleta));
        assertTrue(erro.getMessage().contains("temperatura"));
    }

    @Test
    void rn06PrazoDeVinteEQuatroHoras() {
        OffsetDateTime entrada = OffsetDateTime.parse("2026-11-03T08:00:00-03:00");
        assertEquals("SEM_PRAZO", RegrasDeNegocio.calcularPrazo(false, entrada, entrada).situacao());
        assertEquals("NO_PRAZO", RegrasDeNegocio.calcularPrazo(true, entrada, entrada.plusHours(10)).situacao());
        assertEquals("ALERTA", RegrasDeNegocio.calcularPrazo(true, entrada, entrada.plusHours(21)).situacao());
        assertEquals("ATRASADO", RegrasDeNegocio.calcularPrazo(true, entrada, entrada.plusHours(25)).situacao());
    }
}
