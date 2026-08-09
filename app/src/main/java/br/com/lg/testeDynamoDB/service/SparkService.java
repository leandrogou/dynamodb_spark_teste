package br.com.lg.testeDynamoDB.service;

import br.com.lg.testeDynamoDB.configuration.properties.DynamoDBProperties;
import br.com.lg.testeDynamoDB.model.TbTesteModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class SparkService {

    private final DynamoDBProperties dynamoDBProperties;

    public void ProcessarComSpark(List<TbTesteModel> testeModels) {
        SparkConf conf = new SparkConf()
                .set("spark.ui.enabled", "false")
                .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .set("spark.kryoserializer.buffer.max", "512m")
                .set("spark.rdd.compress", "false")
                .set("spark.shuffle.compress", "false")
                .set("spark.serializer", "org.apache.spark.serializer.JavaSerializer");

        SparkSession spark = SparkSession.builder()
                .appName("SpringBoot Spark DynamoDB")
                .master("local[*]") // executa localmente
                .config(conf)
                .getOrCreate();
        try {
            // Cria um encoder para o modelo
            Encoder<TbTesteModel> encoder = Encoders.bean(TbTesteModel.class);

            // Cria um Dataset a partir da lista
            Dataset<TbTesteModel> ds = spark.createDataset(testeModels, encoder);

            // Exibe os dados que estão sendo processados (opcional, para depuração)
            //ds.show();

            final DynamoDBProperties writerConfig = dynamoDBProperties.copy();

            ds.foreachPartition(partition -> {
                List<TbTesteModel> batch = new ArrayList<>();
                partition.forEachRemaining(batch::add);
                if (!batch.isEmpty()) {
                    DynamoDBBatchWriter.saveBatchToDynamoDB(batch, writerConfig);
                }
            });

        } catch (Exception e) {
            log.error("❌ Erro ao processar batch: {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            //Fechando o SparkSession após o processamento para liberar recursos
            spark.stop();
        }
    }
}
