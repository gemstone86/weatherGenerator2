package context;

import gui.GuiApp;
import gui.Localization;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Saves and loads day comments to/from comments.yaml
 * Keys are date strings (e.g. "2977-7-1"), values are comment strings.
 * Creates a backup of the previous comments.yaml to comments-bak.yaml before each save.
 */
public class CommentLoader {

    private final String commentsPath;
    private final String commentsBackupPath;
    private final String sessionPath;
    private GuiApp guiApp;

    public CommentLoader(String basePath) {
        this.commentsPath       = basePath + "/src/comments/comments.yaml";
        Logger.log(LogLevel.INFO, 2, "Path to comments is: " +this.commentsPath);
        this.commentsBackupPath = basePath + "/src/comments/bak/comments-bak.yaml";
        Logger.log(LogLevel.INFO, 2, "Backup Path is: " + this.commentsBackupPath);
        this.sessionPath        = basePath + "/src/comments/session.yaml";
        Logger.log(LogLevel.INFO, 2, "Path to Session save is: " + this.sessionPath);
    }

    // ── Comments ────────────────────────────────────────────────────────

    public HashMap<String, String> load(GuiApp guiApp) {
        HashMap<String, String> comments = new HashMap<>();
        File file = new File(commentsPath);
        this.guiApp = guiApp;

        if (!file.exists()) {
            Logger.log(LogLevel.INFO, 1, "No comments file found, starting fresh.");
            return comments;
        }

        // Parse line by line to avoid SnakeYAML auto-parsing dates like 2977-11-21 into Date objects.
        // Supports both quoted keys ("2977-11-21": ...) and unquoted (2977-11-21: ...)
        Pattern pattern = Pattern.compile("^\"?([\\w\\-]+)\"?:\\s*(.*)$");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String currentKey = null;
            StringBuilder currentVal = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("  ") || line.startsWith("\t")) {
                    // Continuation of block scalar
                    if (currentKey != null) {
                        if (currentVal.length() > 0) currentVal.append("\n");
                        currentVal.append(line.stripLeading());
                    }
                    continue;
                }
                // Save previous key before starting a new one
                if (currentKey != null) {
                    String val = currentVal.toString().trim();
                    if (!val.isEmpty()) comments.put(currentKey, val);
                    currentKey = null;
                    currentVal.setLength(0);
                }
                line = line.trim();
                if (line.isEmpty()) continue;
                Matcher m = pattern.matcher(line);
                if (m.matches()) {
                    currentKey = m.group(1).trim();
                    String val = m.group(2).trim();
                    if (val.equals("|")) {
                        // Block scalar — value comes on following indented lines
                    } else {
                        currentVal.append(val);
                    }
                }
            }
            // Save last entry
            if (currentKey != null) {
                String val = currentVal.toString().trim();
                if (!val.isEmpty()) comments.put(currentKey, val);
            }
            Logger.log(LogLevel.INFO, 1, "Loaded " + comments.size() + " comments.");
        } catch (IOException e) {
            Logger.log(LogLevel.WARNING, 1, "Error loading comments: " + e.getMessage());
            e.printStackTrace();
        }

        return comments;
    }

    public void save(HashMap<String, String> comments) {
        guiApp.oldComment();

        Map<String, String> toSave = new HashMap<>();
        for (Map.Entry<String, String> entry : comments.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().trim().isEmpty())
                toSave.put(entry.getKey(), entry.getValue());
        }

        ensureDir(commentsPath);
        backupComments();

        try {
            StringBuilder sb = new StringBuilder();
            toSave.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    String val = e.getValue().trim();
                    if (val.contains("\n")) {
                        // Block scalar — indent each line with 2 spaces
                        sb.append("\"").append(e.getKey()).append("\": |\n");
                        for (String line : val.split("\n", -1))
                            sb.append("  ").append(line).append("\n");
                    } else {
                        sb.append("\"").append(e.getKey()).append("\": ").append(val).append("\n");
                    }
                });
            Files.writeString(Path.of(commentsPath), sb.toString());
            Logger.log(LogLevel.INFO, 1, "Saved " + toSave.size() + " comments.");
        } catch (IOException e) {
            Logger.log(LogLevel.WARNING, 1, "Error saving comments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void backupComments() {
        File source = new File(commentsPath);
        if (!source.exists()) return;
        try {
            Files.copy(source.toPath(), Path.of(commentsBackupPath),
                    StandardCopyOption.REPLACE_EXISTING);
            Logger.log(LogLevel.INFO, 1, "Backup saved to comments-bak.yaml.");
        } catch (IOException e) {
            Logger.log(LogLevel.WARNING, 1, "Error creating backup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Session state ────────────────────────────────────────────────────

    public void saveSession(int year, int month, int day, String nation, String campaign) {
        ensureDir(sessionPath);
        try {
            String content = "year: " + year + "\n"
                           + "month: " + month + "\n"
                           + "day: " + day + "\n"
                           + "nation: \"" + nation + "\"\n"
                           + "lang: " + Localization.getLang().name() + "\n"
                           + "campaign: \"" + (campaign != null ? campaign : "") + "\"\n";
            Files.writeString(Path.of(sessionPath), content);
            Logger.log(LogLevel.INFO, 1, "Saved session state.");
        } catch (IOException e) {
            Logger.log(LogLevel.WARNING, 1, "Error saving session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public SessionState loadSession(int defaultYear, int defaultMonth, int defaultDay, String defaultNation) {
        File file = new File(sessionPath);
        if (!file.exists()) {
            Logger.log(LogLevel.INFO, 1, "No session file found, using defaults.");
            return new SessionState(defaultYear, defaultMonth, defaultDay, defaultNation, "SV", "");
        }

        // Parse session.yaml line by line for same reason as comments
        int year = defaultYear, month = defaultMonth, day = defaultDay;
        String nation = defaultNation, lang = "SV", campaign = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(":\\s*", 2);
                if (parts.length != 2) continue;
                String k = parts[0].trim();
                String v = parts[1].trim().replace("\"", "");
                switch (k) {
                    case "year"     -> year     = parseIntOrDefault(v, defaultYear);
                    case "month"    -> month    = parseIntOrDefault(v, defaultMonth);
                    case "day"      -> day      = parseIntOrDefault(v, defaultDay);
                    case "nation"   -> nation   = v.isEmpty() ? defaultNation : v;
                    case "lang"     -> lang     = v.isEmpty() ? "SV" : v;
                    case "campaign" -> campaign = v;
                }
            }
            Logger.log(LogLevel.INFO, 1, "Loaded session: " + nation + " " + year + "-" + month + "-" + day + " [" + lang + "] campaign=" + campaign);
        } catch (IOException e) {
            Logger.log(LogLevel.WARNING, 1, "Error loading session: " + e.getMessage());
            e.printStackTrace();
        }

        return new SessionState(year, month, day, nation, lang, campaign);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private int parseIntOrDefault(Object val, int def) {
        if (val == null) return def;
        try { return Integer.parseInt(val.toString()); }
        catch (NumberFormatException e) { return def; }
    }

    private String parseStrOrDefault(Object val, String def) {
        if (val == null) return def;
        String s = val.toString().trim();
        return s.isEmpty() ? def : s;
    }

    private void ensureDir(String path) {
        File dir = new File(path).getParentFile();
        if (!dir.exists()) dir.mkdirs();
    }
}