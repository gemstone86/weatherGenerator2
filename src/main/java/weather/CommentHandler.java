package weather;

import org.yaml.snakeyaml.Yaml;
import gui.GuiApp;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Saves and loads day comments to/from comments.yaml
 * Also saves/loads last-used session state (date + nation) to/from session.yaml
 */
public class CommentHandler {

    private final String commentsPath;
    private final String sessionPath;
    private GuiApp guiApp;

    public CommentHandler(String basePath) {
        this.commentsPath = basePath + "/src/comments/comments.yaml";
        this.sessionPath  = basePath + "/src/comments/session.yaml";
    }

    // ── Comments ────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
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
            Map<Object, Object> data = yaml.load(fis);
            if (data != null) {
                for (Map.Entry<Object, Object> entry : data.entrySet()) {
                    String key   = (String) entry.getKey();
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

    // ── Session state ────────────────────────────────────────────────────

    public void saveSession(int year, int month, int day, String nation) {
        ensureDir(sessionPath);
        try {
            String content = "year: " + year + "\n"
                           + "month: " + month + "\n"
                           + "day: " + day + "\n"
                           + "nation: \"" + nation + "\"\n";
            Files.writeString(Path.of(sessionPath), content);
            Logger.log(LogLevel.INFO, 1, "Saved session state.");
        } catch (IOException e) {
            Logger.log(LogLevel.WARNING, 1, "Error saving session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public SessionState loadSession(int defaultYear, int defaultMonth, int defaultDay, String defaultNation) {
        File file = new File(sessionPath);
        if (!file.exists()) {
            Logger.log(LogLevel.INFO, 1, "No session file found, using defaults.");
            return new SessionState(defaultYear, defaultMonth, defaultDay, defaultNation);
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(fis);
            if (data != null) {
                int year     = (int) data.getOrDefault("year",   defaultYear);
                int month    = (int) data.getOrDefault("month",  defaultMonth);
                int day      = (int) data.getOrDefault("day",    defaultDay);
                String nation = (String) data.getOrDefault("nation", defaultNation);
                Logger.log(LogLevel.INFO, 1, "Loaded session: " + nation + " " + year + "-" + month + "-" + day);
                return new SessionState(year, month, day, nation);
            }
        } catch (IOException e) {
            Logger.log(LogLevel.WARNING, 1, "Error loading session: " + e.getMessage());
            e.printStackTrace();
        }

        return new SessionState(defaultYear, defaultMonth, defaultDay, defaultNation);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private void ensureDir(String path) {
        File dir = new File(path).getParentFile();
        if (!dir.exists()) dir.mkdirs();
    }
}
