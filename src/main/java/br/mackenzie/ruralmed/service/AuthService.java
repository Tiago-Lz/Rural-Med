package br.mackenzie.ruralmed.service;

import br.mackenzie.ruralmed.model.Medico;
import br.mackenzie.ruralmed.model.Perfil;
import br.mackenzie.ruralmed.model.Usuario;
import br.mackenzie.ruralmed.regras.AcessoNegadoException;
import br.mackenzie.ruralmed.regras.RegraNegocioException;
import br.mackenzie.ruralmed.regras.RegrasDeNegocio;
import br.mackenzie.ruralmed.repository.AcsRepository;
import br.mackenzie.ruralmed.repository.MedicoRepository;
import br.mackenzie.ruralmed.repository.UsuarioRepository;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * RF01 - Login e controle de acesso por perfil.
 *
 * No login, o sistema confere a senha (guardada como hash BCrypt) e devolve um "token":
 * um código aleatório que o navegador envia em todas as chamadas seguintes.
 * Cada endpoint chama exigir(token, perfis...) para saber quem é o usuário e se ele pode entrar.
 */
@Service
public class AuthService {

    /** Dados devolvidos no login. microareaId só vale para ACS; especialidadeId só para médico. */
    public record Sessao(String token, Usuario usuario, Long microareaId, Long especialidadeId) {
    }

    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    private final UsuarioRepository usuarioRepository;
    private final AcsRepository acsRepository;
    private final MedicoRepository medicoRepository;

    public AuthService(UsuarioRepository usuarioRepository, AcsRepository acsRepository,
                       MedicoRepository medicoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.acsRepository = acsRepository;
        this.medicoRepository = medicoRepository;
    }

    public Sessao login(String login, String senha) {
        // aceita e-mail ou CPF
        Usuario usuario = (login.contains("@")
                ? usuarioRepository.findByEmail(login.trim().toLowerCase())
                : usuarioRepository.findByCpf(login.replaceAll("\\D", "")))
                .orElseThrow(() -> new RegraNegocioException("E-mail/CPF ou senha inválidos"));

        if (!bcrypt.matches(senha, usuario.getSenhaHash())) {
            throw new RegraNegocioException("E-mail/CPF ou senha inválidos");
        }
        if (!usuario.getAtivo()) {
            throw new RegraNegocioException("Usuário inativo");
        }

        Long microareaId = null;
        Long especialidadeId = null;
        if (usuario.getPerfil() == Perfil.ACS) {
            microareaId = acsRepository.findById(usuario.getId()).orElseThrow().getMicroarea().getId();
        }
        if (usuario.getPerfil() == Perfil.MEDICO) {
            Medico medico = medicoRepository.findById(usuario.getId()).orElseThrow();
            RegrasDeNegocio.verificarCrmAtivo(medico); // RN04: sem CRM ativo, não entra
            especialidadeId = medico.getEspecialidade().getId();
        }

        String token = UUID.randomUUID().toString();
        Sessao sessao = new Sessao(token, usuario, microareaId, especialidadeId);
        sessoes.put(token, sessao);
        return sessao;
    }

    public void sair(String token) {
        sessoes.remove(token);
    }

    /**
     * Confere o token e o perfil. Sem perfis informados, basta estar logado.
     * Exemplo: auth.exigir(token, Perfil.UBS) só deixa passar a equipe da UBS.
     */
    public Sessao exigir(String token, Perfil... perfisPermitidos) {
        Sessao sessao = token == null ? null : sessoes.get(token);
        if (sessao == null) {
            throw new AcessoNegadoException("Faça login para continuar");
        }
        if (perfisPermitidos.length > 0 && !Arrays.asList(perfisPermitidos).contains(sessao.usuario().getPerfil())) {
            throw new AcessoNegadoException("Seu perfil não tem acesso a esta funcionalidade");
        }
        return sessao;
    }

    /** Gera o hash BCrypt de uma senha (usado no cadastro de usuários). */
    public String gerarHash(String senha) {
        return bcrypt.encode(senha);
    }
}
