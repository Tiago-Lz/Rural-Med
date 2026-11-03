package br.mackenzie.ruralmed.service;

import br.mackenzie.ruralmed.model.*;
import br.mackenzie.ruralmed.regras.RegraNegocioException;
import br.mackenzie.ruralmed.regras.RegrasDeNegocio;
import br.mackenzie.ruralmed.repository.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Caso de uso "Emitir laudo e receita" (RF06 e RN04). */
@Service
public class LaudoService {

    /** Dados que o médico preenche. A prescrição é opcional. */
    public record LaudoForm(String parecer, String cidCodigo, String prescricao) {
    }

    private final LaudoRepository laudoRepository;
    private final ReceitaRepository receitaRepository;
    private final DossieRepository dossieRepository;
    private final MedicoRepository medicoRepository;
    private final CidRepository cidRepository;

    public LaudoService(LaudoRepository laudoRepository, ReceitaRepository receitaRepository,
                        DossieRepository dossieRepository, MedicoRepository medicoRepository,
                        CidRepository cidRepository) {
        this.laudoRepository = laudoRepository;
        this.receitaRepository = receitaRepository;
        this.dossieRepository = dossieRepository;
        this.medicoRepository = medicoRepository;
        this.cidRepository = cidRepository;
    }

    @Transactional
    public Laudo emitir(Long dossieId, LaudoForm form, AuthService.Sessao sessao) {
        Dossie dossie = dossieRepository.findById(dossieId)
                .orElseThrow(() -> new RegraNegocioException("Dossiê não encontrado"));
        if (dossie.getStatus() != StatusDossie.ENCAMINHADO) {
            throw new RegraNegocioException("Só é possível dar laudo a casos encaminhados");
        }
        if (form.parecer() == null || form.parecer().isBlank()) {
            throw new RegraNegocioException("Escreva o parecer");
        }
        Medico medico = medicoRepository.findById(sessao.usuario().getId()).orElseThrow();
        RegrasDeNegocio.verificarCrmAtivo(medico); // RN04
        Cid cid = cidRepository.findById(form.cidCodigo())
                .orElseThrow(() -> new RegraNegocioException("Escolha um código CID válido"));

        // 1) grava o laudo
        Laudo laudo = new Laudo();
        laudo.setDossie(dossie);
        laudo.setMedico(medico);
        laudo.setCid(cid);
        laudo.setParecer(form.parecer());

        // 2) RN04: assina digitalmente (ICP-Brasil simulada) e só então libera
        laudo.setAssinaturaDigital(assinar(form.parecer(), medico));
        laudo.setStatus(StatusLaudo.EMITIDO);
        laudo.setEmitidoEm(OffsetDateTime.now());
        laudoRepository.save(laudo);

        // 3) grava a receita, também assinada
        if (form.prescricao() != null && !form.prescricao().isBlank()) {
            Receita receita = new Receita();
            receita.setLaudo(laudo);
            receita.setPrescricao(form.prescricao());
            receita.setAssinaturaDigital(assinar(form.prescricao(), medico));
            receita.setAssinadaEm(OffsetDateTime.now());
            receita.setStatus(StatusReceita.ASSINADA);
            receitaRepository.save(receita);
            laudo.getReceitas().add(receita);
        }

        // 4) o caso sai da fila do médico
        dossie.setStatus(StatusDossie.LAUDO_EMITIDO);
        dossieRepository.save(dossie);
        return laudo;
    }

    public List<Laudo> meusLaudos(AuthService.Sessao sessao) {
        return laudoRepository.findByMedicoUsuarioIdOrderByIdDesc(sessao.usuario().getId());
    }

    /**
     * Assinatura digital SIMULADA (como previsto no cronograma): gera o hash SHA-256 do texto
     * com o CRM do médico. A integração real com uma certificadora ICP-Brasil substituiria só este método.
     */
    private String assinar(String texto, Medico medico) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return "ICP-Brasil (simulada) | CRM " + medico.getCrm() + "/" + medico.getUfCrm()
                    + " | " + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
