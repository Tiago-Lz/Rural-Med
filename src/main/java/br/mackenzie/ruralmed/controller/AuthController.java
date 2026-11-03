package br.mackenzie.ruralmed.controller;

import br.mackenzie.ruralmed.service.AuthService;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

/** Login e logout (RF01). */
@RestController
@RequestMapping("/api")
public class AuthController {

    public record LoginForm(String login, String senha) {
    }

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/login -> devolve o token e os dados do usuário. */
    @PostMapping("/login")
    public AuthService.Sessao login(@RequestBody LoginForm form) {
        return authService.login(form.login(), form.senha());
    }

    /** POST /api/sair -> encerra a sessão. */
    @PostMapping("/sair")
    public void sair(@RequestHeader(value = "Authorization", required = false) String token) {
        authService.sair(token);
    }

    /** GET /api/health -> o celular do ACS usa para saber se há conexão antes de sincronizar (RN01). */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
