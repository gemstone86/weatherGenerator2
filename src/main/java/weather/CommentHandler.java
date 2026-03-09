package weather;

import org.yaml.snakeyaml.Yaml;

import gui.GuiApp;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Saves and loads day comments to/from comments.yaml
 * Keys are day seeds (integers), values are comment strings.
 */
public class CommentHandler {

    private final String filePath;
    private GuiApp guiApp;
    
    public CommentHandler(String basePath) {
        this.filePath = basePath + "/src/comments/comments.yaml";
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, String> load(GuiApp guiApp) {
        HashMap<String, String> comments = new HashMap<>();
        File file = new File(filePath);
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
                    String key = (String) entry.getKey();
                    String value = entry.getValue() != null ? entry.getValue().toString() : "";
                    if (!value.isEmpty()) {
                        comments.put(key, value);
                    }
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
        // Only save non-empty comments
        guiApp.oldComment();
    	Map<String, String> toSave = new HashMap<>();
        for (Map.Entry<String, String> entry : comments.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                toSave.put(entry.getKey(), entry.getValue());
            }
        }

        try {
            Yaml yaml = new Yaml();
            String output = yaml.dump(toSave);
            Files.writeString(Path.of(filePath), output);
            Logger.log(LogLevel.INFO, 1, "Saved " + toSave.size() + " comments.");
        } catch (IOException e) {
        	Logger.log(LogLevel.WARNING, 1, "Error saving comments: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
