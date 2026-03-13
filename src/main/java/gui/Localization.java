package gui;

import weather.Lang;

public class Localization {

    private static Lang currentLang = Lang.SV;

    public static void setLang(Lang lang) {
        currentLang = lang;
    }

    /** Set language from a plain string — safe to call from anywhere without Lang import. */
    public static void setLangFromString(String s) {
        if ("EN".equals(s)) { currentLang = Lang.EN; } else { currentLang = Lang.SV; }
    }

    public static Lang getLang() {
        return currentLang;
    }

    public static String get(String key) {
        if (currentLang == Lang.EN) return getEN(key);
        return getSV(key);
    }

    // ── Swedish ──────────────────────────────────────────────────────────

    private static String getSV(String key) {
        switch (key) {
            case "label.area":        return "Område:";
            case "label.day":         return "Dag";
            case "label.month":       return "Månad";
            case "label.year":        return "År";
            case "label.weather":     return "Väder";
            case "label.misc":        return "Övrigt";
            case "label.comment":     return "Kommentar";
            case "prompt.comment":    return "Skriv en kommentar...";
            case "button.printfile":  return "Skriv till fil";
            case "button.lang":       return "English";
            case "weather.temp":      return "Temperatur";
            case "weather.wind":      return "Vindstyrka";
            case "weather.rain":      return "Regnmängd";
            case "title":             return "Eon Vädergenerator";
            default:                  return "?" + key + "?";
        }
    }

    // ── English ──────────────────────────────────────────────────────────

    private static String getEN(String key) {
        switch (key) {
            case "label.area":        return "Region:";
            case "label.day":         return "Day";
            case "label.month":       return "Month";
            case "label.year":        return "Year";
            case "label.weather":     return "Weather";
            case "label.misc":        return "Other";
            case "label.comment":     return "Comment";
            case "prompt.comment":    return "Write a comment...";
            case "button.printfile":  return "Print to file";
            case "button.lang":       return "Svenska";
            case "weather.temp":      return "Temperature";
            case "weather.wind":      return "Wind strength";
            case "weather.rain":      return "Rainfall";
            case "title":             return "Eon Weather Generator";
            default:                  return "?" + key + "?";
        }
    }
}
