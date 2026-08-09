package br.com.lg.testeDynamoDB.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aws.dynamodb")
public class DynamoDBProperties implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accessKeyId;
    private String secretKey;
    private String region;
    private String endpoint;

    public DynamoDBProperties copy() {
        DynamoDBProperties copy = new DynamoDBProperties();
        copy.setAccessKeyId(this.accessKeyId);
        copy.setSecretKey(this.secretKey);
        copy.setRegion(this.region);
        copy.setEndpoint(this.endpoint);
        return copy;
    }
}
