package date;

import gui.Localization;
import weather.weatherCalculator;

public class Calendar {

    private weatherCalculator calculator;
    private int[] config;      // e.g. {336, 12, 28} as input
    private int[] unitSize;    // e.g. {336, 28, 1} — days per unit at each level
    private int epochOffset;
    
    public Calendar(weatherCalculator calculator, int[] config, int epochOffset) {
        this.calculator = calculator;
        this.config = config;
        this.epochOffset = epochOffset;
        
        // Calculate how many days each level represents
        unitSize = new int[config.length];
        unitSize[0] = config[0]; // year = total days
        for (int i = 1; i < config.length; i++) {
            unitSize[i] = unitSize[i - 1] / config[i];
        }
        // unitSize = {336, 28, 1} for standard calendar
    }

    public int[] toDate(int serial) {
        int adjusted = serial + epochOffset;
    	int[] date = new int[unitSize.length];
        int remaining = adjusted - 1;
        for (int i = 0; i < unitSize.length; i++) {
            date[i] = remaining / unitSize[i] + 1; // 1-indexed at every level
            remaining = remaining % unitSize[i];
        }
        return date;
    }

    public String toString(int serial) {
        int[] date = toDate(serial);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < date.length; i++) {
            if (i > 0) sb.append("-");
            sb.append(date[i]);
        }
        return sb.toString();
    }

    public String getDayName(int serial) {
        int day = toDate(serial)[config.length - 1]; // last unit = day
        return Localization.get("day." + ((day % 7) + 1));
    }
}