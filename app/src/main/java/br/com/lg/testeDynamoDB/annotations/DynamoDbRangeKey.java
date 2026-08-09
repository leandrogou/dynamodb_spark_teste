package br.com.lg.testeDynamoDB.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DynamoDbRangeKey {
    String value(); // nome da coluna
}
