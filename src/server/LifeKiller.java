package server;

import utility.KVSerialize;
import utility.LogService;
import utility.SocketService;

import java.util.*;
import java.net.*;

/**
 * Created by szeyiu on 3/5/15.
 */
public class LifeKiller implements Runnable{
    SocketService socketService;
    LogService logService;
    String className = "[Server][LifeKiller]";
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
                Thread.sleep(60 * 1000);
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

    private class Logout implements Runnable{
        String from;
        public Logout(String from){
            this.from = from;
        }
        public void run(){
            Map<String, String> logoutDic = new HashMap<String, String>();
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
