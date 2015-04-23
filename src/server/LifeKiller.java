package server;

import utility.KVSerialize;
import utility.LogService;
import utility.SocketService;

import java.util.*;
import java.util.concurrent.*;
import java.net.*;

/**
 * This class handles heartbeats from clients.
 * If it does not receive heartbeat from a client for a certain time,
 * then it will kill the client.
 * Created by szeyiu on 3/5/15.
 */
public class LifeKiller implements Runnable{
    SocketService socketService;
    LogService logService;
    String className = "[Server][LifeKiller]";
    int KILL_TIME = 60; //kill the client if does not receive a heartbeat for 60s
    public LifeKiller(){
        try {
            socketService = SocketService.getInstance(HubService.serverPort);
            logService = LogService.getInstance();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run(){
        while(true) {
            try {
                Thread.sleep(KILL_TIME * 1000);
                for (String u : new ArrayList<String>(HubService.aliveMap.keySet())) {
                    if (HubService.aliveMap.containsKey(u) && !HubService.aliveMap.get(u)) {
                        logService.log(className+"kill user: "+u+", cause: time out");
                        System.out.println(className+"kill user: "+u+", cause: time out");
                        Thread logoutThread = new Thread(new Logout(u));
                        logoutThread.start();
                    } else {
                        if (HubService.aliveMap.containsKey(u)) {
                            HubService.aliveMap.put(u, false);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * force the client to logout to kill it.
     */
    private class Logout implements Runnable{
        String from;
        public Logout(String from){
            this.from = from;
        }
        public void run(){
            Map<String, String> logoutDic = new ConcurrentHashMap<String, String>();
            logoutDic.put("type","logout");
            logoutDic.put("from",from);
            String res = "";
            try {
                res = socketService.request("localhost", HubService.serverPort, KVSerialize.encode(logoutDic));
                Map<String, String> resDic = KVSerialize.decode(res);
                while(!resDic.containsKey("result") || !resDic.get("result").equals("ok")){
                    res = socketService.request("localhost", HubService.serverPort, KVSerialize.encode(logoutDic));
                    resDic = KVSerialize.decode(res);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
