package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import weather.fileHandler;
import weather.nationData;
import weather.*;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Random;

public class GuiApp {

    static int year = 2964;
    static int day = 1;
    static int month = 1;

    int start_year;
    int start_month;
    int start_day;

    String nation;

    nationData nationData;
    fileHandler fileHandler;
    ComboBox<String> dropDownNations;
    String[] listOfNations;

    weatherCalculator newCalc = new weatherCalculator(new Random());

    public GuiApp(fileHandler fileHandler, final LinkedList<weather> listOfWeather,
                  int start_year, int start_month, int start_day, String nation, Stage primaryStage) {

        this.fileHandler = fileHandler;
        this.nation = nation;
        this.start_year = start_year;
        this.start_month = start_month;
        this.start_day = start_day;

        year = start_year;
        month = start_month;
        day = start_day;

        primaryStage.setTitle("Eon Weather Generator");

        // ── Nation selector ──────────────────────────────────────────────
        listOfNations = fileHandler.getListOfNations();
        dropDownNations = new ComboBox<>();
        for (String n : listOfNations) dropDownNations.getItems().add(n);
        dropDownNations.getSelectionModel().selectFirst();

        Label areaLabel = new Label("Area:");
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

        TextField weatherData  = new TextField();
        weatherData.setPrefWidth(320);
        weatherData.setEditable(false);

        TextField otherEffects = new TextField();
        otherEffects.setPrefWidth(420);
        otherEffects.setEditable(false);

        // ── Day controls ──────────────────────────────────────────────────
        Button dayUp   = new Button("+");
        Button dayDown = new Button("-");
        HBox dayBox = new HBox(4, new Label("Day"), dayDown, displayDay, dayUp);
        dayBox.setAlignment(Pos.CENTER);

        // ── Month controls ────────────────────────────────────────────────
        Button monthUp   = new Button("+");
        Button monthDown = new Button("-");
        HBox monthBox = new HBox(4, new Label("Month"), monthDown, displayMonth, monthUp);
        monthBox.setAlignment(Pos.CENTER);

        // ── Year controls ─────────────────────────────────────────────────
        Button yearUp   = new Button("+");
        Button yearDown = new Button("-");
        HBox yearBox = new HBox(4, new Label("Year"), yearDown, displayYear, yearUp);
        yearBox.setAlignment(Pos.CENTER);

        HBox dateControls = new HBox(16, dayBox, monthBox, yearBox);
        dateControls.setAlignment(Pos.CENTER);
        dateControls.setPadding(new Insets(8));

        // ── Weather display ───────────────────────────────────────────────
        Label weatherLabel = new Label("Weather");
        VBox weatherDisplay = new VBox(4, weatherLabel, weatherData, otherEffects);
        weatherDisplay.setAlignment(Pos.CENTER_LEFT);
        weatherDisplay.setPadding(new Insets(8));

        // ── Print to file button ──────────────────────────────────────────
        Button printToFile = new Button("Print to file");

        // ── Root layout ───────────────────────────────────────────────────
        VBox center = new VBox(12, areaBox, dateControls, weatherDisplay);
        center.setPadding(new Insets(12));

        BorderPane root = new BorderPane();
        root.setCenter(center);
        root.setBottom(printToFile);
        BorderPane.setAlignment(printToFile, Pos.CENTER_RIGHT);
        BorderPane.setMargin(printToFile, new Insets(8));

        // ── Event handlers ────────────────────────────────────────────────
        dropDownNations.setOnAction(e -> updateWeather(listOfWeather, weatherData, otherEffects));

        yearUp.setOnAction(e -> { updateYear(1);   updateDisplays(displayYear, displayMonth, displayDay); updateWeather(listOfWeather, weatherData, otherEffects); });
        yearDown.setOnAction(e -> { updateYear(-1); updateDisplays(displayYear, displayMonth, displayDay); updateWeather(listOfWeather, weatherData, otherEffects); });

        monthUp.setOnAction(e -> { updateMonth(1);   updateDisplays(displayYear, displayMonth, displayDay); updateWeather(listOfWeather, weatherData, otherEffects); });
        monthDown.setOnAction(e -> { updateMonth(-1); updateDisplays(displayYear, displayMonth, displayDay); updateWeather(listOfWeather, weatherData, otherEffects); });

        dayUp.setOnAction(e -> { updateDay(1);   updateDisplays(displayYear, displayMonth, displayDay); updateWeather(listOfWeather, weatherData, otherEffects); });
        dayDown.setOnAction(e -> { updateDay(-1); updateDisplays(displayYear, displayMonth, displayDay); updateWeather(listOfWeather, weatherData, otherEffects); });

        printToFile.setOnAction(e -> {
            updateMonth(-1);
            updateDisplays(displayYear, displayMonth, displayDay);
            updateWeather(listOfWeather, weatherData, otherEffects);
        });

        // ── Show stage ────────────────────────────────────────────────────
        Scene scene = new Scene(root, 750, 300);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initial weather render
        updateWeather(listOfWeather, weatherData, otherEffects);
    }

    public void updateWeather(LinkedList<weather> weatherList, TextField data, TextField other) {
        nation = listOfNations[dropDownNations.getSelectionModel().getSelectedIndex()];
        nationData = fileHandler.getNation(nation);

        weather test = newCalc.getWeather(year, month, day, nationData);
        DecimalFormat df = new DecimalFormat("##");

        String text = "Temp: " + df.format(test.getTemperature()) + "C"
                + "   Wind: " + test.getWindStrength()
                + " (" + test.getDirection() + ")"
                + "   Rain: " + test.getRain();

        data.setText(text);
        other.setText(test.getOther());
    }

    public void updateDay(int in) {
        day += in;
        if (day > 28) { day = 1;  updateMonth(1);  }
        else if (day < 1) { day = 28; updateMonth(-1); }
    }

    public void updateMonth(int in) {
        month += in;
        if (month > 12) { month -= 12; updateYear(1);  }
        else if (month < 1) { month += 12; updateYear(-1); }
    }

    public void updateYear(int in) {
        year += in;
    }

    private void updateDisplays(TextField displayYear, TextField displayMonth, TextField displayDay) {
        displayYear.setText(String.valueOf(year));
        displayMonth.setText(String.valueOf(month));
        displayDay.setText(String.valueOf(day));
    }
}
