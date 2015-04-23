/**
 * Created by szeyiu on 3/4/15.
 */
import server.*;
import utility.KVSerialize;
import utility.SocketService;

import java.util.*;
import java.util.concurrent.*;

/**
 * The entry class for server
 */
public class Server {
    public static void main(String argv[]) throws Exception {
        // the first arg is the server port
        HubService.serverPort = Integer.valueOf(argv[0]);
        // start the service of Server
        HubService hubService = HubService.getInstance();
        hubService.start();
        // handle the control c interrupt
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                Map<String, String> killMap = new ConcurrentHashMap<String, String>();
                killMap.put("type","kill");
                killMap.put("reason","Server is down now...");
                try {
                    SocketService socketService = SocketService.getInstance(HubService.serverPort);
                    for(String u: new ArrayList<String>(HubService.ipMap.keySet())) {
                        String IP = HubService.ipMap.containsKey(u)? HubService.ipMap.get(u): "";
                        int port = HubService.portMap.containsKey(u)? HubService.portMap.get(u): -1;
                        if(!IP.equals("") && port >= 0) {
                            socketService.request(IP, port, KVSerialize.encode(killMap));
                        }
                    }
                } catch (Exception e){
                    System.out.println("Try to kill the user who is not online");
                }
            }
        });
    }
}
