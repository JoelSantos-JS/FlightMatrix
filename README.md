# FlightMatrix

FlightMatrix é um sistema avançado de monitoramento de passagens aéreas que utiliza tecnologias modernas e inteligência artificial para identificar as melhores oportunidades de compra de passagens aéreas.

## 🚀 Funcionalidades

- **Monitoramento de Preços**: Acompanha continuamente os preços de passagens aéreas em múltiplas fontes
- **Indicador de Calor de Preços**: Utiliza IA para prever a tendência futura dos preços (subir, cair ou permanecer estável)
- **Alertas Personalizados**: Configuração de alertas baseados em critérios específicos (rota, data, preço máximo)
- **Notificações por Email**: Avisos automáticos quando promoções compatíveis são encontradas
- **Resumos Diários**: Compilação das melhores ofertas enviadas periodicamente
- **API RESTful**: Acesso programático a todas as funcionalidades do sistema

## 📊 Indicador de Calor de Preços

Um diferencial do FlightMatrix é o indicador de calor de preços, que usa análise estatística para prever tendências:

- 🔥🔥 **Muito Quente**: Preço deve subir significativamente (>10%)
- 🔥 **Quente**: Preço tende a subir moderadamente (5-10%)
- ⚖️ **Neutro**: Preço deve permanecer estável (±5%)
- ❄️ **Frio**: Preço tende a cair moderadamente (5-10%)
- ❄️❄️ **Muito Frio**: Preço deve cair significativamente (>10%)

## 🛠️ Tecnologias Utilizadas

- **Backend**: Java 17, Spring Boot 3.2
- **Persistência**: Spring Data JPA, PostgreSQL, H2 (desenvolvimento)
- **Email**: Spring Mail, Thymeleaf
- **Agendamento**: Spring Scheduling
- **HTTP/APIs**: OkHttp, Retrofit
- **Web Scraping**: JSoup
- **Modelagem Estatística**: Apache Commons Math

## 📋 Requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (Produção)

## 🚀 Instalação e Execução

### Clonando o repositório
```bash
git clone https://github.com/seuusuario/flightmatrix.git
cd flightmatrix
```

### Configurando o ambiente
Crie um arquivo `application-dev.properties` na pasta `src/main/resources` com suas configurações:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/flightmatrix
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

# Email (opcional para desenvolvimento)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=seu-email@gmail.com
spring.mail.password=sua-senha-app
```

### Compilando e executando
```bash
mvn clean install
mvn spring-boot:run -Dspring.profiles.active=dev
```

### Usando Docker (opcional)
```bash
docker-compose up -d
```

## 📄 API

A API está documentada com OpenAPI e pode ser acessada em:
- `/swagger-ui.html` - Interface interativa
- `/v3/api-docs` - Especificação OpenAPI em JSON

### Endpoints principais:

- `GET /api/passagens/buscar` - Busca passagens por rota e data
- `GET /api/price-predictions/passagem/{id}` - Obtém previsão de preço para uma passagem
- `POST /api/alertas/usuario/{usuarioId}` - Cria um novo alerta
- `GET /api/alertas/usuario/{usuarioId}` - Lista alertas de um usuário

## 🔗 Estrutura do Projeto

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── flightmatrix/
│   │           ├── adapter/     # Adaptadores para diferentes fontes de passagens
│   │           ├── config/      # Configurações do Spring
│   │           ├── controller/  # Controladores REST
│   │           ├── dto/         # Objetos de transferência de dados
│   │           ├── exception/   # Tratamento de exceções
│   │           ├── model/       # Entidades e modelos
│   │           ├── repository/  # Repositórios JPA
│   │           ├── service/     # Lógica de negócios
│   │           └── scheduler/   # Agendadores de tarefas
│   └── resources/
│       ├── application.properties
│       └── templates/           # Templates de email
└── test/                        # Testes unitários e de integração
```

## 🤝 Contribuindo

1. Faça o fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. Faça commit das alterações (`git commit -m 'Adiciona nova funcionalidade'`)
4. Faça push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## 📝 Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 📧 Contato

Para dúvidas ou sugestões, entre em contato:
- Email: joeltere9@gmail.com
- GitHub: [joelsantos-js](https://github.com/joelsantos-js)
