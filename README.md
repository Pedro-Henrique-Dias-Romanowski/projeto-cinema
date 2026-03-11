# 🎬 Sistema de Cinema - Arquitetura de Microserviços

![CI/CD](https://github.com/pedro-romanski/projetoPessoalCinema/actions/workflows/ci-cd.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen?logo=spring)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-orange?logo=rabbitmq)

Sistema completo de gerenciamento de cinema desenvolvido com arquitetura de microserviços, demonstrando conceitos modernos de desenvolvimento backend, containerização, mensageria assíncrona e CI/CD.

---

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Arquitetura](#-arquitetura)
- [Microserviços](#-microserviços)
- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Execução](#-execução)
- [CI/CD](#-cicd)
- [Endpoints da API](#-endpoints-da-api)
- [Roadmap](#-roadmap)

---

## 🎯 Visão Geral

Sistema que simula as operações de um cinema moderno, incluindo:
- Autenticação e autorização de usuários (JWT)
- Gerenciamento de clientes e carteira digital
- Catálogo de filmes
- Sessões de cinema com reservas
- Pagamentos e notificações por email
- Mensageria assíncrona para eventos do sistema

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                       Cliente (Web/Mobile)                   │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    API Gateway                               │
│                    - Roteamento centralizado                 │
│                    - CORS                                    │
│                    - Rate Limiting                           │
└───────────────────────────┬─────────────────────────────────┘
                            |
        __________________________________________
       |            Service Discovery              |
       |                                          |
       |__________________________________________| 
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼────────┐  ┌──────▼──────┐  ┌────────▼────────┐
│ ms-autenticacao│  │ ms-catalogo  │  │  ms-clientes    │
│   - Login JWT  │  │  - Filmes    │  │  - Cadastro     │
│   - Cadastro   │  │  - CRUD      │  │  - Carteira     │
│   - Security   │  │              │  │  - Pagamentos   │
└────────────────┘  └──────────────┘  └─────────┬───────┘
                                                  │
                    ┌─────────────────────────────┤
                    │                             │
            ┌───────▼────────┐          ┌────────▼────────┐
            │  ms-sessoes    │          │   RabbitMQ      │
            │  - Sessões     │◄─────────┤   - Pagamentos  │
            │  - Reservas    │          │   - DLQ         │
            │  - Email       │          └─────────────────┘
            └────────────────┘
                    │
        ┌───────────┴───────────┐
        │                       │
┌───────▼────────┐    ┌────────▼────────┐
│  MySQL (x4)    │    │     Zipkin      │
│  - Autenticacao│    │   Tracing       │
│  - Catalogo    │    │    :9411        │
│  - Clientes    │    └─────────────────┘
│  - Sessoes     │
└────────────────┘
```

### Comunicação entre Serviços:
- **Síncrona**: OpenFeign (REST)
- **Assíncrona**: RabbitMQ (AMQP)

---

## 🧩 Microserviços

### 1️⃣ **ms-autenticacao-cinema** `:8084`
Microserviço de autenticação e autorização.

**Responsabilidades:**
- Cadastro de clientes e administradores
- Login com JWT (JSON Web Token)
- Validação de tokens
- Controle de acesso baseado em roles (ADMIN, USER)

**Tecnologias:**
- Spring Security
- JWT (Auth0 java-jwt)
- BCrypt para hash de senhas
- Comunicação com ms-clientes via Feign

---

### 2️⃣ **ms-gerenciamento-catalogo** `:8082`
Microserviço de gerenciamento do catálogo de filmes.

**Responsabilidades:**
- CRUD de filmes
- Consulta por título, gênero, data de lançamento
- Controle de disponibilidade
- Acesso restrito a administradores

**Tecnologias:**
- Spring Data JPA
- MySQL com Flyway migrations
- OAuth2 Resource Server

---

### 3️⃣ **ms-gerenciamento-clientes** `:8080`
Microserviço de gerenciamento de clientes e carteira digital.

**Responsabilidades:**
- Cadastro e perfil de clientes
- Carteira digital (saldo)
- Processamento de pagamentos
- Validação de saldo
- Notificações por email
- Publicação de eventos de pagamento (RabbitMQ)

**Tecnologias:**
- Spring Data JPA
- RabbitMQ (Producer)
- Spring Mail (Gmail SMTP)
- Validação com Bean Validation

---

### 4️⃣ **ms-gerenciamento-sessoes** `:8081`
Microserviço de sessões de cinema e reservas.

**Responsabilidades:**
- Gerenciamento de sessões (data, horário, sala, preço)
- Sistema de reservas
- Validação de disponibilidade
- Integração com catálogo (filmes) e clientes
- Consumo de eventos de pagamento (RabbitMQ)
- Notificações por email de confirmação/cancelamento

**Tecnologias:**
- Spring Data JPA
- RabbitMQ (Consumer/Producer)
- OpenFeign (integração com outros serviços)
- Spring Mail

---

### 5️⃣ **ms-api-gateway** `:8085`
Gateway centralizado para roteamento e gerenciamento de requisições.

**Responsabilidades:**
- Roteamento centralizado para todos os microserviços
- Configuração de CORS
- Agregação de documentação Swagger
- Logging de requisições
- Integração com Service Discovery

**Tecnologias:**
- Spring Cloud Gateway (WebFlux)
- Eureka Client
- SpringDoc OpenAPI

---

### 6️⃣ **service-discovery-cinema** `:8761`
Service Discovery para registro automático de serviços.

**Responsabilidades:**
- Registro dinâmico de microserviços
- Health checking
- Load balancing client-side
- Descoberta de serviços

**Tecnologias:**
- Netflix Eureka Server
- Spring Cloud Netflix

---

## 🛠️ Tecnologias

### Backend
- **Java 21** (OpenJDK)
- **Spring Boot 4.0.x**
- **Spring Cloud 2025.1.0**
  - Spring Cloud Gateway
  - OpenFeign
- **Spring Security + JWT**
- **Spring Data JPA**
- **Resilience4j**
- **Zipkin**
- **Bean Validation**

### Banco de Dados
- **MySQL 8.0** (4 instâncias - uma por microserviço)
- **Flyway** (migrations e versionamento)

### Mensageria
- **RabbitMQ 3** (com Management Plugin)
  - Exchanges (Fanout, Direct)
  - Queues com Dead Letter Queue (DLQ)
  - Retry mechanisms

### DevOps e Infraestrutura
- **Docker** (multi-stage builds)
- **Docker Compose** (orquestração local)
- **GitHub Actions** (CI/CD)
- **Docker Hub** (registry de imagens)

---

## ✅ Pré-requisitos

- **Docker** 20.10+
- **Docker Compose** 2.0+
- **Java 21** (apenas para desenvolvimento local)
- **Maven 3.9+** (apenas para desenvolvimento local)

---

## 🚀 Execução

### 1️⃣ Clone o repositório
```bash
git clone https://github.com/pedro-romanski/projetoPessoalCinema.git
cd projetoPessoalCinema
```

### 2️⃣ Configure as variáveis de ambiente
```bash
# Copie o arquivo de exemplo
cp .env.example .env

# Edite o arquivo .env com suas configurações
# IMPORTANTE: Configure o MAIL_USER e MAIL_PASSWORD para notificações funcionarem
```

**Principais variáveis:**
```env
# JWT Secret (altere para produção)
JWT_SECRET=sua-chave-secreta-jwt

# Email (Gmail - gere uma senha de app)
MAIL_USER=seu-email@gmail.com
MAIL_PASSWORD=sua-senha-de-app

# Banco de dados (podem manter os valores padrão)
DB_USER=cinema_user
DB_PASSWORD=cinema_password
```

### 3️⃣ Execute com Docker Compose
```bash
# Inicia todos os serviços
docker-compose up -d

# Visualizar logs
docker-compose logs -f

# Parar os serviços
docker-compose down
```

### 4️⃣ Aguarde os serviços iniciarem
Os microserviços estarão disponíveis em aproximadamente 1-2 minutos.

**Health checks:**
- http://localhost:8084/actuator/health (Autenticação)
- http://localhost:8082/actuator/health (Catálogo)
- http://localhost:8080/actuator/health (Clientes)
- http://localhost:8081/actuator/health (Sessões)

---

## 🔄 CI/CD

Pipeline automatizado com **GitHub Actions** que executa a cada push/PR:

### Etapas do Pipeline:
1. ✅ **Build** de todos os microserviços com Maven
2. ✅ **Testes** unitários e de integração
3. ✅ **Build** das imagens Docker
4. ✅ **Tag** com versionamento (`latest` + `run-id`)
5. ✅ **Push** para Docker Hub

### Imagens no Docker Hub:
- `seu-usuario/ms-autenticacao-cinema`
- `seu-usuario/ms-gerenciamento-catalogo`
- `seu-usuario/ms-gerenciamento-clientes`
- `seu-usuario/ms-gerenciamento-sessoes`

---

## 📡 Documentação da API

### Documentação Centralizada (Recomendado)
**Swagger UI Gateway:** http://localhost:8085/swagger-ui.html

Acesse toda a documentação de forma unificada através do API Gateway.

---

### Documentação Individual por Microserviço

#### 🔐 Autenticação (`:8084`)
**Swagger UI:** http://localhost:8084/swagger-ui.html


#### 🎬 Catálogo (`:8082`)
**Swagger UI:** http://localhost:8082/swagger-ui.html


#### 👥 Clientes (`:8080`)
**Swagger UI:** http://localhost:8080/swagger-ui.html


#### 🎟️ Sessões (`:8081`)
**Swagger UI:** http://localhost:8081/swagger-ui.html

---

### 🐰 RabbitMQ Management
**URL:** http://localhost:15672  
**Credenciais:**
- Usuário: `cinema_rabbitmq` (configurável no .env)
- Senha: `rabbitmq_password` (configurável no .env)

**Queues configuradas:**
- `pagamentos.detalhes` - Fila de processamento de pagamentos
- `pagamentos.detalhes.dlq` - Dead Letter Queue para falhas

---

### 🔍 Zipkin (Distributed Tracing)
**URL:** http://localhost:9411  

Visualize traces distribuídos entre os microserviços para debug e análise de performance.

---

## 📊 Monitoramento

### Actuator Endpoints (todos os serviços)
```
GET /actuator/health       # Status do serviço
GET /actuator/info         # Informações da aplicação
GET /actuator/metrics      # Métricas de performance
```

---

## 🗄️ Banco de Dados

### Acesso MySQL (via cliente)
```bash
# MySQL Autenticação
mysql -h localhost -P 3307 -u cinema_user -p
# Database: db_autenticacao

# MySQL Catálogo
mysql -h localhost -P 3308 -u cinema_user -p
# Database: db_catalogo

# MySQL Clientes
mysql -h localhost -P 3309 -u cinema_user -p
# Database: db_clientes

# MySQL Sessões
mysql -h localhost -P 3310 -u cinema_user -p
# Database: db_sessoes
```

**Senha:** Conforme configurado no `.env` (`DB_PASSWORD`)

---

## 🙏 Agradecimentos

Projeto desenvolvido para fins de aprendizado e demonstração de conceitos de arquitetura de microserviços, containerização e DevOps
