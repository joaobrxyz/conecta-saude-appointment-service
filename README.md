# 📅 Appointment Service - Conecta Saúde

Este repositório contém o microsserviço **Appointment Service**, responsável pela gestão completa da agenda, marcações de consultas e ciclo de vida dos atendimentos na clínica.

## 🎯 Funcionalidades Principais

*   **Gestão de Marcações:** Criação, listagem, atualização e cancelamento de consultas, com validação de horários (intervalos de 15 minutos).
*   **Máquina de Estados (Lifecycle):** Controlo rigoroso do fluxo de atendimento, transitando pelos estados: `AGENDADA`, `EM_ESPERA`, `EM_ATENDIMENTO`, `REALIZADA` e `CANCELADA`[cite: 8].
*   **Processamento Automático de Faltas (Cron Job):** Rotina agendada diária (às 08h00) que identifica consultas do dia anterior que não foram concluídas e atualiza o estado para `NAO_COMPARECEU`[cite: 8].
*   **Comunicação Síncrona:** Utiliza **Feign Clients** para comunicar com o `identity-service` e validar os dados de médicos, pacientes e os seus respetivos níveis de acesso[cite: 8].

## 🛠️ Tecnologias Utilizadas

*   **Java 17+**
*   **Spring Boot** (Core e Web)
*   **Spring Data JPA** & **PostgreSQL** (Persistência de dados)
*   **Spring Cloud OpenFeign** (Integração entre microsserviços)
*   **Maven** (Gestão de dependências)[cite: 8]
*   **Docker & Docker Compose** (Contentorização)[cite: 8]

## 📦 Base de Dados (Database-per-Service)

De forma a manter o isolamento arquitetural, este serviço possui a sua própria base de dados exclusiva:
*   **Motor:** PostgreSQL
*   **Nome:** `db_appointment`
*   **Porta Local (Docker):** `5435`

## 🚀 Como Executar

### Pré-requisitos
*   É necessário ter a base de dados PostgreSQL ativa.
*   O `identity-service` deve estar em execução para que a validação de utilizadores via Feign Client funcione corretamente.

### Localmente (Dev Mode)
Este serviço corre por defeito na porta `8081`. Para o iniciar usando o *wrapper* do Maven[cite: 8], executa o seguinte comando na raiz do projeto:

```bash
./mvnw spring-boot:run
Via Docker
Se preferires executar tudo de forma orquestrada, utiliza o docker-compose.yml[cite: 8] na raiz do ecossistema principal.

🤝 Autor
Desenvolvido por joaobrxyz como componente fundamental do sistema Conecta Saúde.