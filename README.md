# RuralMed

Sistema de telessaúde para triagem em áreas rurais.
Projeto de Desenvolvimento de Sistemas I, Universidade Presbiteriana Mackenzie.

**Tecnologias:** Java 17 + Spring Boot (backend e API REST), PostgreSQL (banco), HTML/CSS/JavaScript (telas).

---

## Como rodar

1. **Crie o banco.** Escolha uma das duas formas:
   - com Docker: `docker compose up -d`;
   - no pgAdmin: `CREATE USER ruralmed WITH PASSWORD 'ruralmed';` e depois `CREATE DATABASE ruralmed OWNER ruralmed;`.
2. **Abra a pasta** no VS Code (Extension Pack for Java) ou no IntelliJ e rode a classe `RuralMedApplication`.
3. **Acesse** http://localhost:8080.

As tabelas e os dados de teste são criados automaticamente na primeira execução, pelos arquivos da pasta `db/migration`.

> **Já rodou a versão anterior do projeto?** Apague e recrie o banco `ruralmed` antes. No Docker: `docker compose down -v` e depois `docker compose up -d`.

**Contas de teste** (senha `ruralmed123`):

| Perfil | E-mail |
|---|---|
| ACS | acs@ruralmed.dev |
| UBS | ubs@ruralmed.dev |
| Médico cardiologista | cardio@ruralmed.dev |
| Médica dermatologista | dermato@ruralmed.dev |
| Gestor | gestor@ruralmed.dev |

---

## Como o código está organizado

```
src/main/java/br/mackenzie/ruralmed/
├── model/        AS TABELAS          Cada classe é uma tabela do banco (Paciente, Dossie, Laudo...)
├── repository/   ACESSO AO BANCO     Interfaces; o Spring gera o SQL pelo nome do método
├── regras/       REGRAS DE NEGÓCIO   RegrasDeNegocio.java tem todas as regras (RN02 a RN06)
├── service/      CASOS DE USO        Executam cada funcionalidade e chamam as regras
└── controller/   API REST            Recebem as requisições do navegador (GET, POST, PUT, DELETE)

src/main/resources/
├── db/migration/ SQL que cria as tabelas (V1) e insere os dados iniciais (V2, V3)
└── static/       As telas: uma página por perfil (acs.html, ubs.html, medico.html, gestor.html)
```

O caminho de uma requisição, do clique até o banco:

```
Tela (JavaScript) ──HTTP/JSON──► Controller ──► Service ──► RegrasDeNegocio
                                                   │
                                                   ▼
                                              Repository ──SQL──► PostgreSQL
```

---

## Roteiro para a apresentação do código

### 1. Banco de dados → `db/migration/V1__criar_tabelas.sql`
É o modelo físico entregue na semana 12. O **Flyway** executa esse arquivo sozinho quando o sistema sobe, então todos do grupo ficam com o mesmo banco.

### 2. Model → `model/Paciente.java`
Mostre as anotações:
- `@Entity` e `@Table`: esta classe representa a tabela `paciente`;
- `@Id`: chave primária;
- `@ManyToOne`: chave estrangeira para a microárea;
- `@NotBlank`: o nome é obrigatório.

### 3. Repository → `repository/DossieRepository.java`
São interfaces sem código. O Spring lê o nome do método e monta o SQL sozinho. Exemplo, a fila do médico (RN05):
```java
findByStatusAndEspecialidadeIdOrderByEscoreDescEncaminhadoEmAsc(...)
// SELECT ... WHERE status = ? AND especialidade_id = ? ORDER BY escore DESC, encaminhado_em ASC
```

### 4. CRUD → `controller/PacienteController.java` + `service/PacienteService.java`
É o CRUD completo, um verbo HTTP para cada operação:

| Operação | HTTP | Método |
|---|---|---|
| Create | `POST /api/pacientes` | `criar` |
| Read | `GET /api/pacientes` e `GET /api/pacientes/{id}` | `listar` e `buscar` |
| Update | `PUT /api/pacientes/{id}` | `atualizar` |
| Delete | `DELETE /api/pacientes/{id}` | `inativar` |

O Delete é lógico: o paciente é inativado, não apagado, para preservar o histórico. Na tela, o arquivo correspondente é `static/js/pacientes.js`.

### 5. Regras de negócio → `regras/RegrasDeNegocio.java`
É a parte principal da apresentação. Todas as regras estão nesta classe:

| Regra | Método | O que faz |
|---|---|---|
| RN02 | `verificarMicroarea` | O ACS só vê pacientes da microárea dele (LGPD) |
| RN03 | `validarTriagem` | Recusa a triagem se faltar um dado obrigatório |
| RN04 | `verificarCrmAtivo` | Médico sem CRM ativo não entra nem emite laudo |
| RN05 / RF03 | `calcularEscore` | Soma pontos de risco e explica cada um (IA explicável) |
| RN06 | `calcularPrazo` | Caso prioritário tem 24 h; alerta quando faltam menos de 4 h |
| Lei 14.510 | `verificarConsentimento` | Só faz triagem de paciente que consentiu com a telessaúde |

Para mostrar as regras sendo usadas, abra `TriagemService.salvarTriagem`: as regras são chamadas uma depois da outra antes de gravar.

### 6. Offline-first → `static/js/acs.js`
A triagem é guardada no navegador (`localStorage`). Quando a internet volta, a função `sincronizar()` envia tudo de uma vez.

O `idLocal`, gerado no celular, impede que um reenvio duplique a triagem no servidor (RNF02). Antes de enviar, o sistema testa `/api/health` para confirmar a conexão (RN01).

**Para demonstrar:** aperte F12, vá em Network, marque **Offline**, salve uma triagem e depois desmarque o Offline.

### 7. Segurança → `service/AuthService.java`
- A senha é guardada como hash **BCrypt**, nunca em texto.
- O login devolve um **token**.
- Cada endpoint chama `authService.exigir(token, Perfil.UBS)` para conferir quem é o usuário e se ele tem permissão (RF01).

### 8. Testes → `src/test/.../RegrasDeNegocioTest.java`
São testes automáticos das regras de negócio. Rode com **Run Tests** na IDE ou com `mvn test`.

---

## Roteiro da demonstração no sistema

1. **ACS:** fazer uma triagem com o Offline ligado, depois desligar e ver a triagem ser enviada.
2. **ACS:** tentar triar a paciente Antônia, que está bloqueada por não ter consentimento.
3. **UBS:** abrir o caso do João Pedro e encaminhar para Dermatologia.
4. **Cardiologista:** ver o José no topo da fila, com o selo de prazo da RN06. Abrir o caso, buscar "angina" no CID e emitir o laudo.
5. **UBS → Usuários:** cadastrar um médico sem marcar "CRM ativo" e ver o erro da RN04.
6. **Gestor:** ver o relatório de viagens de TFD evitadas e o extrato de repasses.
7. **Qualquer tela:** clicar no botão ◐ para ativar o alto contraste (RNF04).

O arquivo `api.http` traz as chamadas da API para mostrar direto no VS Code ou no IntelliJ.

## Simplificações deste MVP

- **Assinatura digital simulada.** A assinatura ICP-Brasil é simulada: é um hash SHA-256 do documento, como previsto no cronograma. Para a assinatura real, basta trocar o método `assinar` do `LaudoService`.
- **Sessão em memória.** O token fica na memória do servidor. Ao reiniciar o servidor, é preciso fazer login de novo.
- **Mídias fora do MVP.** O banco já tem a tabela `midia`, mas o envio de fotos e áudios fica para a próxima versão.
