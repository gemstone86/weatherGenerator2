package weather;

import context.Logger;
import context.LogLevel;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class weatherCalculator {
    Random rng;
    Random yearRng = new Random();
    Random monthRng = new Random();;
    int bonusWind = 0, bonusRain = 0, bonusTemp = 0;

    // Global events loaded from additional-events.yaml
    private List<GlobalEvent> globalEvents = new java.util.ArrayList<>();

    public weatherCalculator(Random rng) { this.rng = rng; }

    public void setGlobalEvents(List<GlobalEvent> events) { this.globalEvents = events; }
    public void setYearSeed(int n) { yearRng.setSeed(n); }
    public void setMonthSeed(int n) { monthRng.setSeed(n); }
    public void setSeed(int n) { rng.setSeed(n); }

    public int obd6() {
        int die = rng.nextInt(6) + 1;
        if (die == 6) return obd6() + obd6();
        return die;
    }

    public int daySeed(int year, int month, int day) {
        int sum = (year-1) * (12*28) + (month-1)*28 + day;
        Logger.log(LogLevel.DEBUG, 1, "Dayseed is: " + sum);
        return sum;
    }
    public int monthSeed(int year, int month) {
        int sum = (year-1) * (12*28) + (month-1)*28;
        Logger.log(LogLevel.DEBUG, 1, "Monthseed is: " + sum);
        return sum;
    }
    public int yearSeed(int year) {
        int sum = (year-1) * (12*28);
        Logger.log(LogLevel.DEBUG, 1, "Yearseed is: " + sum);
        return sum;
    }

    public double windStrengthYear(int year) {
        setYearSeed(yearSeed(year));
        return randomBetweenFrom(yearRng, 0, 1);
    }
    public double windStrengthMonth(int year, int month) {
        setMonthSeed(monthSeed(year, month));
        return randomBetweenFrom(monthRng, 0, 1);
    }
    public int windStrengthFractal(int year, int month, int windBonus) {
        int dayStrength = obd6();
        int yearStrength = (int) windStrengthYear(year);
        int monthStrength = (int) windStrengthMonth(year, month);
        int strength = (int) (dayStrength + yearStrength + monthStrength + windBonus + bonusWind());
        Logger.log(LogLevel.DEBUG, 2, "Year: " + yearStrength + " + Month: " + monthStrength + " + Day: " + dayStrength + " + bonus: " + windBonus + " = " + strength);
        if (strength < 0) strength = 0;
        return strength;
    }
    public int windStrengthNR(int windBonus) {
        int strength = obd6() + windBonus + bonusWind();
        if (strength < 0) strength = 0;
        return strength;
    }
    public int windStrength(int previousDay, int windBonus) {
        int die = obd6(), strength;
        if (die > previousDay + 4)      strength = previousDay + 4 + windBonus;
        else if (die < previousDay - 4) strength = previousDay - 4 + windBonus;
        else                            strength = die + windBonus;
        strength += bonusWind();
        if (strength < 0) strength = 0;
        return strength;
    }
    private int bonusWind() { int t = bonusWind; bonusWind = 0; return t; }

    public double randomBetweenFrom(Random rand, double start, double end) {
        return (rand.nextDouble() * (end - start)) + start;
    }
    public double randomBetween(double start, double end) {
        return (rng.nextDouble() * (end - start)) + start;
    }

    public double getProceduralTemperature(double prev, double cur, double next, int day) {
        double variance = randomBetween(-5, 5);
        if (day < 15) {
            double step = (cur - prev) / 28;
            return (14 + day) * step + prev + variance;
        } else {
            double step = (next - cur) / 28;
            return (day - 14) * step + cur + variance;
        }
    }

    public int rainfall(double temperature, double average, int wind, int rain) {
        int limit = 6, bonus = 0;
        if (rain > 0) bonus += rain;
        if (wind > 5) bonus += wind / 2;
        int die = obd6() + bonus + bonusRain();
        if (die < limit) return 0;
        return die - (limit - 1);
    }
    private int bonusRain() { int t = bonusRain; bonusRain = 0; return t; }

    public boolean chance(int occurs, int days) {
        try {
            return rng.nextInt(days - 1) + 1 < occurs + 1;
        } catch (Exception e) {
            System.out.println("event at " + days + "/" + occurs);
            return false;
        }
    }

    public String generateEvents(LinkedList<event> regionEvents, List<GlobalEvent> globalEvents, int month, int wind, double temperature) {
        StringBuilder result = new StringBuilder();
        Logger.log(LogLevel.DEBUG, 2, "Num of Global events " + globalEvents.size());

        for (GlobalEvent e : globalEvents) {
            Logger.log(LogLevel.DEBUG, 2, "Checking event: " + e.getName());
            if (e.conditionsMet(month, wind, temperature) && chance(e.getOccurs(), e.getDays())) {
                int variantIdx = e.hasVariants() ? rng.nextInt(e.getVariantCount()) : 0;
                String displayName = e.getDisplayName(variantIdx);
                if (result.isEmpty()) result.append(displayName);
                else { result.append(", "); result.append(displayName); }
                bonusWind += e.getBonusWind();
                bonusTemp += e.getBonusTemp();
                bonusRain += e.getBonusRain();
            }
        }
        for (event e : regionEvents) {
            Logger.log(LogLevel.DEBUG, 2, "Checking event: " + e.getName());
            if (chance(1 + e.getOccurs(), e.getDays())) {
                if (result.isEmpty()) result.append(e.getName());
                else { result.append(", "); result.append(e.getName()); }
            }
        }
        return result.toString();
    }

    private direction getNonRandomDirection() {
        switch (rng.nextInt(8)) {
            case 1: return direction.N;  case 2: return direction.NE;
            case 3: return direction.E;  case 4: return direction.SE;
            case 5: return direction.S;  case 6: return direction.SW;
            case 7: return direction.W;  case 8: return direction.NW;
            default: return direction.N;
        }
    }

    public weather getWeather(int year, int month, int day, nationData nation) {
        int previous    = nation.getTemperature(month - 1);
        int average     = nation.getTemperature(month);
        int next        = nation.getTemperature(month + 1);
        int averageWind = nation.getWind(month);
        int rain        = nation.getRain(month);
        rng.setSeed(daySeed(year, month, day));
        int wind = windStrengthFractal(year, month, averageWind);
        double temperature = getProceduralTemperature(previous, average, next, day);
        String events = generateEvents(nation.getEvents(), globalEvents, month, wind, temperature);
        temperature += bonusTemp;
        bonusTemp = 0;
        return new weather(year, month, day, temperature, wind,
                rainfall(temperature, average, wind, rain), events,
                getNonRandomDirection());
    }

    /**
     * Generates 24 hourly values for temperature, wind, and rain for a given day.
     * Uses a seeded RNG so the same day always produces the same hourly pattern.
     * Returns a double[3][24] where [0]=temperature, [1]=wind, [2]=rain.
     */
    public double[][] getHourlyWeather(int year, int month, int day, nationData nation) {
        weather daily = getWeather(year, month, day, nation);
        double dailyTemp = daily.getTemperature();
        int dailyWind    = daily.getWindStrength();
        int dailyRain    = daily.getRain();

        // Seed per day so results are deterministic
        Random hourRng = new Random(daySeed(year, month, day) * 31L + 7 + Math.abs(nation.getName().hashCode()));

        double[] temps = new double[24];
        double[] winds = new double[24];
        double[] rains = new double[24];

        // Temperature: smooth sine-like curve peaking at hour 14, lowest at hour 4
        // plus small seeded noise
        for (int h = 0; h < 24; h++) {
            double angle = Math.PI * 2 * (h - 4) / 24.0;
            double curve = Math.sin(angle) * 3.0; // ±3 degree swing
            double noise = (hourRng.nextDouble() - 0.5) * 2.0; // ±1 noise
            temps[h] = dailyTemp + curve + noise;
        }

        // Wind: random walk clamped around daily value
        double wind = dailyWind;
        for (int h = 0; h < 24; h++) {
            wind += (hourRng.nextDouble() - 0.5) * 2.0;
            wind = Math.max(0, Math.min(wind, dailyWind * 2.0 + 4));
            // Drift back toward daily value
            wind += (dailyWind - wind) * 0.15;
            winds[h] = wind;
        }

        // Rain: 0 if daily rain is 0, otherwise random variation around daily
        if (dailyRain == 0) {
            // All zeros
        } else {
            for (int h = 0; h < 24; h++) {
                double r = dailyRain + (hourRng.nextDouble() - 0.5) * dailyRain * 0.8;
                rains[h] = Math.max(0, r);
            }
        }

        return new double[][]{ temps, winds, rains };
    }

}
