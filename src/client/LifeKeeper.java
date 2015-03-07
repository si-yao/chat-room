package client;
import utility.KVSerialize;
import utility.SocketService;

import java.net.ConnectException;
import java.util.*;
/**
 * Created by szeyiu on 3/5/15.
 */
public class LifeKeeper implements Runnable{
    public void run(){
        SocketService socketService = null;
        try {
            socketService = SocketService.getInstance(SendService.listenPort);
        } catch (Exception e){
            e.printStackTrace();
        }
        Map<String, String> dic = new HashMap<String, String>();
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
                Thread.sleep(30 * 1000);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
