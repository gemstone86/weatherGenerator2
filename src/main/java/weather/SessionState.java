package weather;

/**
 * Holds the last-used date and nation, saved/loaded on exit/startup.
 */
public class SessionState {
    public int year;
    public int month;
    public int day;
    public String nation;

    public SessionState(int year, int month, int day, String nation) {
        this.year   = year;
        this.month  = month;
        this.day    = day;
        this.nation = nation;
    }
}
