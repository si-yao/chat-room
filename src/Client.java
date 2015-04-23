/**
 * Created by szeyiu on 3/4/15.
 */
import client.ReceiveService;
import client.SendService;
import utility.KVSerialize;
import utility.SocketService;

import java.util.*;
import java.util.concurrent.*;

/**
 * The entry class for client
 */
public class Client {
    public static void main(String[] args) throws Exception {
        //the first arg is the server ip
        String serverIP = args[0];
        //second arg is server port
        int serverPort = Integer.valueOf(args[1]);
        if(args.length>2){
            //the third optional arg is client port
            //if not available, then client port is same with server port.
            SendService.listenPort = Integer.valueOf(args[2]);
        }
        SendService.setAddr(serverIP, serverPort);
        //start listening received message
        ReceiveService receiveService = ReceiveService.getInstance();
        //start listening keyboard input
        SendService sendService  = SendService.getInstance();
        receiveService.start();
        sendService.start();
        // handle control c interrupt
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                try {
                    System.out.println("System: exiting...");
                    Map<String, String> dic = new ConcurrentHashMap<String, String>();
                    dic.put("type", "logout");
                    dic.put("from", SendService.username);
                    SocketService socketService = SocketService.getInstance(SendService.listenPort);
                    String res = socketService.request(SendService.serverAddr, SendService.serverPort, KVSerialize.encode(dic));
                    dic = KVSerialize.decode(res);
                    if (!dic.containsKey("result")) {
                        System.out.println("Server Error. Try again.");
                    } else if (dic.get("result").equals("ok")) {
                        System.out.println("success.");
                        SendService.isLogin = false;
                    } else {
                        System.out.println(dic.get("result"));
                    }
                } catch (Exception e){
                }
            }
        });
    }
}
