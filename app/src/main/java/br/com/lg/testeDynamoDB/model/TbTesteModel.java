package br.com.lg.testeDynamoDB.model;

import br.com.lg.testeDynamoDB.annotations.DynamoDbAttribute;
import br.com.lg.testeDynamoDB.annotations.DynamoDbHashKey;
import br.com.lg.testeDynamoDB.annotations.DynamoDbRangeKey;
import br.com.lg.testeDynamoDB.annotations.DynamoDbTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbTable("tblg_teste")
public class TbTesteModel implements java.io.Serializable {
    @DynamoDbHashKey("cod_teste")
    private String codTeste;

    @DynamoDbRangeKey("SK")
    private String SK;

    @DynamoDbAttribute("nome")
    private String nome;

    @DynamoDbAttribute("descricao")
    private String descricao;
}
