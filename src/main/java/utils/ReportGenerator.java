package utils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;

public class ReportGenerator {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Informe o caminho do novo arquivo JSON de rerun.");
            return;
        }

        File newReportFile = new File(args[0]);
        if (!newReportFile.exists()) {
            System.out.println("Arquivo JSON não encontrado: " + args[0]);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        File mergedFile = new File("target/merged-report.json");

        // Carrega os arquivos antigos e novos
        List<Map<String, Object>> previousReport = mergedFile.exists()
                ? mapper.readValue(mergedFile, new TypeReference<List<Map<String, Object>>>() {})
                : new ArrayList<>();

        List<Map<String, Object>> newReport = mapper.readValue(newReportFile, new TypeReference<List<Map<String, Object>>>() {});

        // Mapa com chave única do cenário (uri:line) e cenário atualizado
        Map<String, Map<String, Object>> scenarioMap = new LinkedHashMap<>();

        // Indexa cenários antigos
        for (Map<String, Object> feature : previousReport) {
            String uri = (String) feature.get("uri");
            List<Map<String, Object>> elements = (List<Map<String, Object>>) feature.get("elements");
            if (elements == null) continue;
            for (Map<String, Object> scenario : elements) {
                String key = getScenarioKey(uri, scenario);
                scenarioMap.put(key, Map.of("uri", uri, "scenario", scenario));
            }
        }

        // Sobrescreve com cenários novos
        for (Map<String, Object> feature : newReport) {
            String uri = (String) feature.get("uri");
            List<Map<String, Object>> elements = (List<Map<String, Object>>) feature.get("elements");
            if (elements == null) continue;
            for (Map<String, Object> scenario : elements) {
                String key = getScenarioKey(uri, scenario);
                scenarioMap.put(key, Map.of("uri", uri, "scenario", scenario));
            }
        }

        // Agrupa os cenários novamente por feature
        Map<String, List<Map<String, Object>>> groupedByFeature = new LinkedHashMap<>();
        for (Map<String, Object> entry : scenarioMap.values()) {
            String uri = (String) entry.get("uri");
            Map<String, Object> scenario = (Map<String, Object>) entry.get("scenario");
            groupedByFeature.computeIfAbsent(uri, k -> new ArrayList<>()).add(scenario);
        }

        List<Map<String, Object>> mergedReport = groupedByFeature.entrySet().stream().map(entry -> {
            Map<String, Object> feature = new LinkedHashMap<>();
            feature.put("uri", entry.getKey());
            feature.put("elements", entry.getValue());
            return feature;
        }).collect(Collectors.toList());

        // Salva arquivo final
        mapper.writerWithDefaultPrettyPrinter().writeValue(mergedFile, mergedReport);
        System.out.println("✅ Merge incremental realizado com sucesso!");

        // Gera relatório HTML
        File reportOutputDirectory = new File("target/aggregate-report");
        List<String> jsonFiles = List.of(mergedFile.getAbsolutePath());

        Configuration config = new Configuration(reportOutputDirectory, "Estudos-Gherkin");
        config.setBuildNumber("1.0");
        config.addClassifications("Projeto", "Estudos-Gherkin");
        config.addClassifications("Ambiente", "Local");

        ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, config);
        reportBuilder.generateReports();
    }

    private static String getScenarioKey(String uri, Map<String, Object> scenario) {
        return uri + ":" + scenario.get("line");
    }
}
