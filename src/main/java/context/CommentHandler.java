package context;

import org.yaml.snakeyaml.Yaml;
import gui.GuiApp;
import gui.Localization;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class CommentHandler {

    private final String commentsPath;
    private final String commentsBackupPath;
    private final String sessionPath;
    private GuiApp guiApp;

    public CommentHandler(String basePath) {
        this.commentsPath       = basePath + "/src/comments/comments.yaml";
        this.commentsBackupPath = basePath + "/src/comments/comments-bak.yaml";
        this.sessionPath        = basePath + "/src/comments/session.yaml";
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

        try (FileInputStream fis = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Object raw = yaml.load(fis);
            if (raw instanceof Map) {
                Map<?, ?> data = (Map<?, ?>) raw;
                for (Map.Entry<?, ?> entry : data.entrySet()) {
                    String key   = String.valueOf(entry.getKey());
                    String value = entry.getValue() != null ? entry.getValue().toString() : "";
                    if (!value.isEmpty()) comments.put(key, value);
                }
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
                .forEach(e -> sb
                    .append(e.getKey()).append(": ")
                    .append(e.getValue().replace("\n", " ").trim())
                    .append("\n"));
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

    public void saveSession(int year, int month, int day, String nation) {
        ensureDir(sessionPath);
        try {
            String content = "year: " + year + "\n"
                           + "month: " + month + "\n"
                           + "day: " + day + "\n"
                           + "nation: \"" + nation + "\"\n"
                           + "lang: " + Localization.getLang().name() + "\n";
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
            return new SessionState(defaultYear, defaultMonth, defaultDay, defaultNation, "SV");
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Object raw = yaml.load(fis);
            if (raw instanceof Map) {
                Map<?, ?> data = (Map<?, ?>) raw;
                int year      = parseIntOrDefault(data.get("year"),   defaultYear);
                int month     = parseIntOrDefault(data.get("month"),  defaultMonth);
                int day       = parseIntOrDefault(data.get("day"),    defaultDay);
                String nation = parseStrOrDefault(data.get("nation"), defaultNation);
                String lang   = parseStrOrDefault(data.get("lang"),   "SV");
                Logger.log(LogLevel.INFO, 1, "Loaded session: " + nation + " " + year + "-" + month + "-" + day + " [" + lang + "]");
                return new SessionState(year, month, day, nation, lang);
            }
        } catch (IOException e) {
            Logger.log(LogLevel.WARNING, 1, "Error loading session: " + e.getMessage());
            e.printStackTrace();
        }

        return new SessionState(defaultYear, defaultMonth, defaultDay, defaultNation, "SV");
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
