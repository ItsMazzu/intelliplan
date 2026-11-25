# IntelliPlan

### Organize sua rotina. Planeje seus objetivos. Acompanhe sua evolução.

O **IntelliPlan** é uma aplicação web de organização pessoal desenvolvida para centralizar o gerenciamento de tarefas, projetos, estudos, compromissos e hábitos em um único ambiente integrado.

A proposta é oferecer uma alternativa estruturada às ferramentas tradicionais de produtividade, permitindo que o usuário organize diferentes áreas da sua rotina sem precisar distribuir suas atividades entre diversas plataformas.

---

## 1. Sobre o Projeto

O IntelliPlan surgiu da proposta de desenvolver uma plataforma completa que auxilie na organização, no planejamento e no acompanhamento da rotina pessoal e acadêmica.

A aplicação adota uma **arquitetura monolítica modular**, baseada no Spring Boot, com separação de responsabilidades por domínio funcional. Essa abordagem concentra as funcionalidades em uma única unidade de implantação, mantendo os módulos internos organizados, coesos e integrados.

A plataforma combina uma interface web server-side desenvolvida com Thymeleaf, Tailwind CSS e Lucide Icons, uma API RESTful documentada por meio de OpenAPI 3.0 e Swagger UI, persistência de dados em banco relacional PostgreSQL e integração com serviços externos. Essa estrutura permite reunir diferentes recursos de organização e gerenciamento em um ambiente integrado e extensível.

---

## 2. Funcionalidades e Módulos

A aplicação está organizada nos seguintes módulos:

| Módulo | Descrição | Status no Backend |
|---|---|---|
| **Dashboard** | Visão geral consolidada: métricas da semana, progresso geral, hábitos recentes, tarefas do dia, projetos ativos e calendário com compromissos e feriados nacionais. | **Concluído e Integrado** |
| **Tarefas** | Criação, consulta, atualização, exclusão e alternância de status de tarefas com prioridades, datas de vencimento e vinculação opcional a projetos. | **Concluído (REST + Service)** |
| **Projetos** | Criação e acompanhamento de projetos com status, prazos e agregação de tarefas associadas. | **Concluído (REST + Service)** |
| **Estudos** | Gerenciamento de disciplinas/matérias e acompanhamento de tópicos/conteúdos de estudo com percentuais de conclusão. | **Concluído (REST + Service)** |
| **Agenda** | Gerenciamento de compromissos com horários de início e término, locais e consulta por períodos. | **Concluído (REST + Service)** |
| **Hábitos** | Cadastro de hábitos com frequências (diária, semanal, mensal), ativação/desativação e registro diário de execução. | **Concluído (REST + Service)** |
| **Feriados** | Sincronização e consulta de feriados nacionais brasileiros via BrasilAPI, integrados à agenda e ao calendário. | **Concluído e Integrado** |
| **Usuários** | Cadastro de contas com hash seguro de senhas via BCrypt e consulta por ID ou e-mail. | **Concluído (REST + Service)** |
| **Configurações** | Página autenticada `/configuracoes` com dados da conta, encerramento seguro da sessão e informações disponíveis da instalação. | **Concluído (MVC)** |
| **Notificações** | Central persistente por usuário com lembretes de tarefas, compromissos, hábitos e projetos; permite marcar itens como lidos. | **Concluído (REST + interface)** |

---

## 3. Tecnologias e Ferramentas

### Backend
- **Java 21** — linguagem de programação principal.
- **Spring Boot 4.1.2** — framework base para a arquitetura em camadas.
- **Spring Data JPA & Hibernate** — persistência relacional com lazy loading e métodos de consulta derivados.
- **Jakarta Bean Validation** — validação declarativa de dados de entrada via anotações nos records.
- **Spring Security** — autenticação por sessão, BCrypt, CSRF ativo e isolamento pela identidade autenticada.
- **Springdoc OpenAPI 3.2.0** — documentação padronizada e interface interativa Swagger UI.
- **Apache Maven Wrapper (`mvnw`)** — automação de build e gerenciamento de dependências.

### Integrações Externas
- **BrasilAPI** — consulta resiliente de feriados nacionais via `RestClient` do Spring com timeouts de conexão (3s) e leitura (5s), tratando falhas de rede defensivamente.

As respostas não vazias da BrasilAPI são mantidas em cache em memória por ano. Resultados vazios e falhas não são armazenados, permitindo novas tentativas em chamadas posteriores. O cache não possui persistência nem compartilhamento entre instâncias e é perdido quando a aplicação reinicia.

### Frontend
- **Thymeleaf** — engine de templates server-side integrado ao Spring Boot.
- **Tailwind CSS** — estilização e layout responsivo via CDN.
- **Lucide Icons** — biblioteca de ícones vetoriais em SVG.
- **JavaScript (Vanilla)** — manipulação dinâmica de elementos de tela e interatividade.

### Banco de Dados
- **PostgreSQL 15+** — sistema gerenciador de banco de dados relacional.

---

## 4. Arquitetura e Decisões de Design

O projeto adota uma arquitetura em camadas com separação estrita de responsabilidades:

```
src/main/java/com/planner/
├── agenda          # Compromisso: Entity, Repository, DTOs, Service e REST Controller
├── common          # Exceções de domínio, manipulador global (RFC 7807) e UsuarioContextService
├── config          # Configuração de segurança (SecurityConfig)
├── dashboard       # Dashboard: DTOs, Service de agregação e MVC Controller
├── estudos         # Matéria e ConteúdoEstudo: Entities, Repositories, DTOs, Services e Controllers
├── habitos         # Hábito e RegistroHabito: Entities, Repositories, DTOs, Services e Controllers
├── integracao      # BrasilAPI: Client HTTP resiliente, DTO, Service e Controller
├── notificacao     # Notificações persistentes por usuário, reconciliação de eventos e API autenticada
├── projeto         # Projeto: Entity, Repository, DTOs, Service e Controller
├── tarefa          # Tarefa: Entity, Repository, DTOs, Service e Controller
└── usuario         # Usuário: Entity, Repository, DTOs, Service (BCrypt) e Controller
```

### Decisões Arquiteturais Relevantes

1. **Sem Lombok:** Código explícito e transparente com getters, setters e construtores desenvolvidos manualmente nas entidades e uso nativo de **Java Records** para DTOs.
2. **Isolamento de Dados por Usuário:** Todas as operações de leitura e mutação validam o ID do usuário proprietário, impedindo acesso indevido entre contas.
3. **Resolução de Contexto por Autenticação:** O `UsuarioContextService` obtém o usuário pelo e-mail do principal autenticado no Spring Security. O identificador fixo de desenvolvimento não é mais utilizado.
4. **Tratamento Global de Erros (RFC 7807):** Respostas de erro uniformes via `GlobalExceptionHandler` utilizando `ProblemDetail` com campos `type`, `title`, `status`, `detail`, `timestamp` e `erros` (validações).
5. **Dashboard Resiliente:** O `DashboardService` agrega dados de todos os módulos para alimentar a view Thymeleaf e trata cenários de banco de dados vazio com valores zerados seguros, prevenindo qualquer ocorrência de erro HTTP 500.

---

## 5. Instruções para Execução Local

### Pré-requisitos
- **Java Development Kit (JDK) 21** instalado e configurado no `PATH`.
- **PostgreSQL 15+** em execução localmente ou via container Docker.
- **Git** instalado.

### Passo 1: Clonar o Repositório
```bash
git clone https://github.com/Italo-Mazzucatto/IntelliPlan.git
cd IntelliPlan
```

### Passo 2: Criar o Banco de Dados no PostgreSQL
Acesse o console do PostgreSQL (`psql` ou ferramenta gráfica como DBeaver / pgAdmin) e crie o banco:
```sql
CREATE DATABASE intelliplan;
```

### Passo 3: Configurar o Banco
O IntelliPlan procura primeiro um arquivo local fora do projeto:

```
Windows: %USERPROFILE%\.intelliplan\application.properties
Linux/macOS: ~/.intelliplan/application.properties
```

O caminho pode ser sobrescrito pelo administrador com `INTELLIPLAN_CONFIG_FILE`.

Administradores e ambientes técnicos também podem usar variáveis de ambiente:

**No Windows (PowerShell):**
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/intelliplan"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="sua_senha_aqui"
```

**No Linux / macOS (Bash):**
```bash
export DB_URL="jdbc:postgresql://localhost:5432/intelliplan"
export DB_USERNAME="postgres"
export DB_PASSWORD="sua_senha_aqui"
```

### Passo 4: Configurar o Usuário de Desenvolvimento Local
O identificador fixo `app.dev.usuario-id` não é mais usado. Ao concluir a primeira execução, entre em `/setup` enquanto o banco não tiver usuários e crie a conta principal. A configuração da conexão pode ser testada e salva pelo assistente; como o Spring Boot cria o `DataSource` na inicialização, uma reinicialização é necessária antes da criação da conta caso os parâmetros tenham sido alterados.

### Passo 5: Compilar e Executar a Aplicação

**Para compilar e empacotar o artefato WAR:**
```bash
./mvnw clean package -DskipTests
```

**Para iniciar a aplicação diretamente via Maven Wrapper:**
```bash
./mvnw spring-boot:run
```

A aplicação iniciará na porta padrão `8080`.

---

## 6. Acesso aos Canais da Aplicação

### Interface Web (Dashboard)
Abra o navegador no endereço:
```
http://localhost:8080/
```
*(ou `http://localhost:8080/dashboard`)*

O dashboard carregará as métricas da sua rotina, tarefas, projetos, matérias, hábitos e o calendário com compromissos e feriados nacionais.

### Documentação OpenAPI / Swagger UI
Para explorar e testar interativamente todos os endpoints RESTful:
```
http://localhost:8080/swagger-ui/index.html
```

A documentação em formato JSON da especificação OpenAPI 3.0 está acessível em:
```
http://localhost:8080/v3/api-docs
```

### Central de notificações

A central é acessível pelo sino nas páginas da aplicação. Ao abrir o painel, o backend reconcilia lembretes de tarefas vencidas ou com prazo nos próximos três dias, compromissos nas próximas 24 horas, projetos com data de conclusão nos próximos sete dias e hábitos sem registro no ciclo diário, semanal ou mensal.

As notificações ficam persistidas e isoladas pelo usuário autenticado. A chave única por usuário e evento impede duplicidades mesmo se a central for aberta repetidamente ou houver requisições concorrentes.

Endpoints autenticados:

| Método | Rota | Ação |
|---|---|---|
| `GET` | `/api/v1/notificacoes` | Lista as 30 notificações mais recentes e a contagem não lida. |
| `POST` | `/api/v1/notificacoes/sincronizar` | Reconcilia eventos e retorna a lista atualizada. |
| `PATCH` | `/api/v1/notificacoes/{id}/lida` | Marca uma notificação do usuário atual como lida. |
| `PATCH` | `/api/v1/notificacoes/lidas` | Marca todas as notificações do usuário atual como lidas. |

### Garantias do armazenamento local

- O arquivo local fica em `%USERPROFILE%\.intelliplan\application.properties` no Windows ou `~/.intelliplan/application.properties` no Linux/macOS.
- Administradores podem alterar o caminho com `INTELLIPLAN_CONFIG_FILE`.
- Em sistemas compatíveis com POSIX, o arquivo usa permissões exclusivas do proprietário. No Windows, a proteção efetiva depende das permissões da conta e do diretório; o arquivo não é criptografado.
- Senhas do banco e contas são hasheadas apenas no caso da conta principal (BCrypt). A senha do PostgreSQL fica no arquivo local em texto. Não use diretórios ou contas compartilhadas.
- O estado de instalação é persistido no mesmo arquivo com `installation.initialized=true` e também confirmado pela existência da conta no PostgreSQL.

---

## 7. Status do Desenvolvimento

| Componente | Situação | Observações |
|---|---|---|
| **Estrutura Base e Maven** | Concluída | Java 21, Spring Boot 4.1.2 e empacotamento WAR operacional. |
| **Modelagem JPA (8 Entidades)** | Concluída | `Usuario`, `Tarefa`, `Projeto`, `Materia`, `ConteudoEstudo`, `Compromisso`, `Habito`, `RegistroHabito`. |
| **Repositories (8 Repositories)** | Concluídos | Interfaces Spring Data JPA com consultas derivadas e filtros por usuário. |
| **DTOs e Validações** | Concluídos | Records Java com validações Jakarta (`@NotBlank`, `@Size`, etc.). |
| **Camada de Services (8 Services)** | Concluída | Regras de negócio, transações (`@Transactional`) e isolamento estrito de dados. |
| **Tratamento de Exceções** | Concluído | `GlobalExceptionHandler` mapeando exceções de domínio e validação para RFC 7807 (`ProblemDetail`). |
| **Controllers REST (9 Controllers)** | Concluídos | Endpoints RESTful versionados sob `/api/v1/` com anotações OpenAPI. |
| **Integração BrasilAPI** | Concluída | Client HTTP resiliente com timeouts para feriados nacionais e endpoint `/api/v1/feriados/{ano}`. |
| **Contexto de Usuário** | Concluído | `UsuarioContextService` resolve o usuário pelo principal autenticado. |
| **Dashboard com Dados Reais** | Concluído | Agregação completa de métricas, progresso e calendário sem erros em banco vazio. |
| **Segurança da Aplicação** | Concluída | Login por sessão, BCrypt, CSRF ativo, logout seguro e restrição das APIs e páginas por autenticação. |
| **Assistente de Primeira Execução** | Parcial | Teste e persistência da configuração PostgreSQL e criação da conta principal estão implementados; o assistente depende de o contexto Spring conseguir iniciar com uma conexão utilizável. |
| **Telas Frontend dos Demais Módulos** | Concluídas | Implementação das interfaces visuais completas de cadastro e listagem para tarefas, projetos, estudos, agenda e hábitos. |

---

## 8. Documentação Complementar

- [Guia Técnico de Arquitetura e Funcionamento](file:///c:/Users/italo/OneDrive/Desktop/Studies%20-%20Academics/Coding/Backend/Java/IntelliPlan/docs/GUIA_TECNICO.md) — Documentação exaustiva com diagramas de sequência, contratos de rotas, ciclo de vida de requisição e detalhamento de pacotes.
- [Histórico Técnico de Desenvolvimento](file:///c:/Users/italo/OneDrive/Desktop/Studies%20-%20Academics/Coding/Backend/Java/IntelliPlan/docs/HISTORICO_TECNICO.md) — Análise detalhada dos commits Git, evolução das fases e problemas técnicos superados.

---

## 9. Equipe de Desenvolvimento

- **Italo Mazzucatto** — Engenharia de Software e Desenvolvimento Backend / Integração.
- **Laís Campos** — Design de Interface, Estilização e Desenvolvimento Frontend.

---

*IntelliPlan — Planeje com intenção, avance no seu ritmo.*
