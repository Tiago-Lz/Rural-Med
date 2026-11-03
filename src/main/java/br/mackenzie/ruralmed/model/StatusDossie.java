package br.mackenzie.ruralmed.model;

/** Etapas do dossiê: recebido, aguardando, encaminhado ao médico, com laudo. */
public enum StatusDossie {
    SINCRONIZADO, AGUARDANDO_ENCAMINHAMENTO, ENCAMINHADO, LAUDO_EMITIDO
}
