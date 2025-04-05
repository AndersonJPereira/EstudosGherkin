package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;

public class ReportGenerator {
	
	public static void main(String[] args) throws IOException {
        File reportOutputDirectory = new File("target/aggregate-report");

        ObjectMapper mapper = new ObjectMapper();
        File cucumberJson = new File("target/cucumber-report.json");
        File rerunJson = new File("target/rerun-report.json");

        if (!cucumberJson.exists() && !rerunJson.exists()) {
            System.out.println("❌ Nenhum arquivo JSON encontrado.");
            return;
        }

        // Carrega os arquivos
        List<Map<String, Object>> mainReport = cucumberJson.exists()
                ? mapper.readValue(cucumberJson, new TypeReference<List<Map<String, Object>>>() {})
                : new ArrayList<>();

        List<Map<String, Object>> rerunReport = rerunJson.exists()
                ? mapper.readValue(rerunJson, new TypeReference<List<Map<String, Object>>>() {})
                : new ArrayList<>();

        // Mapear cada cenário por chave única (uri + linha)
        Map<String, Map<String, Object>> scenarioMap = new LinkedHashMap<>();

        for (Map<String, Object> feature : mainReport) {
            String uri = (String) feature.get("uri");
            List<Map<String, Object>> elements = (List<Map<String, Object>>) feature.get("elements");
            if (elements == null) continue;

            for (Map<String, Object> scenario : elements) {
                String key = getScenarioKey(uri, scenario);
                scenarioMap.put(key, Map.of("uri", uri, "scenario", scenario));
            }
        }

        for (Map<String, Object> feature : rerunReport) {
            String uri = (String) feature.get("uri");
            List<Map<String, Object>> elements = (List<Map<String, Object>>) feature.get("elements");
            if (elements == null) continue;

            for (Map<String, Object> scenario : elements) {
                scenario.put("rerun", true); // Marcador
                String key = getScenarioKey(uri, scenario);
                scenarioMap.put(key, Map.of("uri", uri, "scenario", scenario)); // sobrescreve
            }
        }

        // Agrupar novamente os cenários por feature
        Map<String, Map<String, Object>> featureMap = new LinkedHashMap<>();

        for (Map<String, Object> entry : scenarioMap.values()) {
            String uri = (String) entry.get("uri");
            Map<String, Object> scenario = (Map<String, Object>) entry.get("scenario");

            // ⚠️ Adiciona "(REEXECUTADO)" ao nome se for rerun
            if (scenario.containsKey("rerun") && Boolean.TRUE.equals(scenario.get("rerun"))) {
                String originalName = (String) scenario.get("name");
                scenario.put("name", originalName + " (REEXECUTADO)");
            }

            featureMap.computeIfAbsent(uri, k -> {
                Optional<Map<String, Object>> originalFeature =
                        Stream.concat(mainReport.stream(), rerunReport.stream())
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

        // Salvar merge final
        File mergedJson = new File("target/merged-report.json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(mergedJson, new ArrayList<>(featureMap.values()));

        // Gerar relatório com o JSON final
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
}
