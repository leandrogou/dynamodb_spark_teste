package br.com.lg.testeDynamoDB.repository;

import br.com.lg.testeDynamoDB.annotations.DynamoDbAttribute;
import br.com.lg.testeDynamoDB.annotations.DynamoDbHashKey;
import br.com.lg.testeDynamoDB.annotations.DynamoDbTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Log4j2
public class RepositoryTeste {

    private final DynamoDbClient client;


    public <T> void save(T entity) {
        Map<String, AttributeValue> item = toItem(entity);
        String tableName = getTableName(entity.getClass());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        client.putItem(request);
    }

    public <T> void saveAll(List<T> entities) {
        if (entities.isEmpty()) return;

        String tableName = getTableName(entities.get(0).getClass());
        List<WriteRequest> writeRequests = new ArrayList<>();

        for (T entity : entities) {
            Map<String, AttributeValue> item = toItem(entity);
            writeRequests.add(WriteRequest.builder()
                    .putRequest(PutRequest.builder().item(item).build())
                    .build());

            // Limite de 25 itens por batch
            if (writeRequests.size() == 25) {
                client.batchWriteItem(
BatchWriteItemRequest.builder()
                        .requestItems(Map.of(tableName, writeRequests))
                        .build()
                );
                //batchWrite(tableName, writeRequests);
                writeRequests.clear();
            }
        }

        // Escreve os itens restantes
        if (!writeRequests.isEmpty()) {
            client.batchWriteItem(
                    BatchWriteItemRequest.builder()
                            .requestItems(Map.of(tableName, writeRequests))
                            .build()
            );
            //batchWrite(tableName, writeRequests);
        }
    }

    public <T> T load(Class<T> clazz, String hashKeyValue) {
        String tableName = getTableName(clazz);
        String hashKeyName = getHashKeyName(clazz);

        Map<String, AttributeValue> key = new HashMap<>();
        key.put(hashKeyName, AttributeValue.builder().s(hashKeyValue).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        GetItemResponse response = client.getItem(request);
        if (response.hasItem()) {
            return fromItem(clazz, response.item());
        }
        return null;
    }

    // Métodos auxiliares
    private <T> Map<String, AttributeValue> toItem(T entity) {
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

    private <T> T fromItem(Class<T> clazz, Map<String, AttributeValue> item) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String columnName = getColumnName(field);
                if (item.containsKey(columnName)) {
                    field.set(instance, item.get(columnName).s());
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getTableName(Class<?> clazz) {
        DynamoDbTable annotation = clazz.getAnnotation(DynamoDbTable.class);
        if (annotation == null) throw new RuntimeException("Classe sem @DynamoDbTable");
        return annotation.value();
    }

    private String getHashKeyName(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(DynamoDbHashKey.class)) {
                return field.getAnnotation(DynamoDbHashKey.class).value();
            }
        }
        throw new RuntimeException("Nenhum campo com @DynamoDbHashKey");
    }

    private String getColumnName(Field field) {
        if (field.isAnnotationPresent(DynamoDbHashKey.class)) {
            return field.getAnnotation(DynamoDbHashKey.class).value();
        }
        if (field.isAnnotationPresent(DynamoDbAttribute.class)) {
            return field.getAnnotation(DynamoDbAttribute.class).value();
        }
        return field.getName();
    }

//    public final DynamoDBMapper dynamoDBMapper;
//
//    public void save(tbTesteModel testeModel) {
//        log.info("Salvando item: codTeste={}, SK={}", testeModel.getCodTeste(), testeModel.getSK());
//        dynamoDBMapper.save(testeModel);
//    }
//
//    public tbTesteModel findById(String codTeste, String SK) {
//        return dynamoDBMapper.load(tbTesteModel.class, codTeste, SK);
//    }
//
//    public void saveAll(List<tbTesteModel> testeModels) {
//        final int[] qtd = {0};
//        List<tbTesteModel> chunck = new ArrayList();
//
//        testeModels.forEach(tbTesteModel -> {
//            if (qtd[0] < 25) {
//                chunck.add(tbTesteModel);
//                qtd[0]++;
//            } else {
//                dynamoDBMapper.batchSave(chunck);
//                chunck.clear();
//                qtd[0] = 0;
//            }
//        });
//        if(!chunck.isEmpty() && qtd[0] > 0){
//            dynamoDBMapper.batchSave(chunck);
//        }
//    }
}
