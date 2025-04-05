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

        // Carrega o JSON existente (se houver)
        List<Map<String, Object>> previousReport = mergedFile.exists()
                ? mapper.readValue(mergedFile, new TypeReference<List<Map<String, Object>>>() {})
                : new ArrayList<>();

        // Carrega o novo rerun
        List<Map<String, Object>> newReport = mapper.readValue(newReportFile, new TypeReference<List<Map<String, Object>>>() {});

        // Cria um map para agrupar os cenários
        Map<String, Map<String, Object>> scenarioMap = new LinkedHashMap<>();

        // Indexa os anteriores
        for (Map<String, Object> feature : previousReport) {
            String uri = (String) feature.get("uri");
            List<Map<String, Object>> elements = (List<Map<String, Object>>) feature.get("elements");
            if (elements == null) continue;

            for (Map<String, Object> scenario : elements) {
                String key = getScenarioKey(uri, scenario);
                scenarioMap.put(key, Map.of("uri", uri, "scenario", scenario));
            }
        }

        // Indexa os reruns e sobrescreve se necessário
        for (Map<String, Object> feature : newReport) {
            String uri = (String) feature.get("uri");
            List<Map<String, Object>> elements = (List<Map<String, Object>>) feature.get("elements");
            if (elements == null) continue;

            for (Map<String, Object> scenario : elements) {
                String key = getScenarioKey(uri, scenario);
                scenarioMap.put(key, Map.of("uri", uri, "scenario", scenario));
            }
        }

        // Reorganiza em estrutura por feature
        Map<String, Map<String, Object>> featureMap = new LinkedHashMap<>();
        for (Map<String, Object> entry : scenarioMap.values()) {
            String uri = (String) entry.get("uri");
            Map<String, Object> scenario = (Map<String, Object>) entry.get("scenario");

            featureMap.computeIfAbsent(uri, u -> {
                Map<String, Object> feature = new LinkedHashMap<>();
                feature.put("uri", u);
                feature.put("elements", new ArrayList<Map<String, Object>>());
                return feature;
            });

            List<Map<String, Object>> elements = (List<Map<String, Object>>) featureMap.get(uri).get("elements");
            elements.add(scenario);
        }

        // Salva JSON consolidado
        mapper.writerWithDefaultPrettyPrinter().writeValue(mergedFile, new ArrayList<>(featureMap.values()));
        System.out.println("✔️ Merge incremental realizado com sucesso!");

        // Gera o relatório HTML usando Cucumber ReportBuilder
        File reportOutputDirectory = new File("target/aggregate-report");
        List<String> jsonFiles = List.of(mergedFile.getAbsolutePath());

        Configuration config = new Configuration(reportOutputDirectory, "Estudos-Gherkin");
        config.setBuildNumber("1.0");
        config.addClassifications("Projeto", "Estudos-Gherkin");
        config.addClassifications("Ambiente", "Local");

        ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, config);
        reportBuilder.generateReports();

        System.out.println("📊 Relatório HTML agregado gerado com sucesso!");
    }

    private static String getScenarioKey(String uri, Map<String, Object> scenario) {
        return uri + ":" + scenario.get("line");
    }
}
