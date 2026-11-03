package br.mackenzie.ruralmed.controller;

import br.mackenzie.ruralmed.model.Perfil;
import br.mackenzie.ruralmed.service.AuthService;
import br.mackenzie.ruralmed.service.RelatorioService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/** Gestor municipal: relatório de atendimentos (RF05) e extrato de repasses (RF07). */
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final AuthService authService;
    private final RelatorioService relatorioService;

    public RelatorioController(AuthService authService, RelatorioService relatorioService) {
        this.authService = authService;
        this.relatorioService = relatorioService;
    }

    /** Exemplo: GET /api/relatorios/atendimentos?inicio=2026-11-01&fim=2026-11-30 */
    @GetMapping("/atendimentos")
    public RelatorioService.Relatorio atendimentos(@RequestParam LocalDate inicio, @RequestParam LocalDate fim,
                                                   @RequestHeader(value = "Authorization", required = false) String token) {
        authService.exigir(token, Perfil.GESTOR, Perfil.UBS);
        return relatorioService.atendimentos(inicio, fim);
    }

    /** Exemplo: GET /api/relatorios/repasses?ano=2026&mes=11 */
    @GetMapping("/repasses")
    public List<RelatorioService.LinhaExtrato> repasses(@RequestParam int ano, @RequestParam int mes,
                                                        @RequestHeader(value = "Authorization", required = false) String token) {
        authService.exigir(token, Perfil.GESTOR, Perfil.UBS);
        return relatorioService.repasses(ano, mes);
    }
}
