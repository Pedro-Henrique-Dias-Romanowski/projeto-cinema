Projeto Cinema – Descrição
🎬 Projeto Cinema

Este projeto consiste na simulação de um sistema de cinema baseado em microsserviços, com o objetivo de aplicar, na prática, conceitos modernos de arquitetura distribuída, containerização, orquestração e automação de deploy.

🧩 Microsserviços

A aplicação é composta por três microsserviços independentes, cada um com responsabilidades bem definidas:

ms-gerenciamento-clientes:
 Responsável pelo cadastro, consulta e gerenciamento dos clientes do cinema.

ms-gerenciamento-sessoes:
 Responsável pelo gerenciamento das sessões de filmes, bem como pelas reservas e cancelamentos realizados pelos clientes.

ms-gerenciamento-catalogo:
 Responsável pelo gerenciamento do catálogo de filmes disponíveis no cinema.

⚙️ Perfis de Execução

O projeto conta com dois perfis de configuração:

Desenvolvimento (dev): utiliza configurações locais, facilitando testes e desenvolvimento.

Produção (prod): as propriedades da aplicação são carregadas a partir de um repositório centralizado de configuração, promovendo maior segurança e padronização.

🏗️ Arquitetura

A arquitetura adotada foi a MVC (Model-View-Controller). Considerando que o sistema é composto por microsserviços bem isolados, optou-se por uma arquitetura mais simples, evitando a complexidade adicional de padrões como Arquitetura Hexagonal, que não se fazem estritamente necessários neste contexto.

🐳 Containerização e Orquestração

Para facilitar a execução e padronizar os ambientes, o projeto utiliza:

Docker: para a criação de imagens dos microsserviços.

Kubernetes: para orquestração, gerenciamento de pods, serviços e escalabilidade da aplicação.

🔄 CI/CD

O projeto conta com um pipeline de CI/CD utilizando GitHub Actions, responsável por:

Realizar o build das aplicações.

Gerar as imagens Docker dos microsserviços.

Publicar as imagens no Docker Hub, automatizando o processo de entrega contínua.

📧 Notificações por E-mail

Os clientes do cinema recebem notificações por e-mail sempre que:

Uma reserva de sessão é concluída com sucesso.

Uma reserva é cancelada.

Isso melhora a experiência do usuário e garante maior transparência nas operações.

🗄️ Persistência de Dados

O projeto utiliza dois tipos de banco de dados, de acordo com a necessidade de cada microsserviço:

MySQL (SQL):

ms-gerenciamento-clientes

ms-gerenciamento-sessoes

MongoDB (NoSQL):

ms-gerenciamento-catalogo

🧬 ORM e Migrations

JPA é utilizada como tecnologia de ORM tanto para o MySQL quanto para o MongoDB.

O versionamento e controle do esquema do banco de dados MySQL é feito por meio de Flyway, garantindo controle de versões e facilidade na evolução das tabelas.

📌 Resumo: Este projeto integra conceitos essenciais de desenvolvimento backend moderno, como microsserviços, persistência poliglota, automação de deploy, orquestração com Kubernetes e boas práticas de versionamento de banco de dados, servindo como um excelente estudo de caso para aplicações distribuídas.
