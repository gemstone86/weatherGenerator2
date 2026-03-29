package gui;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import context.CampaignLoader;
import context.CommentLoader;
import context.LogLevel;
import context.Logger;
import context.ReligiousDate;
import context.fileHandler;
import date.Calendar;
import date.Day;
import date.InfluxCalculator;
import weather.Nation;
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
    TextArea campaignNoteBox;

    Nation nationData;
    fileHandler fileHandler;
    ComboBox<String> dropDownNations;
    ComboBox<String> calendarSystem;
    ComboBox<String> campaignDropdown;
    String[] listOfNations;

    HashMap<String, String> comments = new HashMap<>();
    HashMap<String, String> campaignNotes = new HashMap<>();
    CommentLoader commentHandler;
    CampaignLoader campaignLoader;
    String currentCampaign = null; // tracks the loaded campaign, independent of dropdown selection
    Button newCampaignBtn;
    Button renameCampaignBtn;
    Button deleteCampaignBtn;

    weatherCalculator newCalc;

    Label calendarLabel, areaLabel, dateLabel, weatherLabel, miscLabel, commentLabel,
          dayLabel, weekLabel, monthLabel, yearLabel, campaignLabel;
    Button printToFile, langToggle;
    Stage primaryStage;
    TextField date;
    TextField weatherData;
    TextArea miscTextBox;
    Canvas graphCanvas;
    Canvas calendarCanvas;
    private int windowWidth = 900;
    private int windowHeight = 750;
    static final int GRAPH_WIDTH  = 400;
    static final int GRAPH_HEIGHT = 500;
    static final int CAL_WIDTH    = 400;
    static final int CAL_HEIGHT   = 160;

    private LinkedList<Day> listOfWeather;
    TextField displayYear, displayMonth, displayDay;


    public GuiApp(fileHandler fileHandler, final LinkedList<Day> listOfWeather,
                  int start_year, int start_month, int start_day, String nation, Stage primaryStage,
                  weatherCalculator newCalc, CommentLoader commentHandler, String initialCampaign) {

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

        String basePath = fileHandler.getBasePath();
        campaignLoader = new CampaignLoader(basePath);

        year = start_year;
        month = start_month;
        day = start_day;
        primaryStage.getIcons().add(
        	    new javafx.scene.image.Image(
        	        getClass().getResourceAsStream("/icon.png")
        	    )
        	);
        
        primaryStage.setTitle(Localization.get("title"));

        // ── Nation selector ──────────────────────────────────────────────
        listOfNations = fileHandler.getListOfNations();
        dropDownNations = new ComboBox<>();
        for (String n : listOfNations) dropDownNations.getItems().add(n);
        dropDownNations.getSelectionModel().select(nation);
        if (dropDownNations.getSelectionModel().getSelectedIndex() < 0)
            dropDownNations.getSelectionModel().selectFirst();

        calendarSystem = new ComboBox<>();
        for (Calendar n : newCalc.getCalendars())
            calendarSystem.getItems().add(n.getName());
        calendarSystem.getSelectionModel().selectFirst();

        areaLabel = new Label(Localization.get("label.area"));
        calendarLabel = new Label(Localization.get("label.calendar"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox areaBox = new HBox(8, areaLabel, dropDownNations, spacer, calendarLabel, calendarSystem);
        areaBox.setAlignment(Pos.CENTER_LEFT);
        areaBox.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(areaBox, Priority.NEVER);

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

        // ── Event box controls ────────────────────────────────────────────
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

        // ── Date display ──────────────────────────────────────────────────
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
        commentBox.setPrefHeight(120);
        commentBox.setWrapText(true);
        VBox commentArea = new VBox(4, commentLabel, commentBox);
        commentArea.setAlignment(Pos.BOTTOM_CENTER);

        // ── Campaign notes ────────────────────────────────────────────────
        campaignLabel = new Label(Localization.get("label.campaign"));
        campaignDropdown = new ComboBox<>();
        campaignDropdown.getItems().add(Localization.get("campaign.none"));
        for (String c : campaignLoader.listCampaigns())
            campaignDropdown.getItems().add(c);
        campaignDropdown.getSelectionModel().selectFirst();

        newCampaignBtn    = new Button(Localization.get("button.newcampaign"));
        renameCampaignBtn = new Button(Localization.get("button.renamecampaign"));
        deleteCampaignBtn = new Button(Localization.get("button.deletecampaign"));

        HBox campaignHeader = new HBox(8, campaignLabel, campaignDropdown, newCampaignBtn, renameCampaignBtn, deleteCampaignBtn);
        campaignHeader.setAlignment(Pos.CENTER_LEFT);

        campaignNoteBox = new TextArea();
        campaignNoteBox.setPromptText(Localization.get("prompt.campaign"));
        campaignNoteBox.setPrefHeight(120);
        campaignNoteBox.setWrapText(true);
        campaignNoteBox.setVisible(false);
        campaignNoteBox.setManaged(false);

        // ── Restore last campaign from session ────────────────────────────
        if (initialCampaign != null && !initialCampaign.isBlank()
                && campaignDropdown.getItems().contains(initialCampaign)) {
            campaignDropdown.getSelectionModel().select(initialCampaign);
            currentCampaign = initialCampaign;
            campaignNotes = campaignLoader.load(initialCampaign);
            campaignNoteBox.setVisible(true);
            campaignNoteBox.setManaged(true);
            // Restore this campaign's saved date
            int[] cDate = campaignLoader.loadCampaignSession(initialCampaign);
            if (cDate != null) {
                year = cDate[0]; month = cDate[1]; day = cDate[2];
                syncDate();
            }
        }

        VBox campaignArea = new VBox(4, campaignHeader, campaignNoteBox);
        campaignArea.setAlignment(Pos.BOTTOM_CENTER);

        // ── Buttons ───────────────────────────────────────────────────────
        printToFile = new Button(Localization.get("button.printfile"));
        langToggle  = new Button(Localization.get("button.lang"));

        // ── Graph panel ───────────────────────────────────────────────────
        graphCanvas    = new Canvas(GRAPH_WIDTH, GRAPH_HEIGHT);
        calendarCanvas = new Canvas(CAL_WIDTH, CAL_HEIGHT);

        calendarCanvas.setOnMouseClicked(e -> {
            int clickedDay = calendarHitTest(e.getX(), e.getY());
            if (clickedDay >= 1 && clickedDay <= 28) {
                oldComment();
                oldCampaignNote();
                day = clickedDay;
                syncDate();
                nextComment();
                nextCampaignNote();
                refreshGui();
            }
        });

        VBox graphPanel = new VBox(4, calendarCanvas, graphCanvas);
        graphPanel.setPadding(new Insets(8));
        graphPanel.setStyle("-fx-background-color: white; -fx-border-color: #aaaaaa; -fx-border-width: 0 0 0 1;");

        // ── Root layout ───────────────────────────────────────────────────
        VBox center = new VBox(12, areaBox, dateControls, weatherDisplay, commentArea, campaignArea);
        center.setPadding(new Insets(12));
        center.setMaxWidth(Double.MAX_VALUE);

        HBox bottomBar = new HBox(8, langToggle, printToFile);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setCenter(center);
        root.setRight(graphPanel);
        root.setBottom(bottomBar);

        // ── Event handlers ────────────────────────────────────────────────
        yearUp.setOnAction(e -> { updateYear(1); refreshGui(); });
        yearDown.setOnAction(e -> { updateYear(-1); refreshGui(); });
        monthUp.setOnAction(e -> { updateMonth(1); refreshGui(); });
        monthDown.setOnAction(e -> { updateMonth(-1); refreshGui(); });
        dayUp.setOnAction(e -> { updateDay(1); refreshGui(); });
        dayDown.setOnAction(e -> { updateDay(-1); refreshGui(); });

        dropDownNations.setOnAction(e -> refreshGui());
        calendarSystem.setOnAction(e -> refreshGui());

        campaignDropdown.setOnAction(e -> {
            // Flush and save the current campaign before switching
            if (currentCampaign != null) {
                campaignNotes.put(getDaySeed(), campaignNoteBox.getText());
                campaignLoader.save(currentCampaign, campaignNotes);
                campaignLoader.saveCampaignSession(currentCampaign, year, month, day);
            }

            // Load the newly selected campaign
            String selected = getSelectedCampaign();
            currentCampaign = selected;
            if (selected != null) {
                campaignNotes = campaignLoader.load(selected);
                campaignNoteBox.setVisible(true);
                campaignNoteBox.setManaged(true);
                // Restore this campaign's saved date if available
                int[] cDate = campaignLoader.loadCampaignSession(selected);
                if (cDate != null) {
                    oldComment();
                    year = cDate[0]; month = cDate[1]; day = cDate[2];
                    syncDate();
                    nextComment();
                }
            } else {
                campaignNotes.clear();
                campaignNoteBox.setVisible(false);
                campaignNoteBox.setManaged(false);
            }
            nextCampaignNote();
            refreshGui();
        });

        newCampaignBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(Localization.get("dialog.newcampaign.title"));
            dialog.setHeaderText(null);
            dialog.setContentText(Localization.get("dialog.newcampaign.prompt"));
            dialog.showAndWait().ifPresent(name -> {
                if (name.isBlank()) return;
                if (campaignLoader.createCampaign(name)) {
                    campaignDropdown.getItems().add(name);
                    campaignDropdown.getSelectionModel().select(name);
                }
            });
        });

        renameCampaignBtn.setOnAction(e -> {
            if (currentCampaign == null) return;
            TextInputDialog dialog = new TextInputDialog(currentCampaign);
            dialog.setTitle(Localization.get("dialog.renamecampaign.title"));
            dialog.setHeaderText(null);
            dialog.setContentText(Localization.get("dialog.renamecampaign.prompt"));
            dialog.showAndWait().ifPresent(newName -> {
                if (newName.isBlank() || newName.equals(currentCampaign)) return;
                // Flush and save current notes before rename
                oldCampaignNote();
                campaignLoader.save(currentCampaign, campaignNotes);
                campaignLoader.saveCampaignSession(currentCampaign, year, month, day);
                String oldName = currentCampaign;
                if (campaignLoader.renameCampaign(oldName, newName)) {
                    int idx = campaignDropdown.getItems().indexOf(oldName);
                    campaignDropdown.getItems().set(idx, newName);
                    currentCampaign = newName;
                    campaignDropdown.getSelectionModel().select(newName);
                }
            });
        });

        deleteCampaignBtn.setOnAction(e -> {
            if (currentCampaign == null) return;
            String name = currentCampaign;

            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.CONFIRMATION);
            confirm.setTitle(Localization.get("dialog.deletecampaign.title"));
            confirm.setHeaderText(Localization.get("dialog.deletecampaign.header") + " \"" + name + "\"?");

            javafx.scene.control.ButtonType btnMerge  = new javafx.scene.control.ButtonType(Localization.get("dialog.deletecampaign.merge"));
            javafx.scene.control.ButtonType btnDelete = new javafx.scene.control.ButtonType(Localization.get("dialog.deletecampaign.delete"));
            javafx.scene.control.ButtonType btnCancel = new javafx.scene.control.ButtonType(
                    Localization.get("dialog.deletecampaign.cancel"),
                    javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(btnMerge, btnDelete, btnCancel);

            confirm.showAndWait().ifPresent(result -> {
                if (result == btnCancel) return;

                // Flush current notes into map
                oldCampaignNote();

                if (result == btnMerge) {
                    // Merge campaign notes into the main comments map
                    for (java.util.Map.Entry<String, String> entry : campaignNotes.entrySet()) {
                        if (entry.getValue() == null || entry.getValue().isBlank()) continue;
                        String existing = comments.getOrDefault(entry.getKey(), "");
                        if (existing.isBlank()) {
                            comments.put(entry.getKey(), entry.getValue());
                        } else {
                            comments.put(entry.getKey(), existing + "\n" + entry.getValue());
                        }
                    }
                    nextComment();
                }

                // Clear state BEFORE touching the dropdown so setOnAction
                // can't fire a save of the just-deleted campaign
                currentCampaign = null;
                campaignNotes.clear();
                campaignNoteBox.setText("");
                campaignNoteBox.setVisible(false);
                campaignNoteBox.setManaged(false);

                // Now safe to archive and update the dropdown
                campaignLoader.deleteCampaign(name);
                campaignDropdown.getItems().remove(name);
                campaignDropdown.getSelectionModel().selectFirst();
                drawCalendar();
            });
        });

        printToFile.setOnAction(e -> { updateMonth(-1); refreshGui(); });
        langToggle.setOnAction(e -> {
            Localization.setLangFromString(Localization.getLang() == Lang.SV ? "EN" : "SV");
            refreshGui();
        });

        primaryStage.setOnCloseRequest(e -> {
            commentHandler.save(comments);
            oldCampaignNote();
            if (currentCampaign != null) {
                campaignLoader.save(currentCampaign, campaignNotes);
                campaignLoader.saveCampaignSession(currentCampaign, year, month, day);
            }
            Logger.log(LogLevel.DEBUG, 1, "saving area: " + dropDownNations.getValue());
            Logger.log(LogLevel.DEBUG, 1, "saving year-month-day: " + year + "-" + month + "-" + day);
            commentHandler.saveSession(year, month, day, dropDownNations.getValue(),
                    currentCampaign != null ? currentCampaign : "");
        });
        updateDate();

        // ── Show stage ────────────────────────────────────────────────────
        Scene scene = new Scene(root, windowWidth, windowHeight);
        primaryStage.setScene(scene);
        primaryStage.show();

        nextComment();
        nextCampaignNote();
        updateWeather(listOfWeather);
    }

    // ── Campaign helpers ──────────────────────────────────────────────────

    /** Returns the selected campaign name, or null if "(none)" is selected. */
    private String getSelectedCampaign() {
        String sel = campaignDropdown.getSelectionModel().getSelectedItem();
        if (sel == null || sel.equals(Localization.get("campaign.none"))) return null;
        return sel;
    }

    public void oldCampaignNote() {
        if (currentCampaign == null) return;
        campaignNotes.put(getDaySeed(), campaignNoteBox.getText());
    }

    public void nextCampaignNote() {
        if (currentCampaign == null) {
            campaignNoteBox.setText("");
            return;
        }
        campaignNoteBox.setText(campaignNotes.getOrDefault(getDaySeed(), ""));
    }

    // ── Core GUI helpers ──────────────────────────────────────────────────

    private void syncDate() {
        newCalc.setDateSerial(newCalc.calculateDateSerial(year, month, day));
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
        campaignLabel.setText(Localization.get("label.campaign"));

        commentBox.setPromptText(Localization.get("prompt.comment"));
        campaignNoteBox.setPromptText(Localization.get("prompt.campaign"));
        printToFile.setText(Localization.get("button.printfile"));
        langToggle.setText(Localization.get("button.lang"));
        newCampaignBtn.setText(Localization.get("button.newcampaign"));
        renameCampaignBtn.setText(Localization.get("button.renamecampaign"));
        deleteCampaignBtn.setText(Localization.get("button.deletecampaign"));

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
        drawGraph(test);
        drawCalendar();
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
        oldCampaignNote();

        day += in;
        if (day > 28) { day = 1;  month++; }
        else if (day < 1) { day = 28; month--; }
        if (month > 12) { month -= 12; year++; }
        else if (month < 1) { month += 12; year--; }

        syncDate();
        nextComment();
        nextCampaignNote();
    }

    public void updateDate() {
        int idx = calendarSystem.getSelectionModel().getSelectedIndex();
        Calendar activeCalendar = newCalc.getCalendars().get(idx);
        int serial = newCalc.getDateSerial();

        String influx = "";
        InfluxCalculator ic = newCalc.getInfluxCalculator();
        if (ic != null) influx = ic.getInfluxString(year, month, day, serial);

        date.setText(activeCalendar.toString(serial, influx));
    }

    public void updateMonth(int in) {
        oldComment();
        oldCampaignNote();

        month += in;
        if (month > 12) { month -= 12; year++; }
        else if (month < 1) { month += 12; year--; }

        syncDate();
        nextComment();
        nextCampaignNote();
    }

    public void updateYear(int in) {
        oldComment();
        oldCampaignNote();

        year += in;

        syncDate();
        nextComment();
        nextCampaignNote();
    }

    private void updateDisplays(TextField displayYear, TextField displayMonth, TextField displayDay) {
        displayYear.setText(String.valueOf(year));
        displayMonth.setText(String.valueOf(month));
        displayDay.setText(String.valueOf(day));
    }

    private void drawGraph(Day daily) {
        double[][] hourly = newCalc.getHourlyWeather(year, month, day,
                fileHandler.getNation(nation), daily);
        double[] temps = hourly[0];
        double[] winds = hourly[1];
        double[] rains = hourly[2];

        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        double w = GRAPH_WIDTH;
        double panelH = GRAPH_HEIGHT;
        double graphH = (panelH - 60) / 3.0;
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

            double min = data[0], max = data[0];
            for (double v : data) { if (v < min) min = v; if (v > max) max = v; }
            if (max == min) { max = min + 1; }

            gc.setFill(Color.rgb(245, 245, 245));
            gc.fillRect(padL, offsetY + padTop, innerW, graphH - padTop - padBot);

            gc.setStroke(Color.rgb(200, 200, 200));
            gc.setLineWidth(0.5);
            for (int i = 0; i <= 2; i++) {
                double gy = offsetY + padTop + (graphH - padTop - padBot) * i / 2.0;
                gc.strokeLine(padL, gy, padL + innerW, gy);
                double val = max - (max - min) * i / 2.0;
                gc.setFill(Color.GRAY);
                gc.setFont(javafx.scene.text.Font.font(9));
                gc.fillText(String.format("%.0f", val), 0, gy + 3);
            }

            gc.setFill(Color.DARKGRAY);
            gc.setFont(javafx.scene.text.Font.font(10));
            gc.fillText(labels[g], padL, offsetY + 11);

            if (g == 2) {
                gc.setFill(Color.GRAY);
                gc.setFont(javafx.scene.text.Font.font(8));
                for (int h = 0; h < 24; h += 4) {
                    double x = padL + h * innerW / 23.0;
                    gc.fillText(String.valueOf(h), x - 3, offsetY + graphH + 12);
                }
            }

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

            gc.setFill(colors[g]);
            for (int h = 0; h < 24; h++) {
                double x = padL + h * innerW / 23.0;
                double norm = (data[h] - min) / (max - min);
                double y = offsetY + padTop + (graphH - padTop - padBot) * (1 - norm);
                gc.fillOval(x - 2, y - 2, 4, 4);
            }
        }
    }

    // ── Calendar grid ─────────────────────────────────────────────────────

    private int calendarHitTest(double px, double py) {
        double cellW = CAL_WIDTH / 8.0;
        double headerH = 24;
        double rowH = (CAL_HEIGHT - headerH) / 4.0;
        if (py < headerH) return -1;
        int col = (int)(px / cellW) - 1;
        int row = (int)((py - headerH) / rowH);
        if (col < 0 || col > 6 || row < 0 || row > 3) return -1;
        return row * 7 + col + 1;
    }

    private void drawCalendar() {
        GraphicsContext gc = calendarCanvas.getGraphicsContext2D();
        double w = CAL_WIDTH;
        double h = CAL_HEIGHT;
        double headerH = 24;
        double cellW = w / 8.0;
        double rowH = (h - headerH) / 4.0;

        gc.clearRect(0, 0, w, h);

        // ── Header ────────────────────────────────────────────────────────
        gc.setFill(Color.rgb(70, 110, 170));
        gc.fillRect(0, 0, w, headerH);
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("SansSerif", javafx.scene.text.FontWeight.BOLD, 13));
        String monthName = Localization.get("month." + month);
        gc.fillText(monthName + "  " + year, w / 2 - 50, headerH - 6);

        // ── Day-of-week header ────────────────────────────────────────────
        String[] dow = {
            Localization.get("cal.wk"),
            Localization.get("day.2"), Localization.get("day.3"),
            Localization.get("day.4"), Localization.get("day.5"),
            Localization.get("day.6"), Localization.get("day.7"),
            Localization.get("day.1")
        };
        gc.setFont(javafx.scene.text.Font.font("SansSerif", javafx.scene.text.FontWeight.BOLD, 10));
        for (int c = 0; c < 8; c++) {
            double cx = c * cellW + cellW / 2;
            if (c == 7) gc.setFill(Color.rgb(100, 140, 200));
            else        gc.setFill(Color.rgb(50, 50, 80));
            gc.fillText(dow[c], cx - gc.getFont().getSize() * dow[c].length() * 0.28, headerH + 12);
        }

        // ── Day cells ────────────────────────────────────────────────────
        int firstDayOfMonth = (month - 1) * 28 + 1;
        int startWeek = (firstDayOfMonth - 1) / 7 + 1;
        boolean hasCampaign = currentCampaign != null;

        for (int row = 0; row < 4; row++) {
            double rowY = headerH + 14 + row * rowH;
            int weekNum = startWeek + row;

            // Week number column
            gc.setFill(Color.rgb(180, 200, 230));
            gc.fillRect(0, headerH + row * rowH, cellW, rowH);
            gc.setFill(Color.rgb(50, 70, 120));
            gc.setFont(javafx.scene.text.Font.font("SansSerif", javafx.scene.text.FontWeight.BOLD, 10));
            gc.fillText(String.valueOf(weekNum), cellW / 2 - 5, rowY);

            for (int col = 0; col < 7; col++) {
                int d = row * 7 + col + 1;
                double cellX = (col + 1) * cellW;
                double cellY = headerH + row * rowH;
                boolean isToday  = (d == day);
                boolean isSunday = (col == 6);

                // Religious tier
                ReligiousDate.Tier holyTier = null;
                for (ReligiousDate rd : newCalc.getReligiousDates()) {
                    if (rd.matches(year, month, d)) {
                        if (holyTier == null) {
                            holyTier = rd.tier;
                        } else if (rd.tier == ReligiousDate.Tier.HOLIEST
                                || rd.tier == ReligiousDate.Tier.UNHOLIEST) {
                            holyTier = rd.tier;
                        }
                    }
                }

                // Cell background
                if (isToday) {
                    gc.setFill(Color.rgb(70, 110, 170));
                } else if (holyTier == ReligiousDate.Tier.HOLIEST) {
                    gc.setFill(Color.rgb(200, 40, 40));
                } else if (holyTier == ReligiousDate.Tier.HOLY) {
                    gc.setFill(Color.rgb(230, 130, 130));
                } else if (holyTier == ReligiousDate.Tier.UNHOLIEST) {
                    gc.setFill(Color.rgb(80, 0, 80));
                } else if (holyTier == ReligiousDate.Tier.UNHOLY) {
                    gc.setFill(Color.rgb(180, 130, 200));
                } else if (isSunday) {
                    gc.setFill(Color.rgb(235, 240, 255));
                } else {
                    gc.setFill(Color.WHITE);
                }
                gc.fillRect(cellX, cellY, cellW, rowH);

                // Cell border
                gc.setStroke(Color.rgb(210, 210, 220));
                gc.setLineWidth(0.5);
                gc.strokeRect(cellX, cellY, cellW, rowH);

                // Text colour
                Color textColor;
                if (isToday) {
                    textColor = Color.WHITE;
                } else if (holyTier == ReligiousDate.Tier.HOLIEST || holyTier == ReligiousDate.Tier.UNHOLIEST) {
                    textColor = Color.WHITE;
                } else if (holyTier != null) {
                    textColor = Color.rgb(80, 0, 0);
                } else if (isSunday) {
                    textColor = Color.rgb(70, 110, 200);
                } else {
                    textColor = Color.rgb(30, 30, 30);
                }

                gc.setFont(javafx.scene.text.Font.font("SansSerif",
                    isToday ? javafx.scene.text.FontWeight.BOLD : javafx.scene.text.FontWeight.NORMAL, 11));
                gc.setFill(textColor);

                // Build cell label: day number + * for comment + c for campaign note
                String commentKey = year + "-" + month + "-" + d;
                boolean hasComment = comments.containsKey(commentKey)
                                  && !comments.get(commentKey).isBlank();
                boolean hasCNote   = hasCampaign
                                  && campaignNotes.containsKey(commentKey)
                                  && !campaignNotes.get(commentKey).isBlank();

                String cellLabel = String.valueOf(d);
                if (hasComment && hasCNote)  cellLabel += "\n* c";
                else if (hasComment)         cellLabel += "\n*";
                else if (hasCNote)           cellLabel += "\nc";

                gc.fillText(cellLabel, cellX + cellW / 2 - 5, rowY);
            }
        }
    }

}