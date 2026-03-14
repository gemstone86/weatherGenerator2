package context;

public class SessionState {
    public int year;
    public int month;
    public int day;
    public String nation;
    public String lang; // stored as plain String "SV" or "EN" to avoid casting issues

    public SessionState(int year, int month, int day, String nation, String lang) {
        this.year   = year;
        this.month  = month;
        this.day    = day;
        this.nation = nation;
        this.lang   = lang;
    }
}
