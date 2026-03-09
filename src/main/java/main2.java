import java.io.File;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Random;

import javafx.application.Application;
import javafx.stage.Stage;

import gui.GuiApp;
import weather.*;

public class main2 extends Application {

    String path = Paths.get("").toAbsolutePath().toString();

    static String nation     = "Colonan";
    static int start_day     = 1;
    static int start_month   = 7;
    static int start_year    = 2977;
    static int until_day     = 28;
    static int until_month   = 12;
    static int until_year    = 2961;

    @Override
    public void start(Stage primaryStage) {
        String Path = path;

        System.out.println("Step 0: Path is \"" + Path + "\"");

        // Convert .txt files to .yaml if no .yaml files exist yet
        String dataPath = Path + "/src/data";
        File dataFolder = new File(dataPath);
        File[] yamlFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".yaml"));
        System.out.println(yamlFiles.length);
        if (yamlFiles == null || yamlFiles.length == 0) {
            System.out.println("No YAML files found — converting .txt files...");
            TxtToYamlConverter.convertAll(Path);
        }

        Random rng = new Random(3118725);
        weatherCalculator calculator = new weatherCalculator(rng);

        System.out.println("Step 1: Loading Data Files");
        fileHandler filehandler = new fileHandler(Path);

        System.out.println("Step 2: Opening Weather File");
        filehandler.createWeatherFile(nation, start_year, until_year);

        nationData Nation = filehandler.getNation(nation);

        System.out.println("Step 3: Setting up data");
        LinkedList<weather> list_of_weather = new LinkedList<weather>();
        filehandler.addToFile(filehandler.printHeader(), true);

        System.out.println("Step 4: Closing files");
        filehandler.closeWeatherFile();

        System.out.println("Step 5: Starting JavaFX GUI");
        new GuiApp(filehandler, list_of_weather, start_year, start_month, start_day, nation, primaryStage);
    }

    public static void main(String[] args) {
        if (args.length > 0) nation = args[0];
        launch(args);
    }

    public String getPath() { return path; }
}
