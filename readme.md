# OrganizaAí - Gestão de Eventos

---

Lucas do Nascimento Lima - 11231101214 <br>
Diogo Mendes Baptista - 11231101219

---

![organiza-ai Logo](src/main/resources/static/images/banner.png)

<!-- Badges -->
<div align="center">

[![University: UMC](https://img.shields.io/badge/University-UMC-0D47A1?style=for-the-badge)](https://www.umc.br/)
![Java 21](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-brightgreen?style=for-the-badge&logo=spring-boot)
![PostgreSQL](https://img.shields.io/badge/MySQL-4169E1?style=for-the-badge&logo=mysql&logoColor=white)

</div>

---

##  Sobre o Projeto
O OrganizaAí é uma solução robusta em formato de API RESTful, desenvolvida com o que há de mais moderno no ecossistema Java 21. O projeto nasceu para sanar uma dor comum no convívio social: a fricção logística na organização de eventos em grupo.

Desde a indefinição de datas que gera conversas infinitas em aplicativos de mensagens até a dificuldade matemática de calcular insumos e custos por pessoa, o OrganizaAí centraliza e automatiza todo o ciclo de vida de um evento.

---

## Funcionalidades do Projeto

📅 **Gestão de Eventos e Datas**

- Criação de Eventos: Permite criar eventos personalizados (Churrascos, Reuniões, Festas).

- Votação de Datas: O organizador propõe múltiplas datas e os participantes votam nas que têm disponibilidade (estilo Doodle).

- Fechamento Automático: O sistema identifica a data com maior adesão para confirmar o evento.

🍖 **Gerenciamento de Insumos**

- Cálculo por Pessoa: Cálculo automático de quantidades (ex: gramas de carne, litros de bebida) com base no número de confirmados.

- Divisão de Custos: Gestão financeira para saber quanto cada participante deve contribuir.

- Lista de Compras: Geração de lista de itens necessários para a realização do evento.

🔐 **Segurança e Controle (Security by Design)**

- Autenticação JWT: Acesso seguro via Tokens para garantir que apenas usuários autorizados interajam com os eventos.

- Níveis de Acesso: Diferenciação entre organizadores (quem criou o evento) e participantes.

- Proteção de Dados: Implementação de filtros de segurança para evitar acessos indevidos a informações privadas de grupos.

---

## Estrutura do Repositório
```text
organiza-ai/
├── src/
│   ├── main/
│   │   ├── java/com/organizaai/
│   │   │   ├── config/          # Configurações (SecurityConfig, JWT, OpenAPI)
│   │   │   ├── controller/      # Endpoints da API (REST)
│   │   │   ├── enums/           # Enumerações (ex: role do usuário)
│   │   │   ├── model/           # Entidades do Banco de Dados (JPA)
│   │   │   ├── repository/      # Interfaces de acesso ao Banco (Spring Data JPA)
│   │   │   ├── service/         # Regras de negócio e lógica do sistema
│   │   │   └── OrganizaAiApplication.java
│   │   └── resources/
│   │       ├── static/images/   # Assets, logos e imagens do README
│   │       ├── templates/       # Templates
│   │       └── application.properties
│   └── test/                    #
├── .env.example                 # Exemplo de variáveis de ambiente
├── .gitignore                   # Arquivos ignorados pelo Git (target, .env)
├── pom.xml                      # Gerenciador de dependências Maven
└── readme.md                    # Documentação principal do projeto
```
---
## Arquitetura do Sistema

Abaixo está o diagrama de classes atualizado, baseado na modelação do sistema OrganizaAí:

```mermaid

classDiagram
    class Usuario {
        +Long id
        +String email
        +String password
        +Role role
    }


    class Evento {
        +Long id
        +String nome
        +String descricao
        +StatusEvento status
        +LocalDateTime dataConfirmada
        +calcularCustoTotal() BigDecimal
        +fecharVotacao() void
    }

    class Participante {
        +Long id
        +String nome
        +String email
        +Boolean confirmado
        +Boolean consomeAlcool
    }

    class ItemConsumo {
        +Long id
        +String nome
        +Double taxaPorPessoa
        +BigDecimal precoUnitario
        +TipoItem tipo
    }

    class SugestaoData {
        +Long id
        +LocalDateTime dataHora
        +Integer votos
    }

    Usuario "1" -- "N" Evento : organiza
    Evento "1" -- "N" Participante : possui
    Evento "1" -- "N" ItemConsumo : contém
    Evento "1" -- "N" SugestaoData : oferece
```

Notas sobre a Modelação:

Cálculo de Insumos: A lógica de consomeAlcool no Participante permite que o sistema filtre quem entra no cálculo de bebidas alcoólicas vs. refrigerantes/água.

Financeiro: O precoUnitario no ItemConsumo possibilita o método calcularCustoTotal() no Evento.

Fluxo de Decisão: O SugestaoData armazena os votos antes de o status do evento mudar e a dataConfirmada ser preenchida.

---
## Como Executar
1. Clone o repositório.
2. Configure o seu `.env` (veja o `.env.example`).
3. Execute o comando `./mvnw spring-boot:run`.

---
