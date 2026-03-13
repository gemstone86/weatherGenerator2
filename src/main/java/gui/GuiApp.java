package gui;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
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
import context.CommentHandler;
import gui.Localization;
import context.LogLevel;
import context.Logger;
import context.fileHandler;
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

    Label areaLabel, weatherLabel, miscLabel, commentLabel, dayLabel, monthLabel, yearLabel;
    Button printToFile, langToggle, graphToggle;
    Stage primaryStage;
    TextField weatherData, otherEffects;
    Canvas graphCanvas;
    boolean graphVisible = false;
    static final int GRAPH_WIDTH = 300;
    static final int GRAPH_HEIGHT = 400;

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
        printToFile  = new Button(Localization.get("button.printfile"));
        langToggle   = new Button(Localization.get("button.lang"));
        graphToggle  = new Button(Localization.get("button.graph"));

        // ── Graph panel ───────────────────────────────────────────────────
        graphCanvas = new Canvas(GRAPH_WIDTH, GRAPH_HEIGHT);
        VBox graphPanel = new VBox(graphCanvas);
        graphPanel.setPadding(new Insets(8));
        graphPanel.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");
        graphPanel.setVisible(false);
        graphPanel.setManaged(false);

        // ── Root layout ───────────────────────────────────────────────────
        VBox center = new VBox(12, areaBox, dateControls, weatherDisplay, commentArea);
        center.setPadding(new Insets(12));

        HBox bottomBar = new HBox(8, langToggle, graphToggle, printToFile);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setCenter(center);
        root.setRight(graphPanel);
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
            updateWeather(listOfWeather);
        });

        primaryStage.setOnCloseRequest(e -> {
            commentHandler.save(comments);
            Logger.log(LogLevel.DEBUG, 1, "saving area: " + dropDownNations.getValue());
            Logger.log(LogLevel.DEBUG, 1, "saving year-month-day: " + year + "-" + month + "-" + day);
            commentHandler.saveSession(year, month, day, dropDownNations.getValue());
        });

        graphToggle.setOnAction(e -> {
            graphVisible = !graphVisible;
            graphPanel.setVisible(graphVisible);
            graphPanel.setManaged(graphVisible);
            primaryStage.sizeToScene();
            if (graphVisible) drawGraph();
        });

        // ── Show stage ────────────────────────────────────────────────────
        Scene scene = new Scene(root, 750, 470);
        primaryStage.setScene(scene);
        primaryStage.show();

        nextComment();
        updateWeather(listOfWeather);
    }

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
        graphToggle.setText(Localization.get("button.graph"));
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
        if (graphVisible) drawGraph();
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

    private void drawGraph() {
        double[][] hourly = newCalc.getHourlyWeather(year, month, day,
                fileHandler.getNation(nation));
        double[] temps = hourly[0];
        double[] winds = hourly[1];
        double[] rains = hourly[2];

        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        double w = GRAPH_WIDTH;
        double panelH = GRAPH_HEIGHT;
        double graphH = (panelH - 60) / 3.0; // 3 graphs stacked, 20px gaps
        double padL = 36, padR = 8, padTop = 18, padBot = 4;
        double innerW = w - padL - padR;

        gc.clearRect(0, 0, w, panelH);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, w, panelH);

        String[] labels = {
            Localization.get("weather.temp"),
            Localization.get("weather.wind"),
            Localization.get("weather.rain")
        };
        Color[] colors = { Color.TOMATO, Color.STEELBLUE, Color.MEDIUMSEAGREEN };
        double[][] datasets = { temps, winds, rains };

        for (int g = 0; g < 3; g++) {
            double offsetY = g * (graphH + 20);
            double[] data = datasets[g];

            // Find min/max for this dataset
            double min = data[0], max = data[0];
            for (double v : data) { if (v < min) min = v; if (v > max) max = v; }
            if (max == min) { max = min + 1; } // avoid div by zero

            // Background
            gc.setFill(Color.rgb(245, 245, 245));
            gc.fillRect(padL, offsetY + padTop, innerW, graphH - padTop - padBot);

            // Grid lines (3 horizontal)
            gc.setStroke(Color.rgb(200, 200, 200));
            gc.setLineWidth(0.5);
            for (int i = 0; i <= 2; i++) {
                double gy = offsetY + padTop + (graphH - padTop - padBot) * i / 2.0;
                gc.strokeLine(padL, gy, padL + innerW, gy);
                // Y axis label
                double val = max - (max - min) * i / 2.0;
                gc.setFill(Color.GRAY);
                gc.setFont(javafx.scene.text.Font.font(9));
                gc.fillText(String.format("%.0f", val), 0, gy + 3);
            }

            // Graph label
            gc.setFill(Color.DARKGRAY);
            gc.setFont(javafx.scene.text.Font.font(10));
            gc.fillText(labels[g], padL, offsetY + 11);

            // Hour labels on bottom graph only
            if (g == 2) {
                gc.setFill(Color.GRAY);
                gc.setFont(javafx.scene.text.Font.font(8));
                for (int h = 0; h < 24; h += 4) {
                    double x = padL + h * innerW / 23.0;
                    gc.fillText(String.valueOf(h), x - 3, offsetY + graphH + 12);
                }
            }

            // Line
            gc.setStroke(colors[g]);
            gc.setLineWidth(1.5);
            gc.beginPath();
            for (int h = 0; h < 24; h++) {
                double x = padL + h * innerW / 23.0;
                double norm = (data[h] - min) / (max - min);
                double y = offsetY + padTop + (graphH - padTop - padBot) * (1 - norm);
                if (h == 0) gc.moveTo(x, y);
                else gc.lineTo(x, y);
            }
            gc.stroke();

            // Dots at each hour
            gc.setFill(colors[g]);
            for (int h = 0; h < 24; h++) {
                double x = padL + h * innerW / 23.0;
                double norm = (data[h] - min) / (max - min);
                double y = offsetY + padTop + (graphH - padTop - padBot) * (1 - norm);
                gc.fillOval(x - 2, y - 2, 4, 4);
            }
        }
    }

}
