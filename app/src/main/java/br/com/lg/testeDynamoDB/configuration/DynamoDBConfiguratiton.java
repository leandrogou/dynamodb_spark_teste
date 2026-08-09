package br.com.lg.testeDynamoDB.configuration;

import br.com.lg.testeDynamoDB.configuration.properties.DynamoDBProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Slf4j
@Configuration
public class DynamoDBConfiguratiton {
    @Value("${app.local:false}")
    private Boolean isLocal;

    @Autowired
    private DynamoDBProperties dynamoDBProperties;

    @Bean
    @Primary
    public DynamoDbClient amazonDynamoDBClient() {

        log.info("selected region for dynamodb is {}", dynamoDBProperties.getRegion());
        DynamoDbClient client;
        if (isLocal) {
            log.info("configuring local dynamodb client with endpoint {}", dynamoDBProperties.getEndpoint());
            client = DynamoDbClient.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(dynamoDBProperties.getAccessKeyId(), dynamoDBProperties.getSecretKey())))
                    .endpointOverride(java.net.URI.create(dynamoDBProperties.getEndpoint()))
                    .region(software.amazon.awssdk.regions.Region.of(dynamoDBProperties.getRegion()))
                    .build();
        } else {
            log.info("configuring aws dynamodb client");
            client = DynamoDbClient.builder()
                    .region(software.amazon.awssdk.regions.Region.of(dynamoDBProperties.getRegion()))
                    .build();
        }
        return client;
    }
}
