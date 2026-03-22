package context;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Manages campaign notes stored as YAML files under src/comments/campaigns/.
 * Each campaign is a separate file: <campaign-name>.yaml
 * Keys are date strings (e.g. "2977-7-1"), values are note strings.
 * Same parse strategy as CommentLoader to avoid SnakeYAML date-parsing issues.
 */
public class CampaignLoader {

    private final String campaignDir;

    public CampaignLoader(String basePath) {
        this.campaignDir = basePath + "/src/comments/campaigns";
    }

    // ── Campaign listing ─────────────────────────────────────────────────

    /** Returns all campaign names (filenames without .yaml), sorted. */
    public List<String> listCampaigns() {
        File dir = new File(campaignDir);
        List<String> names = new ArrayList<>();
        if (!dir.exists()) return names;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".yaml"));
        if (files == null) return names;
        for (File f : files)
            names.add(f.getName().replace(".yaml", ""));
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    /** Creates a new empty campaign file. Returns false if name already exists. */
    public boolean createCampaign(String name) {
        ensureDir();
        File file = new File(campaignDir + "/" + sanitize(name) + ".yaml");
        if (file.exists()) return false;
        try {
            file.createNewFile();
            Logger.log(LogLevel.INFO, 1, "Created campaign: " + name);
            return true;
        } catch (IOException e) {
            Logger.log(LogLevel.WARNING, 1, "Could not create campaign file: " + e.getMessage());
            return false;
        }
    }

    // ── Load / Save ──────────────────────────────────────────────────────

    /** Loads all notes for a campaign. Returns empty map if not found. */
    public HashMap<String, String> load(String campaignName) {
        HashMap<String, String> notes = new HashMap<>();
        if (campaignName == null || campaignName.isBlank()) return notes;

        File file = new File(campaignDir + "/" + sanitize(campaignName) + ".yaml");
        if (!file.exists()) return notes;

        Pattern pattern = Pattern.compile("^\"?([\\w\\-]+)\"?:\\s*(.*)$");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String currentKey = null;
            StringBuilder currentVal = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("  ") || line.startsWith("\t")) {
                    if (currentKey != null) {
                        if (currentVal.length() > 0) currentVal.append("\n");
                        currentVal.append(line.stripLeading());
                    }
                    continue;
                }
                if (currentKey != null) {
                    String val = currentVal.toString().trim();
                    if (!val.isEmpty()) notes.put(currentKey, val);
                    currentKey = null;
                    currentVal.setLength(0);
                }
                line = line.trim();
                if (line.isEmpty()) continue;
                Matcher m = pattern.matcher(line);
                if (m.matches()) {
                    currentKey = m.group(1).trim();
                    String val = m.group(2).trim();
                    if (!val.equals("|")) currentVal.append(val);
                }
            }
            if (currentKey != null) {
                String val = currentVal.toString().trim();
                if (!val.isEmpty()) notes.put(currentKey, val);
            }
            Logger.log(LogLevel.INFO, 1, "Loaded " + notes.size() + " campaign notes for: " + campaignName);
        } catch (IOException e) {
            Logger.log(LogLevel.WARNING, 1, "Error loading campaign notes: " + e.getMessage());
        }
        return notes;
    }

    /** Saves all notes for a campaign, creating a backup first. */
    public void save(String campaignName, HashMap<String, String> notes) {
        if (campaignName == null || campaignName.isBlank()) return;
        ensureDir();

        String filePath   = campaignDir + "/" + sanitize(campaignName) + ".yaml";
        String backupPath = campaignDir + "/" + sanitize(campaignName) + "-bak.yaml";

        // Backup existing file
        File source = new File(filePath);
        if (source.exists()) {
            try {
                Files.copy(source.toPath(), Path.of(backupPath), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Logger.log(LogLevel.WARNING, 1, "Campaign backup failed: " + e.getMessage());
            }
        }

        // Write
        Map<String, String> toSave = new HashMap<>();
        for (Map.Entry<String, String> entry : notes.entrySet())
            if (entry.getValue() != null && !entry.getValue().trim().isEmpty())
                toSave.put(entry.getKey(), entry.getValue());

        try {
            StringBuilder sb = new StringBuilder();
            toSave.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    String val = e.getValue().trim();
                    if (val.contains("\n")) {
                        sb.append("\"").append(e.getKey()).append("\": |\n");
                        for (String ln : val.split("\n", -1))
                            sb.append("  ").append(ln).append("\n");
                    } else {
                        sb.append("\"").append(e.getKey()).append("\": ").append(val).append("\n");
                    }
                });
            Files.writeString(Path.of(filePath), sb.toString());
            Logger.log(LogLevel.INFO, 1, "Saved " + toSave.size() + " campaign notes for: " + campaignName);
        } catch (IOException e) {
            Logger.log(LogLevel.WARNING, 1, "Error saving campaign notes: " + e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /** Strips characters that are unsafe in filenames. */
    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-åäöÅÄÖ ]", "").trim().replace(" ", "_");
    }

    private void ensureDir() {
        File dir = new File(campaignDir);
        if (!dir.exists()) dir.mkdirs();
    }
}