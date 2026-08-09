package br.com.lg.testeDynamoDB.service;

import br.com.lg.testeDynamoDB.configuration.properties.DynamoDBProperties;
import br.com.lg.testeDynamoDB.model.TbTesteModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynamoDBBatchWriter {

    static void saveBatchToDynamoDB(List<TbTesteModel> batch, DynamoDBProperties props) {
        if (props == null) {
            throw new IllegalArgumentException("DynamoDBProperties não pode ser nulo");
        }

        try (DynamoDbClient client = DynamoDbClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.getAccessKeyId(), props.getSecretKey())))
                .endpointOverride(URI.create(props.getEndpoint()))
                .region(Region.of(props.getRegion()))
                .build()) {

            // use o client aqui
            String tableName = getTableName(batch.get(0).getClass());
            List<WriteRequest> writeRequests = new ArrayList<>();

            for (TbTesteModel entity : batch) {
                Map<String, AttributeValue> item = toItem(entity);
                writeRequests.add(WriteRequest.builder()
                        .putRequest(PutRequest.builder().item(item).build())
                        .build());
            }
            // Quebra em sublistas de até 25
            int batchSize = 25;

            for (int i = 0; i < writeRequests.size(); i += batchSize) {
                int end = Math.min(i + batchSize, writeRequests.size());
                List<WriteRequest> subList = writeRequests.subList(i, end);

                BatchWriteItemRequest batchRequest = BatchWriteItemRequest.builder()
                        .requestItems(Map.of(tableName, subList))
                        .build();

                BatchWriteItemResponse response = client.batchWriteItem(batchRequest);

                // Reprocessa itens não processados
                Map<String, List<WriteRequest>> unprocessed = response.unprocessedItems();
                int qtdReprocessamentos = 0;
                while (!unprocessed.isEmpty()) {
                    Thread.sleep((qtdReprocessamentos +1) * 1000L); // Espera qtdReprocessamentos + 1 segundos antes de tentar novamente
                    log.info("⚠️ Reprocessando {} itens não processados...", unprocessed.size());
                    BatchWriteItemRequest retryRequest = BatchWriteItemRequest.builder()
                            .requestItems(unprocessed)
                            .build();
                    response = client.batchWriteItem(retryRequest);
                    unprocessed = response.unprocessedItems();
                    if (qtdReprocessamentos >= 5) {
                        throw new RuntimeException("Erro após 5 tentativas de reprocessamento");
                    }
                    qtdReprocessamentos ++;
                }

                log.info("✅ Persistidos {} registros no DynamoDB", subList.size());
            }
        } catch (Exception e) {
            log.error("❌ Erro ao persistir lote: {}", e.getMessage());
        }
    }

    private static <T> Map<String, AttributeValue> toItem(T entity) {
        Map<String, AttributeValue> item = new HashMap<>();
        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (value != null) {
                    String columnName = getColumnName(field);
                    item.put(columnName, AttributeValue.builder().s(value.toString()).build());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return item;
    }

    private static String getTableName(Class<?> clazz) {
        br.com.lg.testeDynamoDB.annotations.DynamoDbTable annotation = clazz.getAnnotation(br.com.lg.testeDynamoDB.annotations.DynamoDbTable.class);
        if (annotation == null) throw new RuntimeException("Classe sem @DynamoDbTable");
        return annotation.value();
    }

    private static String getColumnName(Field field) {
        if (field.isAnnotationPresent(br.com.lg.testeDynamoDB.annotations.DynamoDbHashKey.class)) {
            return field.getAnnotation(br.com.lg.testeDynamoDB.annotations.DynamoDbHashKey.class).value();
        }
        if (field.isAnnotationPresent(br.com.lg.testeDynamoDB.annotations.DynamoDbAttribute.class)) {
            return field.getAnnotation(br.com.lg.testeDynamoDB.annotations.DynamoDbAttribute.class).value();
        }
        return field.getName();
    }
}
