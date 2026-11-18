# IntelliPlan

### Organize sua rotina. Planeje seus objetivos. Acompanhe sua evolução.

O **IntelliPlan** é uma aplicação web de organização pessoal desenvolvida para centralizar o gerenciamento de tarefas, projetos, estudos, compromissos e hábitos em um único ambiente.

A proposta é oferecer uma alternativa integrada às ferramentas tradicionais de produtividade, permitindo que o usuário organize diferentes áreas da sua rotina sem precisar distribuir suas atividades entre diversas plataformas.

---

## Sobre o projeto

O IntelliPlan surgiu da proposta de desenvolver uma plataforma que auxilie na organização e no acompanhamento da rotina pessoal e acadêmica.

A aplicação busca reunir recursos de planejamento e gerenciamento em uma interface intuitiva, estruturada em módulos independentes e integrados.

Além da interface web, o projeto contempla uma API REST, mecanismos de autenticação e autorização e integração com serviços externos.

## Funcionalidades

As funcionalidades planejadas para a aplicação estão organizadas nos seguintes módulos:

| Módulo | Descrição |
|---|---|
| Dashboard | Visão geral das atividades e informações relevantes da rotina. |
| Tarefas | Criação, organização e acompanhamento de tarefas, prioridades e status. |
| Projetos | Organização de atividades vinculadas a projetos pessoais ou acadêmicos. |
| Estudos | Gerenciamento de matérias e conteúdos de estudo. |
| Agenda | Organização de compromissos e eventos. |
| Hábitos | Acompanhamento de hábitos e registros de frequência. |

### Recursos complementares

- Autenticação e autorização de acesso.
- API REST para disponibilização dos recursos da aplicação.
- Documentação dos endpoints com OpenAPI/Swagger.
- Integração com serviços externos para ampliar as funcionalidades da plataforma.

> Os itens representam o escopo do projeto. A disponibilidade de cada funcionalidade será atualizada conforme sua implementação.

---

## Tecnologias e ferramentas

### Backend

- **Java 21** — linguagem de programação.
- **Spring Boot 4.1.2** — framework principal da aplicação.
- **Spring Data JPA** — abstração para persistência e acesso aos dados.
- **Hibernate** — implementação de referência para o mapeamento objeto-relacional utilizado pelo JPA.
- **Spring Security** — infraestrutura de autenticação e autorização.

### Frontend

- **HTML5** — estrutura das páginas.
- **CSS3** — estilização da interface.
- **Thymeleaf** — mecanismo de templates integrado ao Spring.

### Banco de dados

- **PostgreSQL** — sistema gerenciador de banco de dados relacional.
- **Jakarta Persistence (JPA)** — especificação utilizada no mapeamento das entidades.

### Validação e documentação

- **Jakarta Bean Validation** — validação dos dados de entrada.
- **Springdoc OpenAPI 3.2.0** — geração da documentação da API e interface Swagger UI.

### Infraestrutura e desenvolvimento

- **Apache Maven** — gerenciamento de dependências e processo de build.
- **Apache Tomcat** — servidor de aplicação utilizado na implantação WAR.
- **Git e GitHub** — versionamento e gerenciamento do código-fonte.
- **Visual Studio Code / Antigravity** — ambiente de desenvolvimento.

*As dependências e ferramentas são descritas conforme a configuração atual do projeto. A implementação efetiva de cada recurso será indicada no status de desenvolvimento.*

---

## Arquitetura e organização

O IntelliPlan adota uma organização modular por domínio, separando as responsabilidades da aplicação em camadas.

Essa abordagem busca facilitar a manutenção, a evolução e a compreensão do código, mantendo as regras de negócio separadas da persistência e da apresentação.

### Organização dos pacotes

| Pacote | Responsabilidade |
|---|---|
| `entity` | Entidades e mapeamentos de persistência. |
| `repository` | Interfaces de acesso e consulta aos dados. |
| `dto` | Objetos utilizados na transferência de dados. |
| `service` | Regras de negócio e coordenação das operações. |
| `controller` | Recebimento e tratamento das requisições. |
| `common` | Componentes compartilhados, como exceções. |
| `integracao` | Comunicação com serviços e APIs externas. |

### Princípios arquiteturais

- Separação de responsabilidades.
- Baixo acoplamento entre módulos.
- Encapsulamento das regras de negócio.
- Validação dos dados de entrada.
- Persistência estruturada com JPA.
- Injeção de dependências.
- Organização orientada à manutenção e evolução do sistema.

### Empacotamento

A aplicação utiliza o formato **WAR (Web Application Archive)**, permitindo sua implantação em um servidor de aplicações compatível com Servlet.

---

## Modelo de domínio

O modelo de domínio é composto pelas seguintes entidades principais:

| Entidade | Responsabilidade |
|---|---|
| `Usuario` | Representa o usuário e seu contexto de utilização. |
| `Tarefa` | Representa atividades organizadas por status e prioridade. |
| `Projeto` | Agrupa e organiza atividades relacionadas. |
| `Materia` | Representa uma disciplina ou área de estudo. |
| `ConteudoEstudo` | Organiza conteúdos vinculados a uma matéria. |
| `Compromisso` | Representa eventos e compromissos da agenda. |
| `Habito` | Representa atividades recorrentes que o usuário deseja acompanhar. |
| `RegistroHabito` | Armazena as ocorrências associadas ao acompanhamento de hábitos. |

Os relacionamentos entre essas entidades são definidos por meio do mapeamento JPA.

---

## Status do desenvolvimento

<badge color="info">Em desenvolvimento</badge>

O IntelliPlan encontra-se em fase de construção da estrutura principal e implementação das regras de negócio.

| Etapa | Situação |
|---|---|
| Estrutura inicial do projeto | Concluída |
| Modelagem das entidades JPA | Concluída |
| Repositories | Concluídos |
| DTOs e validações | Concluídos |
| Exceções customizadas | Concluídas |
| Camada de Services | Concluída |
| Autenticação e autorização | Pendente |
| Controllers e API REST | Pendente |
| Documentação funcional da API | Pendente |
| Integração com serviços externos | Pendente |
| Interface web | Pendente |
| Validação e implantação final | Pendente |

*O status será atualizado conforme as etapas forem concluídas e verificadas.*

---

## Execução do projeto

As instruções de instalação, configuração do ambiente, conexão com o PostgreSQL e execução serão adicionadas após a conclusão das configurações necessárias para executar a aplicação.

---

## Equipe de desenvolvimento

**Italo Mazzucatto**

Desenvolvimento e implementação da aplicação.

**Laís Campos**

Desenvolvimento e implementação da aplicação.

---

## Objetivo acadêmico

O IntelliPlan é desenvolvido no contexto acadêmico como projeto de aplicação de conhecimentos em desenvolvimento de software, arquitetura de aplicações web, persistência de dados, segurança e integração de sistemas.

O projeto busca aplicar boas práticas de engenharia de software durante todo o seu ciclo de desenvolvimento.

---

*IntelliPlan — Organização e planejamento em um só lugar.*
