package util;

import exceptions.InvalidConfigFormatException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Command {
    public static void printHelp(){
        System.out.println("Usage:");
        System.out.println("java -jar JProxy.jar -s -f <config_file>: Run as server");
        System.out.println("java -jar JProxy.jar -c -f <config_file>: Run as client");
    }
    public static void printConfigFormat(){
        System.out.println("Invalid config file format");
    }
    public static Map<String,String> parseArgs(String[] argv){
        Map<String,String> m = new HashMap<>();
        for(int i=0;i<argv.length;++i){
            if(argv[i].startsWith("-") && argv[i].length()>1) {
                if (i + 1 == argv.length || argv[i+1].startsWith("-"))
                    m.put(argv[i].substring(1), "DEF");
                else{
                    m.put(argv[i].substring(1), argv[i+1]);
                }
            }
        }
        return m;
    }
    public static Map<String,String> parseConfig(File f) throws Exception {
        BufferedReader fr = new BufferedReader(new FileReader(f));
        String line;
        Map<String,String> m = new HashMap<>();
        while((line=fr.readLine())!=null){
            String[] pair=line.split("=");
            if(pair.length!=2)  throw new InvalidConfigFormatException();
            m.put(pair[0],pair[1]);
        }
        return m;
    }
}
