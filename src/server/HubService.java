package server;

import utility.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;
/**
 * This is hte main thread for incoming requests.
 * Created by szeyiu on 3/4/15.
 */
public class HubService implements Runnable{
    private Thread t;
    private String threadname = "[Server][HubService]";
    private static HubService hubService = null;
    private LogService logService;
    private SocketService socketService;
    //following *Map members are info of users.
    public static Map<String, String> passwdMap;
    public static Map<String, String> ipMap;
    public static Map<String, Integer> portMap;
    public static Map<String, List<String>> blockMap;
    public static Map<String, List<String>> offlineReq;
    public static Map<String, Boolean> aliveMap;
    public static Map<String, Set<String>> p2pPairs;
    public static int serverPort;
    private String passwdFile = "credentials.txt";

    private HubService() throws Exception{
        System.out.println(threadname+" singleton init..");
        passwdMap = new ConcurrentHashMap<String, String>();
        ipMap = new ConcurrentHashMap<String, String>();
        portMap = new ConcurrentHashMap<String, Integer>();
        blockMap = new ConcurrentHashMap<String, List<String>>();
        offlineReq = new ConcurrentHashMap<String, List<String>>();
        aliveMap = new ConcurrentHashMap<String, Boolean>();
        p2pPairs = new ConcurrentHashMap<String, Set<String>>();
        File f = new File(passwdFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        String line = reader.readLine();
        while(line!=null){
            String[] splt = line.split(" ",2);
            passwdMap.put(splt[0],splt[1]);
            line = reader.readLine();
        }
        reader.close();
        logService = LogService.getInstance();
        socketService = SocketService.getInstance(serverPort);
    }

    public static HubService getInstance() throws Exception{
        if(hubService==null){
            hubService = new HubService();
        }
        return hubService;
    }

    public void run(){
        Thread lifeKiller = new Thread(new LifeKiller());
        //start life killer
        lifeKiller.start();
        while(true){
            try {
                Socket src = socketService.listen();
                //for every incoming socket, start a new thread to handle
                Thread thread = new Thread(new HubHandle(src));
                thread.start();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void start(){
        System.out.println(threadname+" Starting");
        if(t == null){
            t = new Thread(this, threadname);
            t.start();
        }
    }
}
