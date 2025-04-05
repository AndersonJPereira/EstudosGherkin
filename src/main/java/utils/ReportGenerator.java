package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ReportGenerator {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("❌ Informe o caminho do novo arquivo JSON.");
            return;
        }

        File newReportFile = new File(args[0]);
        if (!newReportFile.exists()) {
            System.out.println("❌ Arquivo JSON não encontrado: " + args[0]);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        File mergedFile = new File("target/merged-report.json");

        // Carrega o merged atual (se existir)
        List<Map<String, Object>> previousReport = mergedFile.exists()
                ? mapper.readValue(mergedFile, new TypeReference<>() {})
                : new ArrayList<>();

        // Carrega o novo rerun
        List<Map<String, Object>> newReport = mapper.readValue(newReportFile, new TypeReference<>() {});

        // Mapa para consolidar cenários (chave: uri:linha)
        Map<String, Map<String, Object>> scenarioMap = new LinkedHashMap<>();
        Map<String, Map<String, Object>> featureTemplates = new LinkedHashMap<>();

        // Indexa os cenários anteriores
        for (Map<String, Object> feature : previousReport) {
            String uri = (String) feature.get("uri");
            featureTemplates.put(uri, feature);
            List<Map<String, Object>> elements = (List<Map<String, Object>>) feature.get("elements");
            if (elements == null) continue;
            for (Map<String, Object> scenario : elements) {
                String key = getScenarioKey(uri, scenario);
                scenarioMap.put(key, Map.of("uri", uri, "scenario", scenario));
            }
        }

        // Sobrescreve com os novos reruns
        for (Map<String, Object> feature : newReport) {
            String uri = (String) feature.get("uri");
            featureTemplates.put(uri, feature);
            List<Map<String, Object>> elements = (List<Map<String, Object>>) feature.get("elements");
            if (elements == null) continue;
            for (Map<String, Object> scenario : elements) {
                String key = getScenarioKey(uri, scenario);
                scenarioMap.put(key, Map.of("uri", uri, "scenario", scenario));
            }
        }

        // Reconstrói o merged agrupando por feature
        Map<String, Map<String, Object>> mergedFeatures = new LinkedHashMap<>();

        for (Map<String, Object> entry : scenarioMap.values()) {
            String uri = (String) entry.get("uri");
            Map<String, Object> scenario = (Map<String, Object>) entry.get("scenario");

            mergedFeatures.computeIfAbsent(uri, u -> {
                Map<String, Object> original = new LinkedHashMap<>(featureTemplates.get(u));
                original.put("elements", new ArrayList<Map<String, Object>>());
                return original;
            });

            List<Map<String, Object>> elements = (List<Map<String, Object>>) mergedFeatures.get(uri).get("elements");
            elements.add(scenario);
        }

        // Salva o merge final
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(mergedFile, new ArrayList<>(mergedFeatures.values()));

        // Gera HTML (opcional, mas útil para debugging)
        File reportOutputDirectory = new File("target/aggregate-report");
        Configuration config = new Configuration(reportOutputDirectory, "Projeto");
        List<String> jsonFiles = List.of(mergedFile.getAbsolutePath());
        new ReportBuilder(jsonFiles, config).generateReports();

        System.out.println("✅ Merge consolidado com sucesso!");
    }

    private static String getScenarioKey(String uri, Map<String, Object> scenario) {
        return uri + ":" + scenario.get("line");
    }
}
