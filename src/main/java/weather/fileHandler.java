package weather;

import java.io.*;
import java.text.DecimalFormat;
import java.util.LinkedList;

public class fileHandler {
    String basePath;
    BufferedFileReaderClass reader;
    BufferedWriter bufferedWriter;
    LinkedList<nationData> listOfNations = new LinkedList<nationData>();

    public fileHandler(String Path){
        this.basePath = Path;
        System.out.println("Here: " + basePath);
        initializeDataFiles();
    }

    public nationData readFile(String Path){
        File readFile = new File(basePath+"/src/data/"+Path);
        int[] temperature=new int[12], rainfall=new int[12], windStrength=new int[12];
        FileReader fr;
        try {
            fr = new FileReader(readFile.getAbsoluteFile());
            reader = new BufferedFileReaderClass(fr);
            String nationName = reader.readLine();
            System.out.println("Temperatur: ");
            for(int i=0;i<12;i++){ temperature[i]=reader.readNextInt(); System.out.print(temperature[i]+";"); }
            System.out.println();
            for(int i=0;i<12;i++) rainfall[i]=reader.readNextInt();
            int shift = reader.readNextInt();
            System.out.println("Wind: ");
            for(int i=0;i<12;i++){ windStrength[i]=reader.readNextInt(); System.out.print(windStrength[i]+";"); }
            System.out.println("Shift: "+shift);
            LinkedList<event> events = new LinkedList<event>();
            reader.skip(2);
            while(true){
                String specialEvent = reader.readLine();
                if(specialEvent==null) break;
                int occurs=reader.readNextInt(), days=reader.readNextInt();
                reader.skip(2);
                events.add(new event(specialEvent,occurs,days));
            }
            return new nationData(nationName,temperature,rainfall,shift,windStrength,events);
        } catch(IOException e){
            System.out.println("Couldn't read Nation file"); e.printStackTrace();
        }
        return null;
    }

    public void addToFile(String add, boolean linebreak){
        try {
            bufferedWriter.write(add);
            if(linebreak) bufferedWriter.write(System.lineSeparator());
        } catch(IOException e){ e.printStackTrace(); }
    }

    public String[] getListOfNations(){
        String[] list = new String[listOfNations.size()];
        for(int i=0;i<listOfNations.size();i++) list[i]=listOfNations.get(i).getName();
        return list;
    }

    public int[] getTemperatures(String Name){
        for(int i=0;i<listOfNations.size();i++)
            if(listOfNations.get(i).getName()==Name) return listOfNations.get(i).getTemperature();
        return null;
    }

    public int[] getWindStrength(String Name){
        for(int i=0;i<listOfNations.size();i++)
            if(listOfNations.get(i).getName()==Name) return listOfNations.get(i).getWindStrength();
        return null;
    }

    public void initializeDataFiles(){
        String Folderpath = basePath+"/src/data";
        File folder = new File(Folderpath);
        File[] listOfFiles = folder.listFiles();
        for(int i=0;i<listOfFiles.length;i++)
            if(listOfFiles[i].isFile()){ System.out.println("    "+listOfFiles[i]); listOfNations.add(readFile(listOfFiles[i].getName())); }
    }

    public void createWeatherFile(String name, int from, int to){
        try {
            File file = new File(basePath+" "+name+" ("+from+" to "+(to-1)+").txt");
            if(!file.exists()) file.createNewFile();
            bufferedWriter = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
        } catch(IOException e){ System.out.println("Couldn't create weather file at "+basePath); e.printStackTrace(); }
    }

    public void closeWeatherFile(){
        try { bufferedWriter.close(); } catch(IOException e){ e.printStackTrace(); }
    }

    public String printHeader(){ return "Year\tMonth\tDay\tWind\tTemperature\tRainfall\tOther\n"; }

    public String printData(int Year, int Month, int Day, int Wind, double temp, int Rainfall, String Other){
        DecimalFormat df = new DecimalFormat("##.#");
        return Year+"\t"+Month+"\t"+Day+"\t"+Wind+"\t"+df.format(temp)+" C"+"\t\t"+Rainfall+"\t\t"+Other+"\n";
    }

    public String[] getTemperaturesAsString(String Name){
        String[] temp = new String[12];
        for(int i=0;i<listOfNations.size();i++)
            if(listOfNations.get(i).getName()==Name){
                for(int j=0;j<12;j++) temp[j]=Integer.toString(listOfNations.get(i).getTemperature(j));
                return temp;
            }
        return null;
    }

    public nationData getNation(String nation){
        for(int i=0;i<listOfNations.size();i++)
            if(listOfNations.get(i).getName().equals(nation)) return listOfNations.get(i);
        System.out.println("Couldn't find area");
        return null;
    }
}
