-- RuralMed: modelo físico do banco de dados (PostgreSQL 16)
-- Migração inicial (Flyway). Mesmo modelo físico entregue na semana 12.


CREATE TYPE perfil_usuario AS ENUM ('ACS', 'UBS', 'MEDICO', 'GESTOR');
CREATE TYPE status_dossie AS ENUM ('SINCRONIZADO', 'AGUARDANDO_ENCAMINHAMENTO', 'ENCAMINHADO', 'LAUDO_EMITIDO');
CREATE TYPE tipo_midia AS ENUM ('FOTO', 'AUDIO');
CREATE TYPE status_laudo AS ENUM ('RASCUNHO', 'AGUARDANDO_ASSINATURA', 'EMITIDO');
CREATE TYPE status_receita AS ENUM ('AGUARDANDO_ASSINATURA', 'ASSINADA');

CREATE TABLE municipio (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    nome VARCHAR(120) NOT NULL,
    uf CHAR(2) NOT NULL,
    custo_medio_viagem_tfd NUMERIC(10,2) NOT NULL,
    CONSTRAINT pk_municipio PRIMARY KEY (id),
    CONSTRAINT ck_municipio_uf CHECK (uf ~ '^[A-Z]{2}$'),
    CONSTRAINT ck_municipio_custo CHECK (custo_medio_viagem_tfd >= 0),
    CONSTRAINT uq_municipio UNIQUE (nome, uf)
);
COMMENT ON TABLE municipio IS 'Município contratante; guarda o custo médio da viagem de TFD usado no RF05.';

CREATE TABLE ubs (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    municipio_id BIGINT NOT NULL,
    nome VARCHAR(150) NOT NULL,
    cnes CHAR(7) NOT NULL UNIQUE,
    CONSTRAINT pk_ubs PRIMARY KEY (id),
    CONSTRAINT fk_ubs_municipio FOREIGN KEY (municipio_id) REFERENCES municipio(id),
    CONSTRAINT ck_ubs_cnes CHECK (cnes ~ '^[0-9]{7}$')
);
COMMENT ON TABLE ubs IS 'Unidade Básica de Saúde do município, responsável pelas microáreas.';

CREATE TABLE microarea (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    ubs_id BIGINT NOT NULL,
    nome VARCHAR(80) NOT NULL,
    CONSTRAINT pk_microarea PRIMARY KEY (id),
    CONSTRAINT fk_microarea_ubs FOREIGN KEY (ubs_id) REFERENCES ubs(id),
    CONSTRAINT uq_microarea UNIQUE (ubs_id, nome)
);
COMMENT ON TABLE microarea IS 'Território de atuação de um ACS; base do isolamento de dados da RN02.';

CREATE TABLE especialidade (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    nome VARCHAR(80) NOT NULL UNIQUE,
    CONSTRAINT pk_especialidade PRIMARY KEY (id)
);
COMMENT ON TABLE especialidade IS 'Especialidades médicas disponíveis para encaminhamento (RF02).';

CREATE TABLE clinica_parceira (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    municipio_id BIGINT NOT NULL,
    nome VARCHAR(150) NOT NULL,
    cnpj CHAR(14) NOT NULL UNIQUE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_clinica_parceira PRIMARY KEY (id),
    CONSTRAINT fk_clinica_municipio FOREIGN KEY (municipio_id) REFERENCES municipio(id),
    CONSTRAINT ck_clinica_cnpj CHECK (cnpj ~ '^[0-9]{14}$')
);
COMMENT ON TABLE clinica_parceira IS 'Clínica regional que recebe dossiês e agrupa médicos para o repasse (RF07).';

CREATE TABLE usuario (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    cpf CHAR(11) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil perfil_usuario NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT ck_usuario_cpf CHECK (cpf ~ '^[0-9]{11}$')
);
COMMENT ON TABLE usuario IS 'Credenciais e perfil de acesso de todos os usuários (RF01).';

CREATE TABLE acs (
    usuario_id BIGINT NOT NULL,
    microarea_id BIGINT NOT NULL,
    CONSTRAINT pk_acs PRIMARY KEY (usuario_id),
    CONSTRAINT fk_acs_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_acs_microarea FOREIGN KEY (microarea_id) REFERENCES microarea(id)
);
COMMENT ON TABLE acs IS 'Especialização de USUARIO para o Agente Comunitário de Saúde.';

CREATE TABLE medico (
    usuario_id BIGINT NOT NULL,
    especialidade_id BIGINT NOT NULL,
    clinica_id BIGINT,
    crm VARCHAR(10) NOT NULL,
    uf_crm CHAR(2) NOT NULL,
    crm_ativo BOOLEAN NOT NULL DEFAULT FALSE,
    valor_por_laudo NUMERIC(10,2) NOT NULL,
    CONSTRAINT pk_medico PRIMARY KEY (usuario_id),
    CONSTRAINT fk_medico_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_medico_especialidade FOREIGN KEY (especialidade_id) REFERENCES especialidade(id),
    CONSTRAINT fk_medico_clinica FOREIGN KEY (clinica_id) REFERENCES clinica_parceira(id),
    CONSTRAINT uq_medico_crm UNIQUE (crm, uf_crm),
    CONSTRAINT ck_medico_valor CHECK (valor_por_laudo >= 0)
);
COMMENT ON TABLE medico IS 'Especialização de USUARIO para o médico especialista.';

CREATE TABLE paciente (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    microarea_id BIGINT NOT NULL,
    nome VARCHAR(150) NOT NULL,
    cpf CHAR(11) UNIQUE,
    cns CHAR(15) UNIQUE,
    data_nascimento DATE NOT NULL,
    telefone VARCHAR(20),
    consentimento_em TIMESTAMPTZ,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_paciente PRIMARY KEY (id),
    CONSTRAINT fk_paciente_microarea FOREIGN KEY (microarea_id) REFERENCES microarea(id),
    CONSTRAINT ck_paciente_documento CHECK (cpf IS NOT NULL OR cns IS NOT NULL)
);
COMMENT ON TABLE paciente IS 'Cadastro do paciente atendido pelo ACS na microárea.';

CREATE TABLE dossie (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    id_local UUID NOT NULL UNIQUE,
    paciente_id BIGINT NOT NULL,
    acs_id BIGINT NOT NULL,
    especialidade_id BIGINT,
    medico_id BIGINT,
    clinica_id BIGINT,
    dados_triagem JSONB NOT NULL,
    relato TEXT,
    escore NUMERIC(6,2),
    prioritario BOOLEAN NOT NULL DEFAULT FALSE,
    fatores_escore JSONB,
    status status_dossie NOT NULL DEFAULT 'SINCRONIZADO',
    coletado_em TIMESTAMPTZ NOT NULL,
    sincronizado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    encaminhado_em TIMESTAMPTZ,
    CONSTRAINT pk_dossie PRIMARY KEY (id),
    CONSTRAINT fk_dossie_paciente FOREIGN KEY (paciente_id) REFERENCES paciente(id),
    CONSTRAINT fk_dossie_acs FOREIGN KEY (acs_id) REFERENCES acs(usuario_id),
    CONSTRAINT fk_dossie_especialidade FOREIGN KEY (especialidade_id) REFERENCES especialidade(id),
    CONSTRAINT fk_dossie_medico FOREIGN KEY (medico_id) REFERENCES medico(usuario_id),
    CONSTRAINT fk_dossie_clinica FOREIGN KEY (clinica_id) REFERENCES clinica_parceira(id),
    CONSTRAINT ck_dossie_escore CHECK (escore IS NULL OR escore >= 0),
    CONSTRAINT ck_dossie_encaminhado CHECK (status NOT IN ('ENCAMINHADO','LAUDO_EMITIDO') OR (especialidade_id IS NOT NULL AND encaminhado_em IS NOT NULL))
);
COMMENT ON TABLE dossie IS 'Triagem coletada pelo ACS e todo o seu ciclo até o laudo; entidade central do sistema.';

CREATE TABLE midia (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    dossie_id BIGINT NOT NULL,
    tipo tipo_midia NOT NULL,
    chave_objeto VARCHAR(255) NOT NULL UNIQUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_midia PRIMARY KEY (id),
    CONSTRAINT fk_midia_dossie FOREIGN KEY (dossie_id) REFERENCES dossie(id) ON DELETE CASCADE
);
COMMENT ON TABLE midia IS 'Fotos e áudios anexados à triagem; o arquivo fica no armazenamento de objetos.';

CREATE TABLE cid (
    codigo VARCHAR(10) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    versao VARCHAR(6) NOT NULL,
    CONSTRAINT pk_cid PRIMARY KEY (codigo),
    CONSTRAINT ck_cid_versao CHECK (versao IN ('CID-10','CID-11'))
);
COMMENT ON TABLE cid IS 'Tabela de referência da Classificação Internacional de Doenças (RF06).';

CREATE TABLE laudo (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    dossie_id BIGINT NOT NULL UNIQUE,
    medico_id BIGINT NOT NULL,
    cid_codigo VARCHAR(10) NOT NULL,
    parecer TEXT NOT NULL,
    pdf_chave VARCHAR(255),
    assinatura_digital TEXT,
    status status_laudo NOT NULL DEFAULT 'RASCUNHO',
    emitido_em TIMESTAMPTZ,
    CONSTRAINT pk_laudo PRIMARY KEY (id),
    CONSTRAINT fk_laudo_dossie FOREIGN KEY (dossie_id) REFERENCES dossie(id),
    CONSTRAINT fk_laudo_medico FOREIGN KEY (medico_id) REFERENCES medico(usuario_id),
    CONSTRAINT fk_laudo_cid FOREIGN KEY (cid_codigo) REFERENCES cid(codigo),
    CONSTRAINT ck_laudo_emitido CHECK (status <> 'EMITIDO' OR (assinatura_digital IS NOT NULL AND emitido_em IS NOT NULL))
);
COMMENT ON TABLE laudo IS 'Parecer do especialista sobre o dossiê; no máximo um por dossiê.';

CREATE TABLE receita (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    laudo_id BIGINT NOT NULL,
    prescricao TEXT NOT NULL,
    pdf_chave VARCHAR(255),
    assinatura_digital TEXT,
    assinada_em TIMESTAMPTZ,
    status status_receita NOT NULL DEFAULT 'AGUARDANDO_ASSINATURA',
    CONSTRAINT pk_receita PRIMARY KEY (id),
    CONSTRAINT fk_receita_laudo FOREIGN KEY (laudo_id) REFERENCES laudo(id),
    CONSTRAINT ck_receita_assinada CHECK (status <> 'ASSINADA' OR (assinatura_digital IS NOT NULL AND assinada_em IS NOT NULL))
);
COMMENT ON TABLE receita IS 'Prescrição vinculada ao laudo, liberada somente com assinatura válida.';

CREATE INDEX ix_ubs_municipio ON ubs (municipio_id);
CREATE INDEX ix_paciente_microarea ON paciente (microarea_id) WHERE ativo;
CREATE INDEX ix_acs_microarea ON acs (microarea_id);
CREATE INDEX ix_medico_especialidade ON medico (especialidade_id) WHERE crm_ativo;
-- Fila do médico (RF03/RN05): casos encaminhados por escore decrescente e ordem de chegada
CREATE INDEX ix_dossie_fila ON dossie (especialidade_id, escore DESC, encaminhado_em) WHERE status = 'ENCAMINHADO';
-- Linha do tempo do paciente (RF08)
CREATE INDEX ix_dossie_paciente ON dossie (paciente_id, coletado_em);
CREATE INDEX ix_midia_dossie ON midia (dossie_id);
-- Busca de CID por prefixo de descrição (RF06)
CREATE INDEX ix_cid_descricao ON cid (lower(descricao) varchar_pattern_ops);
-- Extrato mensal de repasses (RF07)
CREATE INDEX ix_laudo_extrato ON laudo (medico_id, emitido_em) WHERE status = 'EMITIDO';
CREATE INDEX ix_receita_laudo ON receita (laudo_id);

