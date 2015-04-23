package client;
import utility.KVSerialize;
import utility.SocketService;

import java.util.*;
import java.util.concurrent.*;
/**
 * This is the thread for heartbeats.
 * Send heartbeats for every 30s.
 * Created by szeyiu on 3/5/15.
 */
public class LifeKeeper implements Runnable{
    int BEAT_TIME = 30;
    public void run(){
        SocketService socketService = null;
        try {
            socketService = SocketService.getInstance(SendService.listenPort);
        } catch (Exception e){
            e.printStackTrace();
        }
        Map<String, String> dic = new ConcurrentHashMap<String, String>();
        dic.put("type", "alive");
        dic.put("from", SendService.username);
        String req = KVSerialize.encode(dic);
        while(SendService.isLogin){
            int tryCount = 0;
            while(true) {
                try {
                    if (socketService != null) {
                        socketService.request(SendService.serverAddr, SendService.serverPort, req);
                        break;
                    }
                } catch (Exception e) {
                    tryCount++;
                    if(tryCount==3) {
                        System.out.println("System: server down. exit...");
                        System.exit(-1);
                    }
                    System.out.println("System: Reconnecting the server...");
                }
            }

            try {
                Thread.sleep(BEAT_TIME * 1000);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
