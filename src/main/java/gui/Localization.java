package gui;

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
            case "weather.rain":      return "Nederbörd";
            case "title":             return "Eon Vädergenerator";
            case "button.graph":      return "Dygnsgraf";
            case "day.1":			  return "Söndag";
            case "day.2":			  return "Måndag";
            case "day.3":			  return "Tisdag";
            case "day.4":			  return "Onsdag";
            case "day.5":			  return "Torsdag";
            case "day.6":			  return "Fredag";
            case "day.7":			  return "Lördag";
            case "tier.holiest":      return "Heligaste dag";
            case "tier.holy":         return "Helig dag";
            case "tier.unholy":       return "Ohelig dag";
            case "tier.unholiest":    return "Oheligaste dag";
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
            case "weather.rain":      return "Precipitation";
            case "title":             return "Eon Weather Generator";
            case "button.graph":      return "Day Graph";
            case "day.1":			  return "Sunday";
            case "day.2":			  return "Monday";
            case "day.3":			  return "Tuseday";
            case "day.4":			  return "Wednesday";
            case "day.5":			  return "Thursday";
            case "day.6":			  return "Friday";
            case "day.7":			  return "Saturday";
            case "tier.holiest":      return "Holiest day";
            case "tier.holy":         return "Holy day";
            case "tier.unholy":       return "Unholy Day";
            case "tier.unholiest":    return "Unholies Day";
            default:                  return "?" + key + "?";
        }
    }
}
