/**
 * Created by szeyiu on 3/4/15.
 */
import client.ReceiveService;
import client.SendService;
import utility.KVSerialize;
import utility.SocketService;

import java.util.HashMap;
import java.util.Map;

public class Client {
    public static void main(String[] args) throws Exception {
        String serverIP = args[0];
        int serverPort = Integer.valueOf(args[1]);
        if(args.length>2){
            SendService.listenPort = Integer.valueOf(args[2]);
        }
        SendService.setAddr(serverIP, serverPort);
        ReceiveService receiveService = ReceiveService.getInstance();
        SendService sendService  = SendService.getInstance();
        receiveService.start();
        sendService.start();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                try {
                    System.out.println("System: exiting...");
                    Map<String, String> dic = new HashMap<String, String>();
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
