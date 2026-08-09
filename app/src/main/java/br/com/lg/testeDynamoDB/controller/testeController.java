package br.com.lg.testeDynamoDB.controller;

import br.com.lg.testeDynamoDB.model.TbTesteModel;
import br.com.lg.testeDynamoDB.repository.RepositoryTeste;
import br.com.lg.testeDynamoDB.service.SparkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teste/")
@Slf4j
public class testeController {
    private final RepositoryTeste repositoryTeste;
    private final SparkService service;

    @GetMapping("salvar")
    public String salvar() {
        long startTime = System.currentTimeMillis();
        TbTesteModel testeModel = new TbTesteModel();

        testeModel.setCodTeste(UUID.randomUUID().toString());
        testeModel.setSK("TESTE#PARAM");
        testeModel.setNome("Teste");
        testeModel.setDescricao("Descrição do Teste");
        //Isso é apenas um teste, O certo é chamar uma service, colocar a regras de negocio lá e de lá
        //chamar a repository...
        repositoryTeste.save(testeModel);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("Tempo de execução: {} ms para salvar {} registro.", duration, 1);
        return "Salvo com sucesso!";
    }

    @GetMapping("salvarBatch")
    public String salvarBatch(@RequestParam int quantidade) {
        List<TbTesteModel> testeModels = new ArrayList<>();
        int qtd = quantidade;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < qtd; i++) {
            TbTesteModel testeModel = new TbTesteModel();
            testeModel.setCodTeste(UUID.randomUUID().toString());
            testeModel.setSK("TESTE#PARAM");
            testeModel.setNome("Nome Teste " + i);
            testeModel.setDescricao("Descrição do Teste "+i);
            testeModels.add(testeModel);
        }
        //repositoryTeste.saveAll(testeModels);
        service.ProcessarComSpark(testeModels);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("Tempo de execução: {} ms para salvar {} registros.", duration, qtd);
        return "Salvo com sucesso!";
    }
}
