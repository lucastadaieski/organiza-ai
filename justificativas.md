OrganizaAI - API de Gestão de Eventos
Este projeto é uma API REST desenvolvida com Spring Boot 3 e MySQL, focada na organização de eventos sociais (churrascos), com rigorosos padrões de segurança e defesa cibernética.

🛡️ Justificativas Técnicas de Segurança (Requisito 1.12)
Abaixo estão detalhadas as escolhas tecnológicas e arquiteturais implementadas no bloco de Autenticação e Gestão de Credenciais.

1. Criptografia de Senhas (BCrypt)
   Escolha: Foi utilizado o algoritmo BCrypt para o hashing de senhas.

Justificativa: Diferente de algoritmos como MD5 ou SHA-256, o BCrypt é projetado para ser deliberadamente lento e computacionalmente caro. Ele utiliza um fator de custo (Work Factor) que protege contra ataques de força bruta e evolução do hardware (Lei de Moore).

Parâmetros de Custo (Strength): Configurado com custo 12, garantindo um equilíbrio entre segurança (resistência a ataques de dicionário) e performance do servidor.

Salt: O BCrypt gera e armazena automaticamente um Salt criptográfico único para cada usuário, eliminando a viabilidade de ataques via Rainbow Tables.

2. Autenticação de Dois Fatores (2FA/TOTP)
   Escolha: Implementação do padrão TOTP (Time-based One-Time Password) via Google Authenticator (RFC 6238).

Justificativa: Adiciona uma camada de segurança física. Mesmo que as credenciais primárias (e-mail e senha) sejam comprometidas, o acesso só é concedido mediante a posse do dispositivo físico sincronizado. Foi preferido ao SMS por ser imune a ataques de SIM Swapping.

3. Gestão de Sessões via JWT (Stateless)
   Escolha: Uso de JSON Web Tokens (JWT) assinado com algoritmo HMAC-SHA384.

Justificativa: Adota uma arquitetura Stateless, o que permite escalabilidade horizontal (essencial para sistemas que podem crescer).

Expiração: Os tokens possuem tempo de expiração curto, limitando a janela de oportunidade em caso de interceptação.

4. Invalidação de Sessão (Logout Blacklist)
   Escolha: Implementação de uma tabela de Blacklist no banco de dados para tokens revogados.

Justificativa: Como o JWT é stateless e não pode ser "cancelado" na origem após emitido, o sistema armazena os tokens deslogados até o seu tempo natural de expiração. O filtro de segurança consulta esta lista em cada requisição, garantindo que um logout seja respeitado instantaneamente.

5. Proteção contra Força Bruta
   Escolha: Política de bloqueio temporário após 5 tentativas falhas.

Justificativa: Implementa um atraso exponencial e bloqueio de 15 minutos para prevenir ataques automatizados de preenchimento de credenciais (Credential Stuffing).