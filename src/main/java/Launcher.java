import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import context.CommentHandler;
import context.ImportantDatesLoader;
import context.LogLevel;
import context.Logger;
import context.ReligiousDate;
import context.SessionState;
import context.TxtToYamlConverter;
import context.calendarLoader;
import context.fileHandler;
import date.Calendar;
import javafx.application.Application;
import javafx.stage.Stage;
import gui.GuiApp;
import gui.Localization;
import weather.*;

public class Launcher extends Application {
    String path = Paths.get("").toAbsolutePath().toString();
    static String nation   = "Colonan";
    static int start_day   = 1;
    static int start_month = 7;
    static int start_year  = 2977;
    static int until_year  = 2961;
    static LogLevel myLogLevel = LogLevel.INFO;

    @Override
    public void start(Stage primaryStage) {
        // Redirect stderr to a file so errors are never lost
        try {
            PrintStream errLog = new PrintStream(new FileOutputStream(path + "/error.log", true));
            System.setErr(errLog);
        } catch (Exception e) { /* ignore */ }
        Logger.setLevel(myLogLevel);

        Logger.log(LogLevel.INFO, 0, "Step 0: Path is \"" + path + "\"");
        String dataPath = path + "/src/data/eon";
        Logger.log(LogLevel.INFO, 1, "Path to data folder is" + path + "\\data");
        File dataFolder = new File(dataPath);
        File[] yamlFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".yaml"));
        Logger.log(LogLevel.DEBUG, 1, "Found " + yamlFiles.length + " files:");
        for (int i = 0; i < yamlFiles.length - 1; i++) {
            Logger.log(LogLevel.DEBUG, 2, yamlFiles[i].toString());
        }
        if (yamlFiles == null || yamlFiles.length == 0) {
            Logger.log(LogLevel.INFO, 1, "No YAML files found — converting .txt files...");
            TxtToYamlConverter.convertAll(path);
        }
        Logger.log(LogLevel.INFO, 0, "Step 1: Loading Data Files");
        fileHandler filehandler = new fileHandler(dataPath);

        Logger.log(LogLevel.INFO, 0, "Step 2: Loading Global Events");
        List<GlobalEvent> globalEvents = GlobalEventLoader.load(path);

        Logger.log(LogLevel.INFO, 0, "Step 3: Loading Weather File");
        filehandler.createWeatherFile(nation, start_year, until_year);
  
        Logger.log(LogLevel.INFO, 0, "Step 5: Setting up calculator");
        weatherCalculator calculator = new weatherCalculator(new Random(3118725));
        calculator.setDateSerial(calculator.calculateDateSerial(start_year, start_month, start_day));
        calculator.setGlobalEvents(globalEvents);

        Logger.log(LogLevel.INFO, 0, "Step 6: Loading Important Dates");
        List<ReligiousDate> importantDates = ImportantDatesLoader.load(path);
        calculator.setReligiousDates(importantDates);

        Logger.log(LogLevel.INFO, 0, "Step 7: Loading comments");
        CommentHandler commentHandler = new CommentHandler(path);
        SessionState session = commentHandler.loadSession(start_year, start_month, start_day, nation);

        calculator.setDateSerial(calculator.calculateDateSerial(session.year, session.month, session.day));

        Logger.log(LogLevel.INFO, 0, "Step 4: Loading Calendar File");
        List<Calendar> calendars = calendarLoader.load(path, calculator);
        calculator.setCalendars(calendars);
        
        // Apply saved language using string — avoids any enum casting issues in bytecode
        Localization.setLangFromString(session.lang);

        Logger.log(LogLevel.INFO, 0, "Step 8: Setting up data");
        LinkedList<weather> list_of_weather = new LinkedList<weather>();
        filehandler.addToFile(filehandler.printHeader(), true);
        filehandler.closeWeatherFile();

        Logger.log(LogLevel.INFO, 0, "Step 9: Starting JavaFX GUI");
        new GuiApp(filehandler, list_of_weather, session.year, session.month, session.day,
                   session.nation, primaryStage, calculator, commentHandler);
    }

    public static void main(String[] args) {

        for (int i = 0; i < args.length; i++) {

            switch (args[i].toLowerCase()) {

                case "-loglevel":
                    if (i + 1 < args.length) {
                        String level = args[++i].toLowerCase();

                        switch (level) {
                            case "debug":
                                myLogLevel = LogLevel.DEBUG;
                                break;

                            case "warning":
                                myLogLevel = LogLevel.WARNING;
                                break;

                            case "all":
                                myLogLevel = LogLevel.ALL;
                                break;

                            default:
                                Logger.log(LogLevel.WARNING, 0, "Unknown log level: " + level);
                        }

                    } else {
                        Logger.log(LogLevel.WARNING, 0, "-loglevel requires a value");
                    }
                    break;

                default:
                    Logger.log(LogLevel.WARNING, 0, "Unknown parameter: " + args[i]);
            }
        }

        Logger.log(myLogLevel, 0, "Logging level set to " + myLogLevel);
        launch(args);
    }
}
