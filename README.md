# IntelliPlan

### Organize sua rotina. Planeje seus objetivos. Acompanhe sua evolução.

O **IntelliPlan** é uma aplicação web de organização pessoal desenvolvida para centralizar o gerenciamento de tarefas, projetos, estudos, compromissos e hábitos em um único ambiente integrado.

A proposta é oferecer uma alternativa estruturada às ferramentas tradicionais de produtividade, permitindo que o usuário organize diferentes áreas da sua rotina sem precisar distribuir suas atividades entre diversas plataformas.

---

## 1. Sobre o Projeto

O IntelliPlan surgiu da proposta de desenvolver uma plataforma completa que auxilie na organização e no acompanhamento da rotina pessoal e acadêmica.

A aplicação reúne recursos de planejamento e gerenciamento em uma arquitetura modular por domínio, combinando uma interface web server-side (Thymeleaf, Tailwind CSS, Lucide Icons) com endpoints REST documentados (OpenAPI / Swagger UI), persistência relacional com PostgreSQL e integração com serviços externos. A interface e a API disponíveis variam por módulo; tarefas, por exemplo, são gerenciadas por rotas MVC/Thymeleaf, não por uma API REST.

---

## 2. Funcionalidades e Módulos

A aplicação está organizada nos seguintes módulos:

| Módulo | Descrição | Status no Backend |
|---|---|---|
| **Dashboard** | Visão geral consolidada: métricas da semana, progresso geral, hábitos recentes, tarefas do dia, projetos ativos e calendário com compromissos e feriados nacionais. | **Concluído e Integrado** |
| **Tarefas** | Criação, consulta, atualização, exclusão e alternância de status de tarefas com prioridades, datas de vencimento e vinculação opcional a projetos. | **Concluído (MVC/Thymeleaf + Service)** |
| **Projetos** | Criação e acompanhamento de projetos com status, prazos e agregação de tarefas associadas. | **Concluído (REST + Service)** |
| **Estudos** | Gerenciamento de disciplinas/matérias e acompanhamento de tópicos/conteúdos de estudo com percentuais de conclusão. | **Concluído (REST + Service)** |
| **Agenda** | Gerenciamento de compromissos com horários de início e término, locais e consulta por períodos. | **Concluído (REST + Service)** |
| **Hábitos** | Cadastro de hábitos com frequências (diária, semanal, mensal), ativação/desativação e registro diário de execução. | **Concluído (REST + Service)** |
| **Feriados** | Sincronização e consulta de feriados nacionais brasileiros via BrasilAPI, integrados à agenda e ao calendário. | **Concluído e Integrado** |
| **Usuários** | Cadastro de contas com hash seguro de senhas via BCrypt e consulta por ID ou e-mail. | **Concluído (REST + Service)** |

---

## 3. Tecnologias e Ferramentas

### Backend
- **Java 21** — linguagem de programação principal.
- **Spring Boot 4.1.2** — framework base para a arquitetura em camadas.
- **Spring Data JPA & Hibernate** — persistência relacional com lazy loading e métodos de consulta derivados.
- **Jakarta Bean Validation** — validação declarativa de dados de entrada via anotações nos records.
- **Spring Security (desenvolvimento)** — disponibilização do bean `PasswordEncoder` (BCrypt); atualmente, todas as rotas estão liberadas e a proteção CSRF está desabilitada. Não há autenticação ativa.
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
├── projeto         # Projeto: Entity, Repository, DTOs, Service e Controller
├── tarefa          # Tarefa: Entity, Repository, DTOs, Service e Controller
└── usuario         # Usuário: Entity, Repository, DTOs, Service (BCrypt) e Controller
```

### Decisões Arquiteturais Relevantes

1. **Sem Lombok:** Código explícito e transparente com getters, setters e construtores desenvolvidos manualmente nas entidades e uso nativo de **Java Records** para DTOs.
2. **Isolamento de Dados por Usuário:** As operações de leitura e mutação dos serviços validam o ID do usuário proprietário. No estado atual, porém, esse ID é um valor de desenvolvimento configurado localmente, não a identidade de uma sessão autenticada.
3. **Resolução de Contexto de Desenvolvimento:** O `UsuarioContextService` obtém o usuário através da propriedade configurável `app.dev.usuario-id` no `application.properties`, validando sua existência no banco. Essa configuração não deve ser tratada como autenticação nem como isolamento seguro entre visitantes.
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

### Passo 3: Configurar as Variáveis de Ambiente
O `application.properties` está preparado para receber as credenciais do banco via variáveis de ambiente. Defina-as no seu terminal:

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
No arquivo `src/main/resources/application.properties`, configure o identificador do usuário para desenvolvimento local:
```properties
app.dev.usuario-id=1
```

> **Dica:** Ao executar a aplicação pela primeira vez com o banco vazio, crie um usuário via `POST /api/v1/usuarios` e configure `app.dev.usuario-id` com o ID correspondente. A configuração padrão é `1`.

> **Atenção — somente desenvolvimento:** o `SecurityConfig` atual permite todas as requisições e desabilita CSRF. O usuário de contexto é escolhido por configuração, não pelo Spring Security; além disso, o endpoint de criação de usuários é público. Não exponha esta configuração à Internet nem a utilize em produção.

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
| **Controllers REST (8 Controllers)** | Concluídos | Endpoints RESTful versionados sob `/api/v1/` com anotações OpenAPI. |
| **Integração BrasilAPI** | Concluída | Client HTTP resiliente com timeouts para feriados nacionais e endpoint `/api/v1/feriados/{ano}`. |
| **Contexto de Usuário Local** | Concluído para desenvolvimento | `UsuarioContextService` valida o ID fixo/configurado em `app.dev.usuario-id`; não representa uma sessão autenticada. |
| **Dashboard com Dados Reais** | Concluído | Agregação completa de métricas, progresso e calendário sem erros em banco vazio. |
| **Segurança Provisória** | Ativa apenas para desenvolvimento | `SecurityConfig` disponibiliza BCrypt, mas permite todas as rotas e desabilita CSRF; não há autenticação nem identidade por sessão. |
| **Autenticação e segurança de produção** | Pendente | Login/logout, identidade autenticada, autorização por usuário e proteção CSRF precisam ser implementados antes de exposição pública. |
| **Interface Web** | Parcial | Dashboard e fluxo de tarefas possuem templates; projetos, estudos, agenda e hábitos ainda não têm as páginas correspondentes implementadas. |

---

## 8. Documentação Complementar

- [Guia Técnico de Arquitetura e Funcionamento](docs/GUIA_TECNICO.md) — Arquitetura, contexto de desenvolvimento, contratos de rotas e ciclo de vida das requisições.
- [Histórico Técnico de Desenvolvimento](docs/HISTORICO_TECNICO.md) — Evolução das fases e decisões técnicas registradas no histórico Git.

---

## 9. Equipe de Desenvolvimento

- **Italo Mazzucatto** — Engenharia de Software e Desenvolvimento Backend / Integração.
- **Laís Campos** — Design de Interface, Estilização e Desenvolvimento Frontend.

---

*IntelliPlan — Planeje com intenção, avance no seu ritmo.*
