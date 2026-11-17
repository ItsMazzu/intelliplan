# Diretrizes de Desenvolvimento — IntelliPlan

1. **Qualidade e Clareza:**
   - Priorizar código limpo, explícito, seguro e defensável.
   - Não adicionar comentários explicativos no meio do código.
   - Não utilizar Lombok (utilizar construtores, getters e setters explícitos quando necessário).
   - Não criar abstrações prematuras ou padrões genéricos sem necessidade concreta.

2. **Arquitetura e Responsabilidades:**
   - Preservar rigorosamente a arquitetura de pacotes existente.
   - Não duplicar responsabilidades entre camadas.
   - Utilizar injeção de dependência via construtor.
   - Não criar funcionalidades fora do escopo solicitado.

3. **Persistência e JPA:**
   - Não expor entidades JPA diretamente em respostas externas (utilizar DTOs).
   - Não utilizar `CascadeType.ALL` indiscriminadamente.
   - Não utilizar `FetchType.EAGER` sem justificativa comprovada (preferir `FetchType.LAZY`).

4. **Robustez e Configuração:**
   - Não ignorar exceções ou utilizar blocos `catch` genéricos sem tratamento adequado.
   - Não adicionar dependências no `pom.xml` sem necessidade concreta.
   - Não modificar arquivos de configuração global sem justificativa.

5. **Fluxo de Trabalho e Verificação:**
   - Antes de implementar: analisar detalhadamente o código relacionado existente.
   - Após implementar: revisar o próprio código e executar a compilação Maven (`mvnw compile` / `mvnw clean package -DskipTests`).
   - Informar claramente alterações realizadas, decisões tomadas e problemas encontrados.
   - Não afirmar que uma funcionalidade está concluída sem verificar sua implementação.
