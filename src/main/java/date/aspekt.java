//package date;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public enum aspekt {
//    Kronotropi(0),
//	Nomotropi(1),
//    Ataxatropi(2),
//
//    Termotropi(3),
//    Kryotropi(4),
//    Fototropi(5),
//    Skototropi(6),
//    
//    Pyrotropi(7),
//    Geotropi(8),
//    Pneumotropi(9),
//    Hydrotropi(10),
//    
//    Biotropi(11),
//    Nekrotropi(12),
//    
//    Heliotropi(13),
//    Selenotropi(14),
//    Astrotropi(15);
//
//    private final int level;
//
//    aspekt(int level) {
//        this.level = level;
//    }
//
//    public int getLevel() {
//        return level;
//    }
//    
//
//    
//    public int calculateAspektBonus(int serial, int year, int month, int day) {
//        aspekt[] YEAR = {
//        	    Kronotropi, Nomotropi, Ataxatropi, Termotropi,
//        	    Kryotropi, Fototropi, Skototropi, Biotropi,
//        	    Nekrotropi, Heliotropi, Selenotropi, Astrotropi,
//        	    Pyrotropi, Geotropi, Hydrotropi, Pneumotropi
//        	};
//        aspekt[] MONTH = {Kryotropi, Nomotropi, Geotropi, Biotropi, Pyrotropi, Fototropi, Termotropi, Ataxatropi, Hydrotropi, Pneumotropi, Skototropi};
//        aspekt[] WEEK = {Selenotropi, Ataxatropi, Heliotropi, Nomotropi};
//        aspekt[] DAY = {Heliotropi, Selenotropi, Nekrotropi, Astrotropi, Pyrotropi, Biotropi, Hydrotropi};
//
//        int yearCycle = (year - 1) % 16;
//        int monthIndex = month - 1;
//        int weekdayIndex = (serial - 1) % 7;
//        int dayIndex = (day - 1) % 7;
//        
//    	List<aspekt> aspekts = List.of(
//    		    YEAR[yearCycle],
//    		    MONTH[monthIndex],
//    		    WEEK[weekdayIndex],
//    		    DAY[dayIndex]
//    		);
//
//    	Map<aspekt, Integer> count = new HashMap<>();
//
//    	for (aspekt a : aspekts) {
//    	    count.put(a, count.getOrDefault(a, 0) + 1);
//    	}
//
//        int bonus = 0;
//
//        for (Map.Entry<aspekt, Integer> entry : countMap.entrySet()) {
//            int count = entry.getValue();
//            if (count > 1) {
//                bonus += (count - 1); // or whatever scaling you want
//            }
//        }
//
//        return bonus;
//    }
//    
//    public int[] cycle(int year, int month, int day) {
//    	int yearCycle = (year - 1) % 16;
//    	int monthIndex = month - 1;
//    	int weekdayIndex = (serial - 1) % 7;
//    	int dayIndex = (day - 1) % 7;
//    }
//}
