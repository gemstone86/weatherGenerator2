package context;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Manages campaign notes stored as YAML files under src/comments/campaigns/.
 * Each campaign is a separate file: <campaign-name>.yaml
 * Keys are date strings (e.g. "2977-7-1"), values are note strings.
 *
 * The filename is always identical to the campaign name (with .yaml appended).
 * Only path separators and null bytes are stripped to prevent directory traversal.
 * This ensures listCampaigns() always returns names that match the dropdown exactly.
 */
public class CampaignLoader {

    private final String campaignDir;

    public CampaignLoader(String basePath) {
        this.campaignDir = basePath + "/src/comments/campaigns";
    }

    // ── Campaign listing ──────────────────────────────────────────────────

    /** Returns all campaign names (filenames without .yaml extension), sorted. */
    public List<String> listCampaigns() {
        File dir = new File(campaignDir);
        List<String> names = new ArrayList<>();
        if (!dir.exists()) return names;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".yaml"));
        if (files == null) return names;
        for (File f : files)
            names.add(f.getName().substring(0, f.getName().length() - 5)); // strip ".yaml"
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    /** Creates a new empty campaign file. Returns false if name already exists. */
    public boolean createCampaign(String name) {
        ensureDir();
        File file = campaignFile(name);
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

    // ── Load / Save ───────────────────────────────────────────────────────

    /** Loads all notes for a campaign. Returns empty map if not found. */
    public HashMap<String, String> load(String campaignName) {
        HashMap<String, String> notes = new HashMap<>();
        if (campaignName == null || campaignName.isBlank()) return notes;

        File file = campaignFile(campaignName);
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

        File source = campaignFile(campaignName);
        File backup = campaignBakFile(campaignName);

        if (source.exists()) {
            try {
                backup.getParentFile().mkdirs();
                Files.copy(source.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Logger.log(LogLevel.WARNING, 1, "Campaign backup failed: " + e.getMessage());
            }
        }

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
            Files.writeString(source.toPath(), sb.toString());
            Logger.log(LogLevel.INFO, 1, "Saved " + toSave.size() + " campaign notes for: " + campaignName);
        } catch (IOException e) {
            Logger.log(LogLevel.WARNING, 1, "Error saving campaign notes: " + e.getMessage());
        }
    }

    /** Saves the current date for a campaign so it can be restored on switch. */
    public void saveCampaignSession(String campaignName, int year, int month, int day) {
        if (campaignName == null || campaignName.isBlank()) return;
        File file = campaignSessionFile(campaignName);
        try {
            file.getParentFile().mkdirs();
            Files.writeString(file.toPath(),
                "year: " + year + "\nmonth: " + month + "\nday: " + day + "\n");
        } catch (IOException e) {
            Logger.log(LogLevel.WARNING, 1, "Error saving campaign session: " + e.getMessage());
        }
    }

    /**
     * Loads the saved date for a campaign.
     * Returns int[]{year, month, day} or null if no session file exists.
     */
    public int[] loadCampaignSession(String campaignName) {
        if (campaignName == null || campaignName.isBlank()) return null;
        File file = campaignSessionFile(campaignName);
        if (!file.exists()) return null;
        int year = -1, month = -1, day = -1;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(":\\s*", 2);
                if (parts.length != 2) continue;
                try {
                    int v = Integer.parseInt(parts[1].trim());
                    switch (parts[0].trim()) {
                        case "year"  -> year  = v;
                        case "month" -> month = v;
                        case "day"   -> day   = v;
                    }
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException e) {
            Logger.log(LogLevel.WARNING, 1, "Error loading campaign session: " + e.getMessage());
        }
        if (year < 0 || month < 0 || day < 0) return null;
        return new int[]{year, month, day};
    }

    /** Renames a campaign by moving its file. Returns false if newName already exists. */
    public boolean renameCampaign(String oldName, String newName) {
        File oldFile = campaignFile(oldName);
        File newFile = campaignFile(newName);
        if (newFile.exists()) {
            Logger.log(LogLevel.WARNING, 1, "Rename failed — target already exists: " + newName);
            return false;
        }
        boolean ok = oldFile.renameTo(newFile);
        if (ok) {
            // Also move session file if it exists
            File oldSession = campaignSessionFile(oldName);
            File newSession = campaignSessionFile(newName);
            if (oldSession.exists()) oldSession.renameTo(newSession);
            Logger.log(LogLevel.INFO, 1, "Renamed campaign: " + oldName + " → " + newName);
        } else {
            Logger.log(LogLevel.WARNING, 1, "Rename failed for: " + oldName);
        }
        return ok;
    }

    /**
     * Deletes a campaign by moving its file to bak/ as a .deleted file.
     * The session file is also removed.
     */
    public void deleteCampaign(String campaignName) {
        File source  = campaignFile(campaignName);
        File removed = new File(campaignDir + "/bak/" + safe(campaignName) + ".removed");
        if (source.exists()) {
            try {
                removed.getParentFile().mkdirs();
                boolean moved = false;
                try {
                    Files.move(source.toPath(), removed.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    moved = true;
                } catch (IOException ex) {
                    Logger.log(LogLevel.WARNING, 1, "Files.move failed, trying renameTo: " + ex.getMessage());
                    moved = source.renameTo(removed);
                }
                if (moved) {
                    Logger.log(LogLevel.INFO, 1, "Archived deleted campaign to: " + removed.getName());
                } else {
                    Logger.log(LogLevel.WARNING, 1, "Could not move campaign file to bak: " + source.getAbsolutePath());
                }
            } catch (Exception e) {
                Logger.log(LogLevel.WARNING, 1, "Could not archive campaign file: " + e.getMessage());
            }
        } else {
            Logger.log(LogLevel.WARNING, 1, "deleteCampaign: source file not found: " + source.getAbsolutePath());
        }
        File session = campaignSessionFile(campaignName);
        if (session.exists()) session.delete();
    }

    private File campaignFile(String name) {
        return new File(campaignDir + "/" + safe(name) + ".yaml");
    }

    private File campaignBakFile(String name) {
        return new File(campaignDir + "/bak/" + safe(name) + "-bak.yaml");
    }

    private File campaignSessionFile(String name) {
        return new File(campaignDir + "/bak/" + safe(name) + "-session.yaml");
    }

    /**
     * Strips only path-traversal characters (/ \ and null bytes).
     * Spaces, Swedish chars, dots, etc. are kept so the filename
     * matches the display name exactly.
     */
    private String safe(String name) {
        return name.replaceAll("[/\\\\\0]", "").trim();
    }

    private void ensureDir() {
        File dir = new File(campaignDir);
        if (!dir.exists()) dir.mkdirs();
    }
}