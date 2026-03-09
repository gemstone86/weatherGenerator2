package weather;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

/**
 * Loads global events from additional-events.yaml
 */
public class GlobalEventLoader {

    @SuppressWarnings("unchecked")
    public static List<GlobalEvent> load(String basePath) {
        
    	String path = basePath + "\\src\\additional-events.yaml";
    	Logger.log(LogLevel.INFO, 2, "Additional-events path is: " + path);
    	File file = new File(path);
        List<GlobalEvent> result = new ArrayList<>();

        
        if (!file.exists()) {
        	Logger.log(LogLevel.WARNING, 0, "No additional-events.yaml found at " + path);
            return result;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(fis);
            List<Map<String, Object>> events = (List<Map<String, Object>>) data.get("events");

            for (Map<String, Object> e : events) {
                String name    = (String) e.get("name");
                int occurs     = getInt(e, "occurs", 1);
                int days       = getInt(e, "days", 365);
                int startMonth = getInt(e, "start_month", 0);
                int endMonth   = getInt(e, "end_month", 0);
                int minWind    = getInt(e, "min_wind", 0);
                int maxWind    = getInt(e, "max_wind", 999);
                int bonusWind  = getInt(e, "bonus_wind", 0);
                int bonusTemp  = getInt(e, "bonus_temp", 0);
                int bonusRain  = getInt(e, "bonus_rain", 0);

                result.add(new GlobalEvent(name, occurs, days,
                        startMonth, endMonth, minWind, maxWind,
                        bonusWind, bonusTemp, bonusRain));
                
                Logger.log(LogLevel.INFO, 2, result.getLast().toString());
            }

        } catch (IOException e) {
        	Logger.log(LogLevel.WARNING, 2, "Error loading additional-events.yaml: " + e.getMessage());
            e.printStackTrace();
        }

        Logger.log(LogLevel.INFO, 2, "Loaded " + result.size() + " global events.");
        return result;
    }

    private static int getInt(Map<String, Object> map, String key, int defaultVal) {
        Object val = map.get(key);
        if (val == null) return defaultVal;
        return (int) val;
    }
}
