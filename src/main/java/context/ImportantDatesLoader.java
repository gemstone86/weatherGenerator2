package context;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

/**
 * Loads religious important dates from important_dates.yaml.
 * Each religion can define holiest, holy, unholy, and unholiest dates.
 */
public class ImportantDatesLoader {

    @SuppressWarnings("unchecked")
    public static List<ReligiousDate> load(String basePath) {
        String path = basePath + "/src/important_dates.yaml";
        File file = new File(path);
        List<ReligiousDate> result = new ArrayList<>();

        if (!file.exists()) {
            Logger.log(LogLevel.INFO, 1, "No important_dates.yaml found — skipping.");
            return result;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(fis);
            List<Map<String, Object>> religions = (List<Map<String, Object>>) data.get("religions");

            for (Map<String, Object> religion : religions) {
                String religionName = (String) religion.get("name");

                // holiest — single entry
                loadSingle(result, religionName, religion, "holiest", ReligiousDate.Tier.HOLIEST);
                // unholiest — single entry
                loadSingle(result, religionName, religion, "unholiest", ReligiousDate.Tier.UNHOLIEST);
                // holy — list
                loadList(result, religionName, religion, "holy_days", ReligiousDate.Tier.HOLY);
                // unholy — list
                loadList(result, religionName, religion, "unholy_days", ReligiousDate.Tier.UNHOLY);
            }

            Logger.log(LogLevel.INFO, 1, "Loaded " + result.size() + " important dates.");
        } catch (IOException e) {
            Logger.log(LogLevel.WARNING, 1, "Error loading important_dates.yaml: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    private static void loadSingle(List<ReligiousDate> result, String religion,
                                   Map<String, Object> data, String key, ReligiousDate.Tier tier) {
        if (!data.containsKey(key)) return;
        Map<String, Object> entry = (Map<String, Object>) data.get(key);
        ReligiousDate rd = parseEntry(religion, entry, tier);
        if (rd != null) result.add(rd);
    }

    @SuppressWarnings("unchecked")
    private static void loadList(List<ReligiousDate> result, String religion,
                                 Map<String, Object> data, String key, ReligiousDate.Tier tier) {
        if (!data.containsKey(key)) return;
        List<Map<String, Object>> entries = (List<Map<String, Object>>) data.get(key);
        for (Map<String, Object> entry : entries) {
            ReligiousDate rd = parseEntry(religion, entry, tier);
            if (rd != null) result.add(rd);
        }
    }

    private static ReligiousDate parseEntry(String religion, Map<String, Object> entry, ReligiousDate.Tier tier) {
        try {
            String name = (String) entry.get("name");
            int month   = Integer.parseInt(entry.get("month").toString().trim());
            int day     = Integer.parseInt(entry.get("day").toString().trim());
            Logger.log(LogLevel.INFO, 2, "  " + religion + " [" + tier + "] " + name + " on " + month + "-" + day);
            return new ReligiousDate(religion, name, month, day, tier);
        } catch (Exception e) {
            Logger.log(LogLevel.WARNING, 1, "Could not parse date entry for " + religion + ": " + e.getMessage());
            return null;
        }
    }
}
