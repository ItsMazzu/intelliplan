# IntelliPlan — Guia rápido

O IntelliPlan é uma aplicação web Java/Spring Boot para organizar tarefas, projetos, estudos, compromissos e hábitos. As páginas usam Thymeleaf; o backend também disponibiliza uma API REST.

## Pré-requisitos

- JDK 21
- PostgreSQL acessível
- Git

## Executar localmente

1. Crie um banco PostgreSQL chamado `intelliplan`.
2. Configure a conexão com `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`, ou no arquivo local `%USERPROFILE%\.intelliplan\application.properties` (Windows) / `~/.intelliplan/application.properties` (Linux/macOS). O caminho do arquivo pode ser definido por `INTELLIPLAN_CONFIG_FILE`.
3. Execute o aplicativo:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   Em Linux/macOS, use `./mvnw spring-boot:run`.

4. Acesse `http://localhost:8080/setup` para concluir a configuração e criar uma conta. A autenticação ocorre em `http://localhost:8080/login`.

O processo de setup precisa iniciar com uma conexão ao banco utilizável; se o assistente salvar uma conexão diferente da que foi carregada no início, reinicie a aplicação. O Spring Boot serve também o frontend; não há build separado de Node/npm.

## Documentação

- [README do projeto](README.md): funcionalidades, tecnologias, segurança e instruções operacionais.
- [Guia técnico](docs/GUIA_TECNICO.md): arquitetura, persistência, endpoints e integrações.
- [Histórico técnico](docs/HISTORICO_TECNICO.md): decisões e registros históricos do desenvolvimento.

Swagger UI: `http://localhost:8080/swagger-ui/index.html` (requer autenticação).
