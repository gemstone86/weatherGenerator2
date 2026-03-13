package gui;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import weather.CommentHandler;
import weather.Lang;
import weather.Localization;
import weather.LogLevel;
import weather.Logger;
import weather.fileHandler;
import weather.nationData;
import weather.weather;
import weather.weatherCalculator;

public class GuiApp {

    static int year = 2964;
    static int day = 1;
    static int month = 1;

    int start_year;
    int start_month;
    int start_day;

    String nation;

    TextArea commentBox;

    nationData nationData;
    fileHandler fileHandler;
    ComboBox<String> dropDownNations;
    String[] listOfNations;

    HashMap<String, String> comments = new HashMap<>();
    CommentHandler commentHandler;

    weatherCalculator newCalc;

    // Labels and controls that need updating on language switch
    Label areaLabel, weatherLabel, miscLabel, commentLabel, dayLabel, monthLabel, yearLabel;
    Button printToFile, langToggle;
    Stage primaryStage;
    TextField weatherData, otherEffects;

    public GuiApp(fileHandler fileHandler, final LinkedList<weather> listOfWeather,
                  int start_year, int start_month, int start_day, String nation, Stage primaryStage,
                  weatherCalculator newCalc, CommentHandler commentHandler) {

        this.fileHandler = fileHandler;
        this.nation = nation;
        this.start_year = start_year;
        this.start_month = start_month;
        this.start_day = start_day;
        this.newCalc = newCalc;
        this.commentHandler = commentHandler;
        this.primaryStage = primaryStage;

        // Load saved comments from file
        comments = commentHandler.load(this);

        year = start_year;
        month = start_month;
        day = start_day;

        primaryStage.setTitle(Localization.get("title"));

        // ── Nation selector ──────────────────────────────────────────────
        listOfNations = fileHandler.getListOfNations();
        dropDownNations = new ComboBox<>();
        for (String n : listOfNations) dropDownNations.getItems().add(n);
        dropDownNations.getSelectionModel().select(nation);
        if (dropDownNations.getSelectionModel().getSelectedIndex() < 0)
            dropDownNations.getSelectionModel().selectFirst();

        areaLabel = new Label(Localization.get("label.area"));
        HBox areaBox = new HBox(8, areaLabel, dropDownNations);
        areaBox.setAlignment(Pos.CENTER);

        // ── Display fields ────────────────────────────────────────────────
        TextField displayYear  = new TextField(String.valueOf(year));
        TextField displayMonth = new TextField(String.valueOf(month));
        TextField displayDay   = new TextField(String.valueOf(day));
        for (TextField tf : new TextField[]{displayYear, displayMonth, displayDay}) {
            tf.setPrefWidth(60);
            tf.setEditable(false);
        }

        weatherData = new TextField();
        weatherData.setPrefWidth(320);
        weatherData.setEditable(false);

        otherEffects = new TextField();
        otherEffects.setPrefWidth(420);
        otherEffects.setEditable(false);

        // ── Day controls ──────────────────────────────────────────────────
        Button dayUp   = new Button("+");
        Button dayDown = new Button("-");
        dayLabel = new Label(Localization.get("label.day"));
        HBox dayBox = new HBox(4, dayLabel, dayDown, displayDay, dayUp);
        dayBox.setAlignment(Pos.CENTER);

        // ── Month controls ────────────────────────────────────────────────
        Button monthUp   = new Button("+");
        Button monthDown = new Button("-");
        monthLabel = new Label(Localization.get("label.month"));
        HBox monthBox = new HBox(4, monthLabel, monthDown, displayMonth, monthUp);
        monthBox.setAlignment(Pos.CENTER);

        // ── Year controls ─────────────────────────────────────────────────
        Button yearUp   = new Button("+");
        Button yearDown = new Button("-");
        yearLabel = new Label(Localization.get("label.year"));
        HBox yearBox = new HBox(4, yearLabel, yearDown, displayYear, yearUp);
        yearBox.setAlignment(Pos.CENTER);

        HBox dateControls = new HBox(16, dayBox, monthBox, yearBox);
        dateControls.setAlignment(Pos.CENTER);
        dateControls.setPadding(new Insets(8));

        // ── Weather display ───────────────────────────────────────────────
        weatherLabel = new Label(Localization.get("label.weather"));
        miscLabel    = new Label(Localization.get("label.misc"));
        VBox weatherDisplay = new VBox(4, weatherLabel, weatherData, miscLabel, otherEffects);
        weatherDisplay.setAlignment(Pos.CENTER_LEFT);
        weatherDisplay.setPadding(new Insets(8));

        // ── Comment display ───────────────────────────────────────────────
        commentLabel = new Label(Localization.get("label.comment"));
        commentBox = new TextArea();
        commentBox.setPromptText(Localization.get("prompt.comment"));
        commentBox.setPrefHeight(80);
        commentBox.setWrapText(true);
        VBox commentArea = new VBox(4, commentLabel, commentBox);
        commentArea.setAlignment(Pos.BOTTOM_CENTER);

        // ── Buttons ───────────────────────────────────────────────────────
        printToFile = new Button(Localization.get("button.printfile"));
        langToggle  = new Button(Localization.get("button.lang"));

        // ── Root layout ───────────────────────────────────────────────────
        VBox center = new VBox(12, areaBox, dateControls, weatherDisplay, commentArea);
        center.setPadding(new Insets(12));

        HBox bottomBar = new HBox(8, langToggle, printToFile);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setCenter(center);
        root.setBottom(bottomBar);

        // ── Event handlers ────────────────────────────────────────────────
        dropDownNations.setOnAction(e -> updateWeather(listOfWeather));

        yearUp.setOnAction(e ->    { updateYear(1);    updateDisplays(displayYear, displayMonth, displayDay); updateWeather(listOfWeather); });
        yearDown.setOnAction(e ->  { updateYear(-1);   updateDisplays(displayYear, displayMonth, displayDay); updateWeather(listOfWeather); });
        monthUp.setOnAction(e ->   { updateMonth(1);   updateDisplays(displayYear, displayMonth, displayDay); updateWeather(listOfWeather); });
        monthDown.setOnAction(e -> { updateMonth(-1);  updateDisplays(displayYear, displayMonth, displayDay); updateWeather(listOfWeather); });
        dayUp.setOnAction(e ->     { updateDay(1);     updateDisplays(displayYear, displayMonth, displayDay); updateWeather(listOfWeather); });
        dayDown.setOnAction(e ->   { updateDay(-1);    updateDisplays(displayYear, displayMonth, displayDay); updateWeather(listOfWeather); });

        printToFile.setOnAction(e -> {
            updateMonth(-1);
            updateDisplays(displayYear, displayMonth, displayDay);
            updateWeather(listOfWeather);
        });

        langToggle.setOnAction(e -> {
            Localization.setLangFromString(Localization.getLang() == Lang.SV ? "EN" : "SV");
            refreshLabels();
            updateWeather(listOfWeather); // re-render weather text in new language
        });

        // Save comments and session on window close
        primaryStage.setOnCloseRequest(e -> {
            commentHandler.save(comments);
            Logger.log(LogLevel.DEBUG, 1, "saving area: " + dropDownNations.getValue());
            Logger.log(LogLevel.DEBUG, 1, "saving year-month-day: " + year + "-" + month + "-" + day);
            commentHandler.saveSession(year, month, day, dropDownNations.getValue());
        });

        // ── Show stage ────────────────────────────────────────────────────
        Scene scene = new Scene(root, 750, 470);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load comment for starting day and render weather
        nextComment();
        updateWeather(listOfWeather);
    }

    /** Re-apply all localized strings after a language switch. */
    private void refreshLabels() {
        primaryStage.setTitle(Localization.get("title"));
        areaLabel.setText(Localization.get("label.area"));
        dayLabel.setText(Localization.get("label.day"));
        monthLabel.setText(Localization.get("label.month"));
        yearLabel.setText(Localization.get("label.year"));
        weatherLabel.setText(Localization.get("label.weather"));
        miscLabel.setText(Localization.get("label.misc"));
        commentLabel.setText(Localization.get("label.comment"));
        commentBox.setPromptText(Localization.get("prompt.comment"));
        printToFile.setText(Localization.get("button.printfile"));
        langToggle.setText(Localization.get("button.lang"));
    }

    public void updateWeather(LinkedList<weather> weatherList) {
        nation = listOfNations[dropDownNations.getSelectionModel().getSelectedIndex()];
        nationData = fileHandler.getNation(nation);

        weather test = newCalc.getWeather(year, month, day, nationData);
        DecimalFormat df = new DecimalFormat("##");

        String text = Localization.get("weather.temp") + ": " + df.format(test.getTemperature()) + "C"
                + "   " + Localization.get("weather.wind") + ": " + test.getWindStrength()
                + " (" + test.getDirection() + ")"
                + "   " + Localization.get("weather.rain") + ": " + test.getRain();

        weatherData.setText(text);
        otherEffects.setText(test.getOther());
    }

    public String getDaySeed() {
        return year + "-" + month + "-" + day;
    }

    public void oldComment() {
        comments.put(getDaySeed(), commentBox.getText());
    }

    public void nextComment() {
        commentBox.setText(comments.getOrDefault(getDaySeed(), ""));
    }

    public void updateDay(int in) {
        oldComment();
        day += in;
        if (day > 28) { day = 1;  updateMonth(1); }
        else if (day < 1) { day = 28; updateMonth(-1); }
        nextComment();
    }

    public void updateMonth(int in) {
        oldComment();
        month += in;
        if (month > 12) { month -= 12; updateYear(1); }
        else if (month < 1) { month += 12; updateYear(-1); }
        nextComment();
    }

    public void updateYear(int in) {
        oldComment();
        year += in;
        nextComment();
    }

    private void updateDisplays(TextField displayYear, TextField displayMonth, TextField displayDay) {
        displayYear.setText(String.valueOf(year));
        displayMonth.setText(String.valueOf(month));
        displayDay.setText(String.valueOf(day));
    }
}
