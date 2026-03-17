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
import context.CommentLoader;
import context.LogLevel;
import context.Logger;
import context.fileHandler;
import weather.Nation;
import weather.Calendar;
import weather.Day;
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

    Nation nationData;
    fileHandler fileHandler;
    ComboBox<String> dropDownNations;
    ComboBox<String> calendarSystem;
    String[] listOfNations;

    HashMap<String, String> comments = new HashMap<>();
    CommentLoader commentHandler;

    weatherCalculator newCalc;

    Label calendarLabel, areaLabel, dateLabel, weatherLabel, miscLabel, commentLabel, dayLabel, weekLabel, monthLabel, yearLabel;
    Button printToFile, langToggle;
    Stage primaryStage;
    TextField date;
    TextField weatherData;
    TextArea miscTextBox;
    Canvas graphCanvas;
    private int windowWidth = 900; 
    private int windowHeight = 600;
    static final int GRAPH_WIDTH = 400;
    static final int GRAPH_HEIGHT = 500;
    
    private LinkedList<Day> listOfWeather;
    TextField displayYear, displayMonth, displayDay;
    

    public GuiApp(fileHandler fileHandler, final LinkedList<Day> listOfWeather,
                  int start_year, int start_month, int start_day, String nation, Stage primaryStage,
                  weatherCalculator newCalc, CommentLoader commentHandler) {

        this.fileHandler = fileHandler;
        this.nation = nation;
        this.start_year = start_year;
        this.start_month = start_month;
        this.start_day = start_day;
        this.newCalc = newCalc;
        this.commentHandler = commentHandler;
        this.primaryStage = primaryStage;
        this.listOfWeather = listOfWeather;

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
        
        calendarSystem = new ComboBox<>();
       
        
        for(Calendar n : newCalc.getCalendars()) {
        	calendarSystem.getItems().add(n.getName());
        }
        calendarSystem.getSelectionModel().selectFirst();
        

        areaLabel = new Label(Localization.get("label.area"));
        calendarLabel = new Label(Localization.get("label.calendar"));
        HBox areaBox = new HBox(8, areaLabel, dropDownNations, calendarLabel, calendarSystem);
        areaBox.setAlignment(Pos.CENTER);

        // ── Display fields ────────────────────────────────────────────────
        displayYear  = new TextField(String.valueOf(year));
        displayYear.setAlignment(Pos.CENTER);
        
        displayMonth = new TextField(String.valueOf(month));
        displayMonth.setAlignment(Pos.CENTER);
        
        displayDay   = new TextField(String.valueOf(day));
        displayDay.setAlignment(Pos.CENTER);
        
        for (TextField tf : new TextField[]{displayYear, displayMonth, displayDay}) {
            tf.setPrefWidth(60);
            tf.setEditable(false);
        }

        date = new TextField();
        date.setAlignment(Pos.CENTER);
        date.setPrefWidth(320);
        date.setEditable(false);
        
        weatherData = new TextField();
        weatherData.setAlignment(Pos.CENTER);
        weatherData.setPrefWidth(320);
        weatherData.setEditable(false);

        // ── Event box controls ──────────────────────────────────────────────────
        miscTextBox = new TextArea();
        miscTextBox.setWrapText(true);
        miscTextBox.setPrefHeight(100);
        miscTextBox.setPrefWidth(420);
        miscTextBox.setEditable(false);

        // ── Day controls ──────────────────────────────────────────────────
        Button dayUp   = new Button("+");
        Button dayDown = new Button("-");
        dayLabel = new Label(Localization.get("label.day"));
        HBox dayControls = new HBox(4, dayDown, displayDay, dayUp);
        dayControls.setAlignment(Pos.CENTER);
        VBox dayBox = new VBox(2, dayLabel, dayControls);
        dayBox.setAlignment(Pos.CENTER);

        // ── Week controls ────────────────────────────────────────────────
//        Button weekUp   = new Button("+");
//        Button weelDown = new Button("-");
//        weekLabel = new Label(Localization.get("label.week"));
//        HBox weekControls = new HBox(4, dayDown, displayDay, dayUp);
//        weekControls.setAlignment(Pos.CENTER);
//        VBox weekBox = new VBox(2, dayLabel, dayControls);
//        weekBox.setAlignment(Pos.CENTER);
//        
        // ── Month controls ────────────────────────────────────────────────
        Button monthUp   = new Button("+");
        Button monthDown = new Button("-");
        monthLabel = new Label(Localization.get("label.month"));
        HBox monthControls = new HBox(4, monthDown, displayMonth, monthUp);
        monthControls.setAlignment(Pos.CENTER);
        VBox monthBox = new VBox(2, monthLabel, monthControls);
        monthBox.setAlignment(Pos.CENTER);

        // ── Year controls ─────────────────────────────────────────────────
        Button yearUp   = new Button("+");
        Button yearDown = new Button("-");
        yearLabel = new Label(Localization.get("label.year"));
        HBox yearControls = new HBox(4, yearDown, displayYear, yearUp);
        yearControls.setAlignment(Pos.CENTER);
        VBox yearBox = new VBox(2, yearLabel, yearControls);
        yearBox.setAlignment(Pos.CENTER);

        HBox dateControls = new HBox(16, dayBox, monthBox, yearBox);
        dateControls.setAlignment(Pos.CENTER);
        dateControls.setPadding(new Insets(8));

        // ── Date display ───────────────────────────────────────────────
        dateLabel = new Label(Localization.get("label.date"));
        
        // ── Weather display ───────────────────────────────────────────────
        weatherLabel = new Label(Localization.get("label.weather"));
        miscLabel    = new Label(Localization.get("label.misc"));
        VBox weatherDisplay = new VBox(4, date, weatherLabel, weatherData, miscLabel, miscTextBox);
        weatherDisplay.setAlignment(Pos.CENTER_LEFT);
        weatherDisplay.setPadding(new Insets(8));

        // ── Comment display ───────────────────────────────────────────────
        commentLabel = new Label(Localization.get("label.comment"));
        commentBox = new TextArea();
        commentBox.setPromptText(Localization.get("prompt.comment"));
        commentBox.setPrefHeight(200);
        commentBox.setWrapText(true);
        VBox commentArea = new VBox(4, commentLabel, commentBox);
        commentArea.setAlignment(Pos.BOTTOM_CENTER);

        // ── Buttons ───────────────────────────────────────────────────────
        printToFile  = new Button(Localization.get("button.printfile"));
        langToggle   = new Button(Localization.get("button.lang"));

        // ── Graph panel ───────────────────────────────────────────────────
        graphCanvas = new Canvas(GRAPH_WIDTH, GRAPH_HEIGHT);
        VBox graphPanel = new VBox(graphCanvas);
        graphPanel.setPadding(new Insets(8));
        graphPanel.setStyle("-fx-background-color: white; -fx-border-color: #aaaaaa; -fx-border-width: 0 0 0 1;");

        // ── Root layout ───────────────────────────────────────────────────
        VBox center = new VBox(12, areaBox, dateControls, weatherDisplay, commentArea);
        center.setPadding(new Insets(12));

        HBox bottomBar = new HBox(8, langToggle, printToFile);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setCenter(center);
        root.setRight(graphPanel);
        root.setBottom(bottomBar);

        // ── Event handlers ────────────────────────────────────────────────
        dropDownNations.setOnAction(e -> updateWeather(listOfWeather));
        calendarSystem.setOnAction(e -> updateDate());

        yearUp.setOnAction(e -> { updateYear(1); refreshGui(); });
        yearDown.setOnAction(e -> { updateYear(-1); refreshGui(); });

        monthUp.setOnAction(e -> { updateMonth(1); refreshGui(); });
        monthDown.setOnAction(e -> { updateMonth(-1); refreshGui(); });

        dayUp.setOnAction(e -> { updateDay(1); refreshGui(); });
        dayDown.setOnAction(e -> { updateDay(-1); refreshGui(); });

        dropDownNations.setOnAction(e -> refreshGui());
        calendarSystem.setOnAction(e -> refreshGui());

        printToFile.setOnAction(e -> {updateMonth(-1); updateDisplays(displayYear, displayMonth, displayDay); updateWeather(listOfWeather); });

        langToggle.setOnAction(e -> { Localization.setLangFromString(Localization.getLang() == Lang.SV ? "EN" : "SV"); refreshGui(); });

        primaryStage.setOnCloseRequest(e -> {
            commentHandler.save(comments);
            Logger.log(LogLevel.DEBUG, 1, "saving area: " + dropDownNations.getValue());
            Logger.log(LogLevel.DEBUG, 1, "saving year-month-day: " + year + "-" + month + "-" + day);
            commentHandler.saveSession(year, month, day, dropDownNations.getValue());
        });
        updateDate();

// ── Show stage ────────────────────────────────────────────────────
        Scene scene = new Scene(root, windowWidth, windowHeight);
        primaryStage.setScene(scene);
        primaryStage.show();

        nextComment();
        updateWeather(listOfWeather);
    }

    private void refreshGui() {
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

        updateDisplays(displayYear, displayMonth, displayDay);
        updateDate();
        updateWeather(listOfWeather);
    }

    public void updateWeather(LinkedList<Day> weatherList) {
        nation = listOfNations[dropDownNations.getSelectionModel().getSelectedIndex()];
        nationData = fileHandler.getNation(nation);

        Day test = newCalc.getWeather(year, month, day, nationData);
        DecimalFormat df = new DecimalFormat("##");

        String text = Localization.get("weather.temp") + ": " + df.format(test.getTemperature()) + "C"
                + "   " + Localization.get("weather.wind") + ": " + test.getWindStrength()
                + " (" + test.getDirection() + ")"
                + "   " + Localization.get("weather.rain") + ": " + test.getRain();

        weatherData.setText(text);
        miscTextBox.setText(test.getOther());
        drawGraph();
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
        if (day > 28) { day = 1;  month++; }
        else if (day < 1) { day = 28; month--; }

        if (month > 12) { month -= 12; year++; }
        else if (month < 1) { month += 12; year--; }

        newCalc.setDateSerial(newCalc.calculateDateSerial(year, month, day));

        nextComment();
    }
    
    public void updateDate() {
        int idx = calendarSystem.getSelectionModel().getSelectedIndex();
        Calendar activeCalendar = newCalc.getCalendars().get(idx);
        date.setText(activeCalendar.toString(newCalc.getDateSerial()));
    }

    public void updateMonth(int in) {
        oldComment();
        month += in;
        if (month > 12) { month -= 12; year++; }
        else if (month < 1) { month += 12; year--; }
        newCalc.setDateSerial(newCalc.calculateDateSerial(year, month, day));
        nextComment();
    }

    public void updateYear(int in) {
        oldComment();
        year += in;
        newCalc.setDateSerial(newCalc.calculateDateSerial(year, month, day));
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
