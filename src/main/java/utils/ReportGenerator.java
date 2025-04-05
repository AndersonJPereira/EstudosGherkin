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
            System.out.println("❌ Informe o caminho do novo arquivo JSON de rerun.");
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
            ? mapper.readValue(mergedFile, new TypeReference<List<Map<String, Object>>>() {})
            : new ArrayList<>();

        // Carrega o novo rerun
        List<Map<String, Object>> newReport = mapper.readValue(
            newReportFile, new TypeReference<List<Map<String, Object>>>() {}
        );

        // Indexar todos os cenários por uri:line
        Map<String, Map<String, Object>> scenarioMap = new LinkedHashMap<>();
        insertScenarios(previousReport, scenarioMap);
        insertScenarios(newReport, scenarioMap); // sobrescreve se mesmo cenário

        // Agrupar por feature (uri)
        Map<String, List<Map<String, Object>>> groupedByFeature = new LinkedHashMap<>();
        for (Map<String, Object> entry : scenarioMap.values()) {
            String uri = (String) entry.get("uri");
            Map<String, Object> scenario = (Map<String, Object>) entry.get("scenario");

            groupedByFeature.computeIfAbsent(uri, k -> new ArrayList<>()).add(scenario);
        }

        // Criar lista final com as features completas (preservando os campos como name)
        List<Map<String, Object>> mergedResult = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedByFeature.entrySet()) {
            String uri = entry.getKey();
            List<Map<String, Object>> scenarios = entry.getValue();

            Map<String, Object> sourceFeature = findFeatureByUri(uri, newReport, previousReport);

            Map<String, Object> finalFeature = new LinkedHashMap<>();
            finalFeature.putAll(sourceFeature); // preserva name, tags, etc.
            finalFeature.put("elements", scenarios); // atualiza apenas os cenários

            mergedResult.add(finalFeature);
        }

        // Salva JSON consolidado
        mapper.writerWithDefaultPrettyPrinter().writeValue(mergedFile, mergedResult);
        System.out.println("✅ Merge incremental realizado com sucesso!");

        // Gerar relatório agregado
        File reportOutputDirectory = new File("target/aggregate-report");
        Configuration config = new Configuration(reportOutputDirectory, "Estudos-Gherkin");
        config.setBuildNumber("1.0");
        config.addClassifications("Projeto", "Estudos-Gherkin");
        config.addClassifications("Ambiente", "Local");

        ReportBuilder reportBuilder = new ReportBuilder(List.of(mergedFile.getAbsolutePath()), config);
        reportBuilder.generateReports();

        System.out.println("📊 Relatório agregado com merge gerado com sucesso!");
    }

    private static void insertScenarios(List<Map<String, Object>> report,
                                        Map<String, Map<String, Object>> scenarioMap) {
        for (Map<String, Object> feature : report) {
            String uri = (String) feature.get("uri");
            List<Map<String, Object>> elements = (List<Map<String, Object>>) feature.get("elements");
            if (elements == null) continue;
            for (Map<String, Object> scenario : elements) {
                String key = getScenarioKey(uri, scenario);
                scenarioMap.put(key, Map.of("uri", uri, "scenario", scenario));
            }
        }
    }

    private static String getScenarioKey(String uri, Map<String, Object> scenario) {
        return uri + ":" + scenario.get("line");
    }

    private static Map<String, Object> findFeatureByUri(String uri, List<Map<String, Object>>... reports) {
        for (List<Map<String, Object>> report : reports) {
            for (Map<String, Object> feature : report) {
                if (uri.equals(feature.get("uri"))) {
                    return feature;
                }
            }
        }
        // fallback vazio, mas evita null pointer
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("uri", uri);
        fallback.put("name", "Unknown Feature");
        fallback.put("elements", new ArrayList<>());
        return fallback;
    }
}
