package weather;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class fileHandler {
    String basePath;
    BufferedWriter bufferedWriter;
    LinkedList<nationData> listOfNations = new LinkedList<nationData>();

    public fileHandler(String Path) {
        this.basePath = Path;
        System.out.println("Here: " + basePath);
        initializeDataFiles();
    }

    /**
     * Read a single .yaml nation file and return a nationData object.
     */
    @SuppressWarnings("unchecked")
    public nationData readYamlFile(String path) {
        File file = new File(path);
        try (FileInputStream fis = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(fis);

            String nationName = (String) data.get("name");

            // Temperature
            Map<String, Integer> tempMap = (Map<String, Integer>) data.get("temperature");
            int[] temperature = mapToMonthArray(tempMap);

            // Precipitation
            Map<String, Integer> rainMap = (Map<String, Integer>) data.get("precipitation");
            int[] rainfall = mapToMonthArray(rainMap);

            // Shift
            int shift = data.containsKey("shift") ? (int) data.get("shift") : 0;

            // Wind
            Map<String, Integer> windMap = (Map<String, Integer>) data.get("wind");
            int[] windStrength = mapToMonthArray(windMap);

            // Events
            LinkedList<event> events = new LinkedList<>();
            if (data.containsKey("events")) {
                List<Map<String, Object>> eventList = (List<Map<String, Object>>) data.get("events");
                for (Map<String, Object> e : eventList) {
                    String name = (String) e.get("name");
                    int occurs = (int) e.get("occurs");
                    int days = (int) e.get("days");
                    events.add(new event(name, occurs, days));
                }
            }

            return new nationData(nationName, temperature, rainfall, shift, windStrength, events);

        } catch (IOException e) {
            System.out.println("Couldn't read YAML file: " + path);
            e.printStackTrace();
        }
        return null;
    }

    private int[] mapToMonthArray(Map<String, Integer> map) {
        String[] keys = {"jan", "feb", "mar", "apr", "may", "jun",
                         "jul", "aug", "sep", "oct", "nov", "dec"};
        int[] arr = new int[12];
        for (int i = 0; i < 12; i++) {
            arr[i] = map.getOrDefault(keys[i], 0);
        }
        return arr;
    }

    /**
     * Load all .yaml files from the data folder.
     */
    public void initializeDataFiles() {
        String folderPath = basePath + "/src/data";
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yaml"));

        if (files == null || files.length == 0) {
            System.out.println("No YAML files found in " + folderPath);
            System.out.println("Run the converter first to convert your .txt files to YAML.");
            return;
        }

        for (File f : files) {
            System.out.println("    Loading: " + f.getName());
            nationData nd = readYamlFile(f.getAbsolutePath());
            if (nd != null) listOfNations.add(nd);
        }
    }

    public String[] getListOfNations() {
        String[] list = new String[listOfNations.size()];
        for (int i = 0; i < listOfNations.size(); i++)
            list[i] = listOfNations.get(i).getName();
        return list;
    }

    public nationData getNation(String nation) {
        for (int i = 0; i < listOfNations.size(); i++)
            if (listOfNations.get(i).getName().equals(nation))
                return listOfNations.get(i);
        System.out.println("Couldn't find area: " + nation);
        return null;
    }

    public int[] getTemperatures(String Name) {
        for (int i = 0; i < listOfNations.size(); i++)
            if (listOfNations.get(i).getName().equals(Name))
                return listOfNations.get(i).getTemperature();
        return null;
    }

    public int[] getWindStrength(String Name) {
        for (int i = 0; i < listOfNations.size(); i++)
            if (listOfNations.get(i).getName().equals(Name))
                return listOfNations.get(i).getWindStrength();
        return null;
    }

    public void addToFile(String add, boolean linebreak) {
        try {
            bufferedWriter.write(add);
            if (linebreak) bufferedWriter.write(System.lineSeparator());
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void createWeatherFile(String name, int from, int to) {
        try {
            File file = new File(basePath + " " + name + " (" + from + " to " + (to - 1) + ").txt");
            if (!file.exists()) file.createNewFile();
            bufferedWriter = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
        } catch (IOException e) {
            System.out.println("Couldn't create weather file at " + basePath);
            e.printStackTrace();
        }
    }

    public void closeWeatherFile() {
        try { bufferedWriter.close(); } catch (IOException e) { e.printStackTrace(); }
    }

    public String printHeader() {
        return "Year\tMonth\tDay\tWind\tTemperature\tRainfall\tOther\n";
    }

    public String printData(int Year, int Month, int Day, int Wind, double temp, int Rainfall, String Other) {
        DecimalFormat df = new DecimalFormat("##.#");
        return Year + "\t" + Month + "\t" + Day + "\t" + Wind + "\t" + df.format(temp) + " C\t\t" + Rainfall + "\t\t" + Other + "\n";
    }

    public String[] getTemperaturesAsString(String Name) {
        String[] temp = new String[12];
        for (int i = 0; i < listOfNations.size(); i++)
            if (listOfNations.get(i).getName().equals(Name)) {
                for (int j = 0; j < 12; j++)
                    temp[j] = Integer.toString(listOfNations.get(i).getTemperature(j));
                return temp;
            }
        return null;
    }
}
