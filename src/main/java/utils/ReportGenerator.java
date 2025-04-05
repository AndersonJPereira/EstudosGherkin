package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

        List<Map<String, Object>> previousReport = mergedFile.exists()
        	    ? mapper.readValue(mergedFile, new TypeReference<List<Map<String, Object>>>() {})
        	    : new ArrayList<>();

        List<Map<String, Object>> newReport = mapper.readValue(newReportFile, new TypeReference<List<Map<String, Object>>>() {});


        Map<String, Map<String, Object>> scenarioMap = new LinkedHashMap<>();

        // Indexar todos os cenários anteriores
        for (Map<String, Object> feature : previousReport) {
            String uri = (String) feature.get("uri");
            List<Map<String, Object>> elements = (List<Map<String, Object>>) feature.get("elements");
            if (elements == null) continue;
            for (Map<String, Object> scenario : elements) {
                String key = getScenarioKey(uri, scenario);
                scenarioMap.put(key, Map.of("uri", uri, "scenario", scenario));
            }
        }

        // Indexar e sobrescrever com os novos reruns
        for (Map<String, Object> feature : newReport) {
            String uri = (String) feature.get("uri");
            List<Map<String, Object>> elements = (List<Map<String, Object>>) feature.get("elements");
            if (elements == null) continue;
            for (Map<String, Object> scenario : elements) {
                String key = getScenarioKey(uri, scenario);
                scenarioMap.put(key, Map.of("uri", uri, "scenario", scenario));
            }
        }

        // Agrupar por feature
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

        mapper.writerWithDefaultPrettyPrinter().writeValue(mergedFile, new ArrayList<>(featureMap.values()));
        System.out.println("✔️ Merge incremental realizado com sucesso!");
    }

    private static String getScenarioKey(String uri, Map<String, Object> scenario) {
        return uri + ":" + scenario.get("line");
    }
}
