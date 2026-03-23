package context;

public class SessionState {
    public int year;
    public int month;
    public int day;
    public String nation;
    public String lang;
    public String campaign; // last active campaign, or empty string if none

    public SessionState(int year, int month, int day, String nation, String lang, String campaign) {
        this.year     = year;
        this.month    = month;
        this.day      = day;
        this.nation   = nation;
        this.lang     = lang;
        this.campaign = campaign;
    }
}