package context;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * One-time converter: reads all .txt nation files and writes .yaml equivalents.
 * Automatically called on first run if no .yaml files exist.
 */
public class TxtToYamlConverter {

    private static final String[] MONTH_KEYS = {
        "jan", "feb", "mar", "apr", "may", "jun",
        "jul", "aug", "sep", "oct", "nov", "dec"
    };

    public static void convertAll(String basePath) {
        String dataPath = basePath + "/src/data";
        File folder = new File(dataPath);
        File[] txtFiles = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        if (txtFiles == null || txtFiles.length == 0) {
            System.out.println("No .txt files found to convert in " + dataPath);
            return;
        }

        int converted = 0;
        for (File f : txtFiles) {
            System.out.println("Converting: " + f.getName());
            try {
                String yaml = convertFile(f);
                String outPath = dataPath + "/" + f.getName().replace(".txt", ".yaml");
                Files.writeString(Path.of(outPath), yaml);
                converted++;
                System.out.println("  -> Written: " + outPath);
            } catch (Exception e) {
                System.out.println("  ERROR converting " + f.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("Converted " + converted + " files.");
    }

    private static String convertFile(File file) throws IOException {
        // Read entire file into lines, stripping blank lines
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }

        int i = 0;

        // Line 0: nation name
        String nationName = lines.get(i++).trim();

        // Line 1: temperature (12 semicolon-separated values)
        int[] temperature   = parseSemicolonLine(lines.get(i++));
        // Line 2: precipitation
        int[] precipitation = parseSemicolonLine(lines.get(i++));
        // Line 3: shift (single value followed by semicolon)
        int shift = parseSemicolonLine(lines.get(i++))[0];
        // Line 4: wind
        int[] wind          = parseSemicolonLine(lines.get(i++));

        // Skip any blank lines before events
        while (i < lines.size() && lines.get(i).trim().isEmpty()) i++;

        // Events: name line, then "occurs;days;" line, repeat
        List<String>   eventNames   = new ArrayList<>();
        List<int[]>    eventChances = new ArrayList<>();

        while (i < lines.size()) {
            String eventName = lines.get(i++).trim();
            if (eventName.isEmpty()) continue;
            if (i >= lines.size()) break;

            int[] chances = parseSemicolonLine(lines.get(i++));
            if (chances.length < 2) continue;

            eventNames.add(eventName);
            eventChances.add(chances);

            // Skip blank separator lines
            while (i < lines.size() && lines.get(i).trim().isEmpty()) i++;
        }

        // Build YAML
        StringBuilder sb = new StringBuilder();
        sb.append("name: \"").append(nationName.replace("\"", "'")).append("\"\n\n");

        sb.append("temperature:\n");
        for (int m = 0; m < 12; m++)
            sb.append("  ").append(MONTH_KEYS[m]).append(": ").append(temperature[m]).append("\n");

        sb.append("\nprecipitation:\n");
        for (int m = 0; m < 12; m++)
            sb.append("  ").append(MONTH_KEYS[m]).append(": ").append(precipitation[m]).append("\n");

        sb.append("\nshift: ").append(shift).append("\n");

        sb.append("\nwind:\n");
        for (int m = 0; m < 12; m++)
            sb.append("  ").append(MONTH_KEYS[m]).append(": ").append(wind[m]).append("\n");

        if (!eventNames.isEmpty()) {
            sb.append("\nevents:\n");
            for (int e = 0; e < eventNames.size(); e++) {
                sb.append("  - name: \"").append(eventNames.get(e).replace("\"", "'")).append("\"\n");
                sb.append("    occurs: ").append(eventChances.get(e)[0]).append("\n");
                sb.append("    days: ").append(eventChances.get(e)[1]).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Parse a line like "-29;-22;-16;" into an int array.
     */
    private static int[] parseSemicolonLine(String line) {
        String[] parts = line.trim().split(";");
        List<Integer> vals = new ArrayList<>();
        for (String p : parts) {
            p = p.trim();
            if (!p.isEmpty()) {
                try { vals.add(Integer.parseInt(p)); }
                catch (NumberFormatException e) { /* skip */ }
            }
        }
        return vals.stream().mapToInt(Integer::intValue).toArray();
    }
}
