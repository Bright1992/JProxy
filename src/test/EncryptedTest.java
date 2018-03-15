package test;

import exceptions.InvalidConfigFormatException;
import util.Command;

import java.io.File;
import java.util.Map;
import proxy.server.*;
import proxy.client.*;

public class EncryptedTest {
    public static void main(String[] argv) throws Exception{
        Map<String, String> args = Command.parseArgs(argv);
        if((!args.containsKey("s") && !args.containsKey("c"))||(args.containsKey("s")&& args.containsKey("c"))||!args.containsKey("f")) {
            Command.printHelp();
            return;
        }
        File config = new File(args.get("f"));
        if(!config.exists()){
            System.out.println("Config file not exists");
        }
        Map<String, String> conf=null;
        try {
            conf = Command.parseConfig(config);
        }catch (InvalidConfigFormatException e){
            Command.printConfigFormat();
            return;
        }
        String password,method;
        int port;
        try {
            password = conf.get("password");
            port = Integer.valueOf(conf.get("remotePort"));
            method = conf.get("method");
        }catch (Exception e){
            Command.printConfigFormat();
            return;
        }
        if(password==null||method==null){
            Command.printConfigFormat();
            return;
        }
        if(args.containsKey("s")){
            System.out.println("launching server");
            new EncryptedServer(port,method,password,1000,127).start();
        }
        else{
            String remoteHost;
            int localPort,remotePort;
            try{
                remoteHost = conf.get("host");
                if(remoteHost==null)    throw new Exception();
                localPort=Integer.valueOf(conf.get("localPort"));
                remotePort=Integer.valueOf(conf.get("remotePort"));
            } catch (Exception e){
                Command.printConfigFormat();
                return;
            }
            System.out.println("launching client");
            new EncryptedClient(localPort,remoteHost,remotePort,method,password,1000,127).start();
        }
    }
}
