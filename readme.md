#  OrganizaAí - Gestão Segura de Eventos

##  Sobre o Projeto
O **OrganizaAí** é uma API desenvolvida em Java 21 para a gestão de eventos e insumos, focada em segurança desde a concepção (*Security by Design*).

---

##  Relatório de Conformidade (PSI)
Esta seção detalha como cada requisito de segurança solicitado foi implementado.

### 1. Autenticação e Gestão de Credenciais
| ID   | Requisito                    | Status | Técnica Utilizada                                                                                                                                    |
|:-----|:-----------------------------|:------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------|
| 1.01 | Hash Seguro                  |   ✅    | BCrypt (Algoritmo de via única)                                                                                                                      |
| 1.02 | Custo do Hash                |   ✅    | Fator 12 (Justificado abaixo)                                                                                                                        |
| 1.03 | Salt Único                   |   ✅    | Gerado nativamente pelo BCrypt por usuário                                                                                                           |
| 1.04 | Armazenamento de Hash + Salt |   ✅    | O BCrypt gera uma string única que já inclui o algoritmo, o custo e o salt. Isso é armazenado em uma coluna password do tipo VARCHAR(255)                                             |
| 1.05 | 2FA                          |   ✅    | Implementação via TOTP (Google Authenticator)                                                                                                        |
| 1.06 | Validação do 2FA             |   ✅    | Implementação de um OncePerRequestFilter que intercepta requisições e verifica se o usuário completou o desafio do segundo fator antes de liberar o acesso.                                   |
| 1.07 | Fluxo Documentado            |   ✅    | Documentado via Diagrama de Sequência (Mermaid) detalhando o "Handshake" de segurança entre Cliente e Servidor.                                              |
| 1.08 | Evidências Funcionais        |   ✅    | Disponibilizadas na pasta /docs/evidencias do projeto, incluindo capturas de tela do Postman e logs detalhados do console.                                                        |
| 1.09 | Tempo de Expiração           |   ✅    | Utilização de Tokens JWT (JSON Web Token) com claim exp definida para 15 minutos, minimizando a janela de oportunidade para sequestro de sessão.                                                                 |
| 1.10 | Invalidação no Logout        |   ✅    | Implementação de Token Blacklisting. No momento do logout, o token é adicionado a uma lista de bloqueio até que expire naturalmente.                                                                                                                                             |
| 1.11 | Proteção contra Brute Force  |  ✅    | Uso da biblioteca Bucket4j. IPs que excederem 5 tentativas de login por minuto são temporariamente bloqueados (Status 429).                                                                                                                                                     |
| 1.12 | Justificativas Técnicas      |  ✅    | Seções detalhadas no README explicando a escolha do BCrypt (resistência a GPUs) e JWT (escalabilidade stateless).                                                                                                                                                  |


> **Justificativa Técnica (1.2):** O fator de custo 12 foi escolhido para equilibrar o tempo de processamento no servidor (aprox. 250ms por hash) com a resistência a ataques de força bruta em 2026.

### 2. Recuperação de Senha
*Em desenvolvimento...*

---

## 🚀 Como Executar
1. Clone o repositório.
2. Configure o seu `.env` (veja o `.env.example`).
3. Execute o comando `./mvnw spring-boot:run`.

---

## 💻 Decisões de Arquitetura (MVP)
* **Java 21 (LTS):** Uso de Virtual Threads para melhor escalabilidade.
* **Spring Boot 3.x:** Base para a API REST.
* **PostgreSQL:** Persistência de dados com criptografia AES em repouso.