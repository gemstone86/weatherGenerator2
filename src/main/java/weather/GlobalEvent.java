package weather;

/**
 * Represents a global event that can happen in any region.
 * Conditions (start_month, end_month, min_wind, max_wind) are optional.
 * Bonus fields affect weather calculation for that day.
 */
public class GlobalEvent {
    private String name;
    private int occurs;
    private int days;

    // Conditions (defaults = no restriction)
    private int startMonth; // 0 = no restriction
    private int endMonth;   // 0 = no restriction
    private int minWind;    // 0 = no restriction
    private int maxWind;    // 999 = no restriction

    // Bonuses applied when event triggers
    private int bonusWind;
    private int bonusTemp;
    private int bonusRain;

    public GlobalEvent(String name, int occurs, int days,
                       int startMonth, int endMonth,
                       int minWind, int maxWind,
                       int bonusWind, int bonusTemp, int bonusRain) {
        this.name       = name;
        this.occurs     = occurs;
        this.days       = days;
        this.startMonth = startMonth;
        this.endMonth   = endMonth;
        this.minWind    = minWind;
        this.maxWind    = maxWind;
        this.bonusWind  = bonusWind;
        this.bonusTemp  = bonusTemp;
        this.bonusRain  = bonusRain;
    }

    /**
     * Check if this event's conditions are met for the given month and wind.
     */
    public boolean conditionsMet(int month, int wind) {
        // Check wind range
        if (wind < minWind || wind > maxWind) return false;

        // Check month range (handles wrap-around e.g. Oct-Feb)
        if (startMonth != 0 && endMonth != 0) {
            if (startMonth <= endMonth) {
                if (month < startMonth || month > endMonth) return false;
            } else {
                // Wraps around year end e.g. Oct(10) to Feb(2)
                if (month < startMonth && month > endMonth) return false;
            }
        }

        return true;
    }

    public String getName()     { return name; }
    public int getOccurs()      { return occurs; }
    public int getDays()        { return days; }
    public int getBonusWind()   { return bonusWind; }
    public int getBonusTemp()   { return bonusTemp; }
    public int getBonusRain()   { return bonusRain; }
    
    public String toString() {
    	return("\t"+name + ": " + occurs + " of " + days);
    }
}
