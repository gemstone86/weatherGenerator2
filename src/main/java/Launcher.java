import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javafx.application.Application;
import javafx.stage.Stage;

import gui.GuiApp;
import weather.*;

public class Launcher extends Application {

    String path = Paths.get("").toAbsolutePath().toString();

    static String nation   = "Colonan";
    static int start_day   = 1;
    static int start_month = 7;
    static int start_year  = 2977;
    static int until_year  = 2961;

    @Override
    public void start(Stage primaryStage) {
    	// Redirect stderr to a file so errors are never lost
    	try {
    	    PrintStream errLog = new PrintStream(new FileOutputStream(path + "/error.log", true));
    	    System.setErr(errLog);
    	} catch (Exception e) { /* ignore */ }
    	Logger.setLevel(LogLevel.INFO);
    	
    	Logger.log(LogLevel.INFO, 0, "Step 0: Path is \"" + path + "\"");

        String dataPath = path + "/src/data";
        File dataFolder = new File(dataPath);
        File[] yamlFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".yaml"));
        if (yamlFiles == null || yamlFiles.length == 0) {
            Logger.log(LogLevel.INFO, 1, "No YAML files found — converting .txt files...");
            TxtToYamlConverter.convertAll(path);
        }

        Logger.log(LogLevel.INFO, 0, "Step 1: Loading Data Files");
        fileHandler filehandler = new fileHandler(path);

        Logger.log(LogLevel.INFO, 0, "Step 2: Loading Global Events");
        List<GlobalEvent> globalEvents = GlobalEventLoader.load(path);

        Logger.log(LogLevel.INFO, 0, "Step 3: Opening Weather File");
        filehandler.createWeatherFile(nation, start_year, until_year);

        Logger.log(LogLevel.INFO, 0, "Step 4: Setting up calculator");
        weatherCalculator calculator = new weatherCalculator(new Random(3118725));
        calculator.setGlobalEvents(globalEvents);

        Logger.log(LogLevel.INFO, 0, "Step 5: Loading comments");
        CommentHandler commentHandler = new CommentHandler(path);
        SessionState session = commentHandler.loadSession(start_year, start_month, start_day, nation);

        // Apply saved language using string — avoids any enum casting issues in bytecode
        Localization.setLangFromString(session.lang);

        Logger.log(LogLevel.INFO, 0, "Step 6: Setting up data");
        LinkedList<weather> list_of_weather = new LinkedList<weather>();
        filehandler.addToFile(filehandler.printHeader(), true);
        filehandler.closeWeatherFile();

        Logger.log(LogLevel.INFO, 0, "Step 7: Starting JavaFX GUI");
        new GuiApp(filehandler, list_of_weather, session.year, session.month, session.day,
                   session.nation, primaryStage, calculator, commentHandler);
    }

    public static void main(String[] args) {
        if (args.length > 0) nation = args[0];
        launch(args);
    }
}
