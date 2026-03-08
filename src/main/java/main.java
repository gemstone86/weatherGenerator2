import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Random;

import gui.GuiApp;
import javafx.application.Application;
import javafx.stage.Stage;
import weather.fileHandler;
import weather.nationData;
import weather.weather;
import weather.weatherCalculator;

public class main extends Application {

    LinkedList<String> list_of_files = new LinkedList<String>();
    String path = Paths.get("").toAbsolutePath().toString();

    boolean print = true;
    static String nation = "Colonan";
    Random rng;

    static int start_day   = 1;
    static int start_month = 7;
    static int start_year  = 2977;

    static int until_day   = 28;
    static int until_month = 12;
    static int until_year  = 2961;

    // Called by JavaFX after init(); this is where the GUI is built.
    @Override
    public void start(Stage primaryStage) {
        String Path = getPath();

        rng = new Random(3118725);
        System.out.println("Step 0: Path is \"" + Path + "\"");

        weatherCalculator calculator = new weatherCalculator(rng);

        System.out.println("Step 1: Loading Data Files");
        fileHandler filehandler = new fileHandler(Path);

        System.out.println("Step 2: Opening Weather File");
        filehandler.createWeatherFile(nation, start_year, until_year);

        nationData Nation = filehandler.getNation(nation);

        System.out.println("Step 3: Setting up data");
        double temp = Nation.getTemperature(start_month);
        int rain = calculator.obd6(), wind = calculator.obd6();

        LinkedList<weather> list_of_weather = new LinkedList<weather>();

        if (print) System.out.println("Step 3b: Printing Data on screen");
        if (print) System.out.print(filehandler.printHeader());

        /* write header to file */
        filehandler.addToFile(filehandler.printHeader(), true);
        System.out.println("Step 4: Closing files");
        filehandler.closeWeatherFile();

        System.out.println("Step 5: Starting JavaFX GUI");
        new GuiApp(filehandler, list_of_weather, start_year, start_month, start_day, nation, primaryStage);
    }

    // Standard JavaFX entry point
    public static void main(String[] args) {
        // Optionally override the nation from command-line args
        if (args.length > 0) {
            nation = args[0];
        }
        // Launch JavaFX application (calls start() on the JavaFX thread)
        launch(args);
    }

    public String getNation() { return nation; }
    public String getPath()   { return path; }
}
