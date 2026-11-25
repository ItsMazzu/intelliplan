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

| Módulo | Implementação atual |
|---|---|---|
| **Dashboard** | **Implementada.** Métricas do usuário, tarefas, projetos, estudos, hábitos e calendário de compromissos/feriados. |
| **Tarefas** | **Implementada.** CRUD e filtros pela interface MVC/Thymeleaf; não há controller REST de tarefas neste checkout. |
| **Projetos** | **Implementada.** Interface MVC e API REST; estados, datas e vínculo/progresso das tarefas. |
| **Estudos** | **Implementada.** Matérias e conteúdos com estados e progresso, por interface MVC e API REST. |
| **Agenda** | **Implementada.** Interface MVC e API REST para compromissos, período, horários e local. |
| **Hábitos** | **Implementada.** Hábitos, ativação, frequência e registros, por interface MVC e API REST. |
| **Feriados** | **Parcialmente implementada.** BrasilAPI para feriados nacionais reconhecidos pelo provider; cobertura regional implementada para SP e Santos/SP. |
| **Localidades** | **Implementada.** Consulta de estados e municípios e validação da localização da instalação via IBGE. |
| **Usuários e acesso** | **Implementada.** Criação de conta pelo fluxo `/setup`, login/logout por sessão, senha BCrypt e alteração de senha; a API de usuários permite apenas consulta autenticada do próprio ID. |
| **Configurações** | **Implementada.** Perfil, alteração de senha e localização compartilhada pela instalação em `/configuracoes`. |
| **Notificações** | **Implementada.** Central persistente por usuário, sincronizada ao abrir/solicitar atualização, com lembretes de tarefas, compromissos, hábitos e projetos. |
| **Descrições formatadas** | **Implementada.** Markdown renderizado no navegador nos formulários e nas visualizações; o backend continua armazenando texto. |

---

## 3. Tecnologias e Ferramentas

### Backend
- **Java 21** — linguagem de programação principal.
- **Spring Boot 4.1.2** — framework base para a arquitetura em camadas.
- **Spring Data JPA & Hibernate** — persistência relacional com lazy loading e métodos de consulta derivados.
- **Jakarta Bean Validation** — validação declarativa de dados de entrada via anotações nos records.
- **Spring Security** — formulário de login por sessão, `UserDetailsService`, BCrypt, CSRF habilitado e autorização das rotas.
- **Springdoc OpenAPI 3.2.0** — documentação padronizada e interface interativa Swagger UI.
- **Spring Web MVC / Thymeleaf** — páginas server-side e endpoints MVC.
- **Apache Maven Wrapper (`mvnw`)** — automação de build e gerenciamento de dependências.

### Integrações Externas
- **BrasilAPI** — fornece as datas usadas pelo provider nacional, consultadas por `RestClient` com timeout de conexão de 3 segundos e leitura de 5 segundos.
- **IBGE Localidades** — lista estados e municípios e valida a combinação de UF, nome e código IBGE antes de salvar a localização.

O `FeriadoService` mantém cache em memória por 12 horas, com chave por ano e localização; o cache de localidades do IBGE tem TTL de 24 horas. Não há persistência nem compartilhamento desses caches entre instâncias, e eles são perdidos ao reiniciar a aplicação. Respostas válidas, inclusive vazias, são armazenadas; uma consulta parcial por falha de provider não é cacheada. Os providers disponíveis ainda podem contribuir com os dados do calendário.

As fontes estaduais e municipais não têm cobertura nacional uniforme. Há provider estadual para São Paulo e municipal para Santos/SP; para outras localidades, não são inferidas datas regionais. Carnaval, Sexta-feira Santa e Corpus Christi podem aparecer como ponto facultativo; em Santos, Sexta-feira Santa e Corpus Christi também têm incidência municipal. Quarta-feira de Cinzas está explicitamente incluída para 2026. Páscoa não é retornada como feriado.

A localização usa os serviços oficiais do IBGE apenas para dados geográficos e validação, não como fonte de feriados. Ela fica no arquivo local da instalação, fora do repositório, e é compartilhada por seus usuários.

### Frontend
- **Thymeleaf** — engine de templates server-side integrado ao Spring Boot.
- **Tailwind CSS** — estilização e layout responsivo via CDN.
- **Lucide Icons** — biblioteca de ícones SVG carregada via CDN.
- **JavaScript (Vanilla)** — interações, notificações, seleção de localidades e renderer compartilhado de Markdown.
- Não existe um projeto frontend separado nem um passo de build Node/npm; templates, CSS e JavaScript são servidos como recursos estáticos do Spring Boot.

### Persistência
- As entidades JPA cobrem usuários, tarefas, projetos, matérias, conteúdos de estudo, compromissos, hábitos, registros de hábito e notificações.
- A localização e as credenciais de conexão configuradas pelo assistente ficam em arquivo de propriedades fora do repositório; as demais entidades são persistidas no PostgreSQL.
- Descrições são mantidas como texto/string. O Markdown é interpretado apenas na apresentação web.

### Banco de Dados
- **PostgreSQL** — sistema gerenciador relacional; o projeto não fixa uma versão mínima do servidor.

---

## 4. Arquitetura e Decisões de Design

O projeto adota uma arquitetura em camadas com separação estrita de responsabilidades:

```
src/main/java/com/planner/
├── agenda          # Compromisso: Entity, Repository, DTOs, Service e REST Controller
├── common          # Exceções de domínio, manipulador global (RFC 7807) e UsuarioContextService
├── config          # SecurityConfig, instalação e localização consultada/validada pelo IBGE
├── dashboard       # Dashboard: DTOs, Service de agregação e MVC Controller
├── estudos         # Matéria e ConteúdoEstudo: Entities, Repositories, DTOs, Services e Controllers
├── habitos         # Hábito e RegistroHabito: Entities, Repositories, DTOs, Services e Controllers
├── integracao      # Feriados: BrasilAPI e provedores regionais com consolidação e cache
├── notificacao     # Notificações persistidas, reconciliação de eventos e API
├── projeto         # Projeto: Entity, Repository, DTOs, Service e Controller
├── tarefa          # Tarefa: Entity, Repository, DTOs, Service e MVC Controller
└── usuario         # Usuário, login, UserDetailsService, DTOs, Repository e Service
```

As responsabilidades seguem o fluxo Controller (REST ou MVC) → Service → Repository/integração, com DTOs nas fronteiras REST. Os templates Thymeleaf ficam em `src/main/resources/templates`; estilos e scripts em `static/css` e `static/js`. Fragmentos de navegação, perfil e notificações são reutilizados pelas páginas.

### Decisões Arquiteturais Relevantes

1. **Sem Lombok:** Código explícito e transparente com getters, setters e construtores desenvolvidos manualmente nas entidades e uso nativo de **Java Records** para DTOs.
2. **Isolamento de Dados por Usuário:** Os services de domínio recebem o ID derivado do principal autenticado e limitam buscas/mutações ao proprietário.
3. **Resolução de Contexto por Autenticação:** O `UsuarioContextService` obtém o e-mail do principal autenticado e resolve o usuário no repositório; não usa ID fixo de desenvolvimento.
4. **Tratamento Global de Erros (RFC 7807):** Respostas de erro uniformes via `GlobalExceptionHandler` utilizando `ProblemDetail` com campos `type`, `title`, `status`, `detail`, `timestamp` e `erros` (validações).
5. **Dashboard Agregado:** O `DashboardService` reúne métricas dos módulos, compromissos e feriados da localização; retorna dados vazios quando não há usuário resolvido, sem substituir erros gerais por sucesso.

---

## 5. Instruções para Execução Local

### Pré-requisitos
- **Java Development Kit (JDK) 21** instalado e configurado no `PATH`.
- **PostgreSQL** acessível; o projeto não declara uma versão mínima do servidor.
- **Git** instalado.

### Passo 1: Clonar o Repositório
```bash
git clone https://github.com/ItsMazzu/intelliplan.git
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

### Passo 4: Configurar a instalação e criar a conta
Inicie a aplicação com uma conexão PostgreSQL inicializável. Acesse `/setup` para testar/salvar os parâmetros do banco, selecionar estado e município e criar a primeira conta. Se o assistente salvar uma conexão diferente da que foi carregada no início, reinicie a aplicação antes de continuar. Após a instalação, `/setup` também apresenta o cadastro de contas adicionais.

### Passo 5: Compilar e Executar a Aplicação

**Windows (PowerShell):**
```bash
.\mvnw.cmd test
.\mvnw.cmd clean package -DskipTests
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**
```bash
./mvnw test
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

A aplicação inicia na porta padrão `8080`. Não há inicialização separada de frontend: o Spring Boot serve as páginas Thymeleaf, CSS e JavaScript.

As configurações padrão estão em `src/main/resources/application.properties`: URL `jdbc:postgresql://localhost:5432/intelliplan`, usuário `postgres` e senha vazia. Podem ser sobrescritas por `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`, ou pelo arquivo local de configuração descrito abaixo. O Hibernate está configurado com `ddl-auto=update` e `show-sql=true`; o projeto não configura ferramenta de migração de schema.

---

## 6. Acesso aos Canais da Aplicação

### Interface Web (Dashboard)
Abra o navegador no endereço:
```
http://localhost:8080/
```
*(ou `http://localhost:8080/dashboard`)*

O dashboard e as demais páginas autenticadas exigem login. A cobertura regional validada de feriados está limitada a São Paulo e Santos/SP; outras localidades recebem somente datas nacionais disponibilizadas pelos providers.

### Documentação OpenAPI / Swagger UI
Para explorar e testar interativamente os endpoints REST. As rotas Swagger/OpenAPI exigem autenticação com uma conta do IntelliPlan:
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

### Segurança e armazenamento local

- O login usa e-mail e senha por formulário do Spring Security, sessão HTTP e senha armazenada com BCrypt. Não há JWT nem provedor externo de identidade.
- CSRF permanece habilitado pelo Spring Security; formulários MVC e chamadas mutáveis da central de notificações enviam token CSRF.
- As rotas de aplicação e APIs exigem autenticação, exceto rotas explicitamente públicas, como `/login`, `/setup/**`, recursos estáticos e consultas de localidades do IBGE. O Swagger exige a role `USER`; `POST /api/v1/usuarios` é negado. A criação de contas é feita pelo fluxo público `/setup`.
- Logout é servido por `POST /logout`; alterar a senha exige a senha atual e encerra a sessão.
- O `UsuarioContextService` resolve a identidade autenticada pelo e-mail. Os services de domínio escopam os dados do usuário atual.

- O arquivo local fica em `%USERPROFILE%\.intelliplan\application.properties` no Windows ou `~/.intelliplan/application.properties` no Linux/macOS.
- Administradores podem alterar o caminho com `INTELLIPLAN_CONFIG_FILE`.
- Em sistemas compatíveis com POSIX, o arquivo usa permissões exclusivas do proprietário. No Windows, a proteção efetiva depende das permissões da conta e do diretório; o arquivo não é criptografado.
- A senha do usuário é armazenada com BCrypt. A senha do PostgreSQL pode ficar em texto no arquivo local; restrinja o acesso ao arquivo e não use diretórios/contas compartilhados.
- O estado de instalação e a localização compartilhada ficam no mesmo arquivo. A instalação também considera a existência de usuários no PostgreSQL.

### Formatação das descrições

Os campos de descrição são persistidos/transmitidos como texto. O frontend usa o renderer compartilhado `markdown-renderer.js` tanto no preview dos formulários quanto nas visualizações de cards/detalhes; não é adicionada uma transformação Markdown no backend. São suportados títulos, parágrafos/quebras de linha, listas simples, negrito, itálico, tachado, links, código em linha e blocos de código. Conteúdo HTML digitado é tratado como texto e links são limitados a `http`, `https`, `mailto` e caminhos seguros. Não é uma implementação completa do CommonMark (por exemplo, não documenta suporte a tabelas ou imagens).

---

## 7. Estado da implementação neste checkout

| Componente | Situação | Observações |
|---|---|---|
| **Estrutura Base e Maven** | Concluída | Java 21, Spring Boot 4.1.2 e empacotamento WAR operacional. |
| **Modelagem JPA (9 entidades)** | Implementada | Inclui `Notificacao`, além das oito entidades de domínio listadas no histórico inicial. |
| **Repositories (9 interfaces)** | Implementados | Interfaces Spring Data JPA para as entidades persistidas. |
| **DTOs e Validações** | Concluídos | Records Java com validações Jakarta (`@NotBlank`, `@Size`, etc.). |
| **Services de domínio** | Implementados | Regras de negócio, transações e escopo pelo usuário autenticado. |
| **Tratamento de Exceções** | Concluído | `GlobalExceptionHandler` mapeando exceções de domínio e validação para RFC 7807 (`ProblemDetail`). |
| **APIs REST** | Implementadas parcialmente por domínio | Projetos, matérias, conteúdos, compromissos, hábitos/registros, usuários (consulta), notificações, feriados e localidades. Tarefas são MVC, não REST. |
| **Integração de feriados** | Parcialmente implementada | Cobertura nacional via BrasilAPI e regional explicitamente restrita a SP e Santos/SP. |
| **Localidades IBGE** | Implementada | Listagem de estados/municípios e validação; cache local de 24 horas. |
| **Segurança** | Implementada | Login por sessão, BCrypt, CSRF padrão e autorização por rota/identidade; criação de contas permanece pública pelo fluxo `/setup`. |
| **Assistente de instalação** | Implementado | Testa e persiste conexão, permite criar conta e exige localização no primeiro cadastro. A aplicação precisa iniciar com um datasource utilizável para renderizar o assistente. |
| **Interface Web** | Implementada | Templates Thymeleaf, fragments compartilhados, páginas responsivas e JavaScript vanilla; sem build frontend separado. |

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
