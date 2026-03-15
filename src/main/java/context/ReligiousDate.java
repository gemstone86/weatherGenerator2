package context;

/**
 * Represents a single named date for a religion.
 */
public class ReligiousDate {
    public enum Tier { HOLIEST, HOLY, UNHOLY, UNHOLIEST }

    public final String religion;
    public final String name;
    public final int month;
    public final int day;
    public final Tier tier;

    public ReligiousDate(String religion, String name, int month, int day, Tier tier) {
        this.religion = religion;
        this.name     = name;
        this.month    = month;
        this.day      = day;
        this.tier     = tier;
    }

    public boolean matches(int month, int day) {
        return this.month == month && this.day == day;
    }
}
