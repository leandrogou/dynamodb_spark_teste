# TesteDynamoDB

Projeto de estudo em Java + Spring Boot que demonstra operações básicas com DynamoDB e processamento em lote usando Apache Spark (rodando em modo local dentro da aplicação).

Tecnologias principais
- Java 17
- Spring Boot
- AWS SDK v2 (DynamoDbClient)
- Apache Spark (spark-core, spark-sql) — executado em modo local[*]
- Conector spark-dynamodb (com.audienceproject)

Descrição
Este repositório implementa uma API REST simples para salvar registros em DynamoDB e um fluxo de processamento em lote que usa Spark para agrupar registros e gravá-los em lotes no DynamoDB.

Estrutura relevante
- src/main/java/br/com/lg/testeDynamoDB: código-fonte
  - TesteDynamoDbApplication.java: classe principal
  - configuration/DynamoDBConfiguratiton.java: cria o DynamoDbClient (suporta modo local via `app.local=true`)
  - controller/testeController.java: endpoints REST para testes (`/api/teste/salvar` e `/api/teste/salvarBatch`)
  - repository/RepositoryTeste.java: implementação simples de gravação usando AWS SDK v2
  - service/SparkService.java: executa Spark em `local[*]` e chama DynamoDBBatchWriter para persistência em lote

Pré-requisitos
- Java 17
- Maven
- (Opcional) Docker — para executar LocalStack ou DynamoDB Local

Configuração
As propriedades estão em `src/main/resources/application.properties` e esperam as variáveis de ambiente:
- AWS_ACCESSKEY (ex.: test)
- AWS_SECRETKEY (ex.: test)

Principais propriedades (padrão no arquivo):
- aws.region (padrão: us-east-1)
- aws.dynamodb.endpoint (padrão: http://localhost:8000)
- app.local=true (quando true, o client usa endpointOverride)
- server.port=8082
- server.servlet.context-path=/testeDynamoDB

Executando localmente
1. Levantar um DynamoDB local (duas opções):
   - DynamoDB Local (AWS):
     docker run -p 8000:8000 amazon/dynamodb-local
   - LocalStack (se preferir):
     docker run --rm -p 4566:4566 -e SERVICES=dynamodb localstack/localstack
   Ajuste `aws.dynamodb.endpoint` (ex.: http://localhost:8000 ou http://localhost:4566) e `app.local=true` no `application.properties` ou via variáveis de ambiente.

2. Definir variáveis de ambiente (exemplo Windows PowerShell):
   $env:AWS_ACCESSKEY='test'
   $env:AWS_SECRETKEY='test'

3. Build e execução:
   mvn clean package
   mvn spring-boot:run
   ou
   java -jar target/testeDynamoDB-0.0.1-SNAPSHOT.jar

Endpoints de exemplo
- Salvar um item único:
  GET http://localhost:8082/testeDynamoDB/api/teste/salvar

- Salvar em batch (processado com Spark):
  GET http://localhost:8082/testeDynamoDB/api/teste/salvarBatch?quantidade=100

Observações
- O projeto usa Spark em modo local (`master("local[*]")`). Há um `docker-compose.yml` comentado para alternativamente subir um cluster Spark (não obrigatório para estudo).
- application.properties usa placeholders para AWS_ACCESSKEY/AWS_SECRETKEY — para Local DynamoDB você pode usar valores fictícios.
- A persistência em lote respeita o limite de 25 itens por BatchWrite.

Sugestões
- Para testes rápidos use DynamoDB Local ou LocalStack.
- Ajuste timeouts/retries conforme necessidade ao usar a AWS real.

Feito para estudos com DynamoDB e Java + Spark.
