package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;

public class ReportGenerator {

    public static void main(String[] args) throws IOException {
        File reportOutputDirectory = new File("target/aggregate-report");
        ObjectMapper mapper = new ObjectMapper();

        File cucumberJson = new File("target/cucumber-report.json");

        // 🔍 Coleta todos os rerun-report-*.json
        List<File> rerunJsons = Files.list(Paths.get("target"))
                .filter(p -> p.getFileName().toString().startsWith("rerun-report-") && p.toString().endsWith(".json"))
                .map(Path::toFile)
                .collect(Collectors.toList());

        if (!cucumberJson.exists() && rerunJsons.isEmpty()) {
            System.out.println("❌ Nenhum arquivo JSON encontrado.");
            return;
        }

        // Carrega JSON principal
        List<Map<String, Object>> mainReport = cucumberJson.exists()
        	    ? mapper.readValue(cucumberJson, new TypeReference<List<Map<String, Object>>>() {})
        	    : new ArrayList<>();

        // Carrega todos os reruns
        List<Map<String, Object>> allRerunScenarios = new ArrayList<>();
        for (File rerunJson : rerunJsons) {
            List<Map<String, Object>> parsed = mapper.readValue(rerunJson, new TypeReference<List<Map<String, Object>>>() {});
            allRerunScenarios.addAll(parsed);
        }

        // Indexa cenários por chave única e mantém o mais recente
        Map<String, Map<String, Object>> scenarioMap = new LinkedHashMap<>();

        List<Map<String, Object>> allFeatures = new ArrayList<>();
        allFeatures.addAll(mainReport);
        allFeatures.addAll(allRerunScenarios);

        for (Map<String, Object> feature : allFeatures) {
            String uri = (String) feature.get("uri");
            List<Map<String, Object>> elements = (List<Map<String, Object>>) feature.get("elements");
            if (elements == null) continue;

            for (Map<String, Object> scenario : elements) {
                String key = getScenarioKey(uri, scenario);
                Map<String, Object> existing = scenarioMap.get(key);

                if (existing == null || isNewerExecution((Map<String, Object>) existing.get("scenario"), scenario)) {
                    scenarioMap.put(key, Map.of("uri", uri, "scenario", scenario));
                }
            }
        }

        // Agrupa novamente os cenários por feature
        Map<String, Map<String, Object>> featureMap = new LinkedHashMap<>();

        for (Map<String, Object> entry : scenarioMap.values()) {
            String uri = (String) entry.get("uri");
            Map<String, Object> scenario = (Map<String, Object>) entry.get("scenario");

            featureMap.computeIfAbsent(uri, k -> {
                Optional<Map<String, Object>> originalFeature = allFeatures.stream()
                        .filter(f -> uri.equals(f.get("uri")))
                        .findFirst();

                if (originalFeature.isPresent()) {
                    Map<String, Object> copy = new LinkedHashMap<>(originalFeature.get());
                    copy.put("elements", new ArrayList<Map<String, Object>>());
                    return copy;
                }

                Map<String, Object> fallback = new LinkedHashMap<>();
                fallback.put("uri", k);
                fallback.put("elements", new ArrayList<Map<String, Object>>());
                return fallback;
            });

            List<Map<String, Object>> elements = (List<Map<String, Object>>) featureMap.get(uri).get("elements");
            elements.add(scenario);
        }

        // Salva JSON final
        File mergedJson = new File("target/merged-report.json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(mergedJson, new ArrayList<>(featureMap.values()));

        // Gera relatório com merged
        List<String> jsonFiles = List.of(mergedJson.getAbsolutePath());

        Configuration config = new Configuration(reportOutputDirectory, "Estudos-Gherkin");
        config.setBuildNumber("1.0");
        config.addClassifications("Projeto", "Estudos-Gherkin");
        config.addClassifications("Ambiente", "Local");

        ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, config);
        reportBuilder.generateReports();

        System.out.println("✅ Relatório agregado com merge gerado com sucesso!");
    }

    private static String getScenarioKey(String uri, Map<String, Object> scenario) {
        return uri + ":" + scenario.get("line");
    }

    private static boolean isNewerExecution(Map<String, Object> oldScenario, Map<String, Object> newScenario) {
        String oldTimestamp = (String) oldScenario.get("start_timestamp");
        String newTimestamp = (String) newScenario.get("start_timestamp");

        return newTimestamp != null &&
                (oldTimestamp == null || newTimestamp.compareTo(oldTimestamp) > 0);
    }
}
