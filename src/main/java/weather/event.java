package weather;

public class event {
    private String name;
    private int occurs;
    private int days;
    private int bonus_temp;
    private int bonus_wind;
    private int bonus_rain;

    public event(String name, int occurs, int days, int bonus_wind, int bonus_temp, int bonus_rain){
        this.name = name; this.occurs = occurs; this.days = days;
        this.bonus_wind = bonus_wind; this.bonus_temp = bonus_temp; this.bonus_rain = bonus_rain;
    }

    public int getOccurs(){ return occurs; }
    public int getDays()  { return days; }
    public String getName(){ return name; }
    public int getBonusTemp() { return bonus_temp; }
    public int getBonusWind() { return bonus_wind; }
    public int getbonusRain() { return bonus_rain; }
}
