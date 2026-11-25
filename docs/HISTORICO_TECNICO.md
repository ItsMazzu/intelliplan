# Histórico Técnico de Desenvolvimento e Decisões — IntelliPlan

Este documento consolida a evolução cronológica do projeto **IntelliPlan — Organizador Pessoal Inteligente**, mapeando os commits do repositório Git, as etapas cumpridas, os desafios técnicos enfrentados e as soluções arquiteturais adotadas.

> Este é um registro histórico, não uma especificação do estado atual. A tabela abaixo documenta commits até 24/11/2026 e não pretende cobrir alterações posteriores. Para arquitetura, rotas e comportamento vigentes, consulte o [Guia Técnico](GUIA_TECNICO.md) e o [README](../README.md).

---

## 1. Rastreabilidade dos Commits no Repositório Git

A tabela abaixo correlaciona os registros do histórico de versionamento às fases de implementação:

| Hash | Autor | Data | Mensagem do Commit | Escopo / Fase |
|---|---|---|---|---|
| `77d7c3f` | Italo Mazzucatto | 2026-11-16 | `Initial commit` | Inicialização do repositório no GitHub. |
| `edceb5f` | Italo Mazzucatto | 2026-11-16 | `chore: initialize IntelliPlan project structure` | Criação do esqueleto base do projeto com Maven Wrapper (`mvnw`), `pom.xml` e árvore de diretórios padrão Spring Boot. |
| `6940387` | Italo Mazzucatto | 2026-11-16 | `chore: configure initial project architecture` | Estabelecimento das diretrizes no `AGENTS.md`, modularização inicial de pacotes e definições arquiteturais. |
| `981351a` | Italo Mazzucatto | 2026-11-16 | `Merge remote-tracking branch 'origin/main'` | Sincronização entre ramos do repositório remoto. |
| `aa0c346` | Italo Mazzucatto | 2026-11-16 | `chore: exclude generated build files` | Atualização das regras do `.gitignore` para ignorar artefatos gerados pelo Maven no diretório `target/`. |
| `66237ef` | Italo Mazzucatto | 2026-11-16 | `chore: remove duplicated project directory` | Limpeza estrutural e eliminação de diretórios redundantes no workspace. |
| `3e3ab3c` | Italo Mazzucatto | 2026-11-17 | `feat: add JPA entities and repository interfaces` | Implementação das 8 entidades de domínio (`Usuario`, `Projeto`, `Tarefa`, `Materia`, `ConteudoEstudo`, `Compromisso`, `Habito`, `RegistroHabito`) e seus 8 repositories Spring Data JPA. |
| `0be4aae` | Italo Mazzucatto | 2026-11-18 | `feat: implement backend service layer and business logic` | Implementação inicial dos primeiros Services (`UsuarioService`, `ProjetoService`), com hash de senhas e DTOs Records. |
| `464a96d` | Italo Mazzucatto | 2026-11-18 | `feat: implement backend service layer and business logic` | Conclusão dos 8 Services da aplicação (`TarefaService`, `MateriaService`, `ConteudoEstudoService`, `CompromissoService`, `HabitoService`, `RegistroHabitoService`). |
| `3cf9d26` | Italo Mazzucatto | 2026-11-18 | `docs: update project development status` | Atualização do status de desenvolvimento no `README.md`. |
| `02c56b8` | Laís Campos | 2026-11-18 | `Add files via upload` | Inclusão de recursos visuais e ativos de design do frontend. |
| `591a43f` | Laís Campos | 2026-11-18 | `Add files via upload` | Adição de arquivos estáticos de estilização CSS. |
| `7094ce5` | Italo Mazzucatto | 2026-11-18 | `refactor: organize frontend resources structure` | Reorganização de arquivos estáticos em pastas estruturadas (`static/css/base`, `static/css/pages`, `static/js/pages`). |
| `ff46928` | Italo Mazzucatto | 2026-11-18 | `feat: implement initial dashboard frontend` | Criação do template `dashboard/dashboard.html` utilizando Thymeleaf, Tailwind CSS e ícones Lucide. |
| `227f80d` | Laís Campos | 2026-11-18 | `Refactor dashboard.css for style consistency and responsiveness` | Ajustes de estilo, paleta de cores e responsividade para dispositivos móveis e desktop. |
| `8e1057d` | Laís Campos | 2026-11-18 | `Add files via upload` | Inclusão de ilustrações estáticas para enriquecimento do dashboard. |
| `e9edde5` | Laís Campos | 2026-11-18 | `Refactor HTML comments and text colors in dashboard` | Padronização dos comentários no HTML e refinamento de contraste de tipografia. |
| `50f614d` | Italo Mazzucatto | 2026-11-18 | `feat: add initial dashboard setup and frontend integration` | Criação do `DashboardController` mapeando a rota `/` e configuração de desenvolvimento do Spring Security (`SecurityConfig` com BCrypt e `permitAll()`). |
| `7c87662` | Italo Mazzucatto | 2026-11-18 | `docs: update README with dashboard implementation status` | Atualização do `README.md` refletindo a disponibilidade visual do dashboard. |
| `6df0016` | Italo Mazzucatto | 2026-11-22 | `feat: add REST controllers and global exception handling` | Implementação de todos os 8 Controllers REST sob `/api/v1`, anotações OpenAPI/Swagger e manipulador global `GlobalExceptionHandler` (RFC 7807). |
| `25eb2e5` | Italo Mazzucatto | 2026-11-22 | `fix: stabilize backend queries and integrations` | Estabilização das consultas e integrações do backend. |
| `b359fe5` | Italo Mazzucatto | 2026-11-22 | `chore: checkpoint backend implementation` | Registro de checkpoint da implementação do backend. |
| `8ce4634` | Italo Mazzucatto | 2026-11-22 | `merge: integrate main into backend stabilization` | Integração das alterações de `main` na estabilização do backend. |
| `263ca60` | Italo Mazzucatto | 2026-11-22 | `Merge backend stabilization into main` | Integração da estabilização do backend em `main`. |
| `8ba28c3` | Laís Campos | 2026-11-24 | `Refactor TarefaController to TarefaViewController` | Separação do fluxo de tarefas em controller MVC para páginas Thymeleaf; o módulo não possui controller REST no estado atual. |
| `a784b4f` | Laís Campos | 2026-11-24 | `Create tarefas.html template for task management` | Inclusão da página Thymeleaf para listagem e gerenciamento de tarefas. |
| `9746802` | Laís Campos | 2026-11-24 | `Update tarefas.html` | Ajustes na página de tarefas. |
| `9ebd6ec` | Laís Campos | 2026-11-24 | `Create task form in formulario.html` | Inclusão do formulário Thymeleaf de criação e edição de tarefas. |
| `00de9ff` | Laís Campos | 2026-11-24 | `Refactor sidebar structure and navigation links` | Refatoração estrutural da navegação lateral da interface. |
| `7ae89bd` | Laís Campos | 2026-11-24 | `Add referer header handling to task completion` | Retorno à página de origem, quando disponível, após alternar a conclusão de uma tarefa. |

---

## 2. Fases de Desenvolvimento e Decisões de Arquitetura

### Fase 1: Fundação do Projeto e Diretrizes do AGENTS.md
- **Objetivo:** Estabelecer uma base sólida, sem abstrações prematuras ou dependências invasivas.
- **Decisões:**
  - Proibição estrita do uso de Lombok para evitar dependências de anotações que mascaram detalhes de compilação e dificultam manutenção explícita.
  - Adoção de injeção de dependência via construtor com campos `private final`.
  - Proibição de comentários inline no código-fonte para priorizar código autoexplicativo e limpo.
  - Empacotamento configurado como WAR para permitir implantação em containers Servlet como Tomcat.

### Fase 2: Persistência Relacional e Integridade de Dados
- **Objetivo:** Mapear o domínio do IntelliPlan com JPA/Hibernate garantindo integridade referencial.
- **Decisões:**
  - Uso estrito de `FetchType.LAZY` em todas as relações `@ManyToOne` para evitar problemas de desempenho (N+1 queries).
  - Criação de chaves estrangeiras explícitas (`usuario_id`, `projeto_id`, `materia_id`, `habito_id`).
  - Criação de constraints de unicidade essenciais no banco:
    - `uk_usuarios_email`: unicidade de endereço de e-mail por conta.
    - `uk_registro_habito_data`: unicidade composta entre hábito e data de registro, impedindo duplo registro no mesmo dia.

### Fase 3: Camada de Serviços e Isolamento por Usuário
- **Objetivo:** Concentrar a lógica de negócio na camada de Service, protegendo o acesso aos dados.
- **Decisões:**
  - Uso sistemático de DTOs (`Records`) nas fronteiras de entrada e saída, nunca expondo entidades JPA.
  - Verificação de propriedade do registro em cada operação: métodos como `buscarTarefaDoUsuario`, `buscarProjetoDoUsuario` e `buscarCompromissoDoUsuario` garantem que um usuário não consiga acessar ou modificar entidades pertencentes a outro usuário.
  - Uso criterioso de anotações `@Transactional(readOnly = true)` em leituras e `@Transactional` em mutações.

### Fase 4: Construção da Interface Visual (Dashboard)
- **Objetivo:** Criar um painel moderno e intuitivo para visualização das tarefas, métricas e compromissos.
- **Decisões:**
  - Template Thymeleaf modular em `src/main/resources/templates/dashboard/dashboard.html`.
  - Uso de Tailwind CSS via CDN para flexibilidade e rápida prototipação de layout.
  - Incorporação dos Lucide Icons para representação gráfica de seções.
  - Estruturação de recursos estáticos CSS e JS separados em `static/css` e `static/js`.

### Fase 5: Exposição de APIs RESTful e Tratamento de Exceções
- **Objetivo:** Disponibilizar os recursos do backend via endpoints HTTP documentados.
- **Decisões:**
  - Padronização do prefixo `/api/v1/` para versionamento de rotas.
  - Integração com Springdoc OpenAPI 3.2.0 (`/swagger-ui/index.html` e `/v3/api-docs`).
  - Criação de `GlobalExceptionHandler` utilizando a especificação RFC 7807 (`ProblemDetail`), provendo respostas uniformes com `title`, `status`, `detail`, `type` e `timestamp`.

### Fase 6: Finalização do Backend e Integrações Reais
- **Objetivo:** Auditar, corrigir inconsistências, integrar serviços externos e conectar dados reais à interface.
- **Ações Implementadas:**
  1. **Substituição da Estratégia de Contexto de Usuário (registro da fase):**
     - O mecanismo anterior utilizava leitura frágil de cabeçalho `X-Usuario-Id` e busca arbitrária pelo primeiro usuário do banco.
     - Foi substituído pela resolução do usuário autenticado pelo e-mail do principal do Spring Security.
     - O `UsuarioContextService` consulta o usuário correspondente no repositório e não depende de identificadores fixos.
     - O cadastro público por `POST /api/v1/usuarios` foi removido. No estado documentado pela fase, a conta inicial era criada pelo assistente de instalação; a política posterior de criação de contas está descrita no [Guia Técnico](GUIA_TECNICO.md).
  2. **Integração com a BrasilAPI:**
     - Criação do pacote `com.planner.integracao.feriados`.
     - Implementação de `BrasilApiClient` usando `RestClient` nativo do Spring com timeouts de 3s (conexão) e 5s (leitura).
     - Implementação de `FeriadoService` com tratamento de falhas de rede, registrando logs e mantendo a aplicação disponível. A política atual de consolidação parcial e cache está descrita no [Guia Técnico](GUIA_TECNICO.md).
     - Exposição do endpoint `GET /api/v1/feriados/{ano}` com documentação OpenAPI.
  3. **Conexão Real do Dashboard (`DashboardService`):**
     - O `DashboardController` deixou de ser um simples visualizador estático e passou a delegar ao `DashboardService`.
     - Foram populados os atributos demandados pelo Thymeleaf naquela versão (`usuarioNome`, `primeiroNome`, contadores, listas e métricas de progresso). A composição atual do model está descrita no [Guia Técnico](GUIA_TECNICO.md).
     - Integração do calendário mensal com os feriados nacionais obtidos da BrasilAPI e os compromissos do usuário.
     - Implementação de tratamento defensivo: em caso de banco de dados vazio ou usuário sem dados cadastrados, a aplicação exibe a tela com valores zerados e mensagens amigáveis sem lançar erro 500.
     - Adição do endpoint MVC `POST /tarefas/{id}/concluir` para suporte à alternância de status diretamente na tela. A rota REST de tarefas foi removida posteriormente quando o módulo passou a usar controller MVC.

### Fase 7: Interface MVC para Tarefas e registro da segurança naquele estágio
- **Objetivo:** Disponibilizar o gerenciamento de tarefas na interface Thymeleaf.
- **Ações registradas no histórico Git:**
  - Substituição do controller REST de tarefas por `TarefaViewController`, com listagem, filtros, criação, edição e exclusão via MVC.
  - Inclusão dos templates de listagem e formulário, além de ajustes na navegação lateral.
  - Retorno à página de referência após alternar o status de conclusão.
- **Registro histórico de segurança:** esta fase corresponde ao estágio retratado pelos commits listados nesta seção. A menção a `app.dev.usuario-id`, `permitAll()` e CSRF desabilitado não descreve a configuração atual; consulte a seção de autenticação e autorização do [Guia Técnico](GUIA_TECNICO.md).

---

## 3. Desafios Técnicos Enfrentados e Soluções

1. **Bloqueio de Arquivos no `.m2/repository/.locks` no Windows:**
   - *Sintoma:* Tentativas de execução do Maven Wrapper em sandbox restrito falhavam com `IllegalStateException: Could not open file channel for ... NamedLockKey ... lock`.
   - *Solução:* Execução das etapas de compilação e empacotamento Maven fora do sandbox restrito, permitindo que os canais de lock do repositório local do Maven funcionassem normalmente.

2. **Refinamento no Tratamento de Violação de Integridade:**
   - *Sintoma:* Inicialmente, qualquer `DataIntegrityViolationException` era convertida genericamente em `DuplicateResourceException`.
   - *Solução:* Foi revisado o tratamento em `UsuarioService` e `RegistroHabitoService` para inspecionar a causa raiz e converter apenas violações de constraints de unicidade conhecidas (`uk_usuarios_email` e `uk_registro_habito_data`), deixando outras violações (como integridade referencial ou campos nulos) serem tratadas com suas causas reais.

3. **Renderização Segura do Thymeleaf com Banco Vazio:**
   - *Sintoma:* O template `dashboard.html` utiliza operações de iteração (`th:each`) e propriedades aninhadas que causariam exceções de ponteiro nulo (NPE) caso variáveis retornassem nulas.
   - *Solução:* O `DashboardService` constrói instâncias consistentes com listas vazias, listas de dias do mês atual e percentuais zerados (`"0%"`), assegurando renderização 100% estável independentemente do estado do banco.
