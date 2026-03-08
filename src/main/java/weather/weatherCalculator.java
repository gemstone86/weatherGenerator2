package weather;

import java.util.LinkedList;
import java.util.Random;

public class weatherCalculator {
    Random rng;
    int bonusWind = 0, bonusRain = 0, bonusTemp = 0;

    public weatherCalculator(Random rng){ this.rng = rng; }

    public void setSeed(int n){ rng.setSeed(n); }

    public int obd6(){
        int die = rng.nextInt(6)+1;
        if(die == 6) return obd6()+obd6();
        return die;
    }

    public int daySeed(int year, int month, int day){ return year*100*100+month*100+day; }

    public int windStrengthNR(int windBonus){
        int strength = obd6() + windBonus + bonusWind();
        if(strength < 0) strength = 0;
        return strength;
    }

    public int windStrength(int previousDay, int windBonus){
        int die = obd6(), strength;
        if(die > previousDay+4)      strength = previousDay+4+windBonus;
        else if(die < previousDay-4) strength = previousDay-4+windBonus;
        else                         strength = die+windBonus;
        strength += bonusWind();
        if(strength < 0) strength = 0;
        return strength;
    }

    private int bonusWind(){ int t=bonusWind; bonusWind=0; return t; }

    public double randomBetween(double start, double end){
        return (rng.nextDouble()*(end-start))+start;
    }

    public double getProceduralTemperature(double prev, double cur, double next, int day){
        double variance = randomBetween(0,5)-randomBetween(0,5);
        if(day < 15){
            double step = (cur-prev)/28;
            return (14+day)*step+prev+variance;
        } else {
            double step = (next-cur)/28;
            return (day-14)*step+cur+variance;
        }
    }

    public int rainfall(double temperature, double average, int wind, int rain){
        int limit=6, bonus=0;
        if(rain > 0) bonus += rain;
        if(wind > 5) bonus += wind/2;
        int die = obd6()+bonus+bonusRain();
        if(die < limit) return 0;
        return die-(limit-1);
    }

    private int bonusRain(){ int t=bonusRain; bonusRain=0; return t; }

    public boolean chance(int occurs, int days){
        return rng.nextInt(days-1)+1 < occurs+1;
    }

    public String generateEvents(int magistorm, int thunder, int dimma, LinkedList<event> events, int month){
        String event = "";
        if(chance(1+magistorm,365)) event += "Magistorm ";
        if(chance(1+thunder,28))    event += "Åska ";
        if(chance(1+dimma,28*2))    { event +="Lätt Dimma "; bonusWind=-4; bonusTemp=-2; }
        else if(chance(1+dimma,28*3)){ event +="Tjock Dimma "; bonusWind=-6; bonusTemp=-2; }
        if((month>9||month<3)&&chance(1,200))  { event +="Köldknäpp"; bonusTemp=-5; }
        if((month>3||month<9)&&chance(1,200))  { event +="Värmevåg"; bonusTemp=5; }
        if(chance(1,50))  event +="Molnklart ";
        if(chance(1,300)) event +="Stjärnfall ";
        if(chance(1,150)) event +="Hagel ";
        if(chance(1,300)) event +="Komet ";
        for(int i=0;i<events.size();i++){
            event temp=events.get(i);
            if(chance(1+temp.getOccurs(),temp.getDays())) event+=temp.getName()+" ";
        }
        return event;
    }

    private direction getNonRandomDirection(){
        switch(rng.nextInt(8)){
            case 1: return direction.N;  case 2: return direction.NE;
            case 3: return direction.E;  case 4: return direction.SE;
            case 5: return direction.S;  case 6: return direction.SW;
            case 7: return direction.W;  case 8: return direction.NW;
            default: return direction.N;
        }
    }

    public weather getWeather(int year, int month, int day, nationData nation){
        int previous    = nation.getTemperature(month-1);
        int average     = nation.getTemperature(month);
        int next        = nation.getTemperature(month+1);
        int averageWind = nation.getWind(month);
        int rain        = nation.getRain(month);

        rng.setSeed(daySeed(year, month, day));

        String events    = generateEvents(0,0,0,nation.getEvents(),month);
        int wind         = windStrengthNR(averageWind);
        double temperature = getProceduralTemperature(previous,average,next,day);

        return new weather(year,month,day,temperature,wind,rainfall(temperature,average,wind,rain),events,getNonRandomDirection());
    }
}
