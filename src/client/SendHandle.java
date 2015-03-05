package client;

import utility.KVSerialize;
import utility.SocketService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by szeyiu on 3/4/15.
 */
public class SendHandle implements Runnable{
    private String cmd;
    public SendHandle(String cmd){
        this.cmd = cmd;
    }
    public void run(){
        try {
            SocketService socketService = SocketService.getInstance();
            Map<String, String> dic = new HashMap<String, String>();
            int spaceIdx = cmd.indexOf(" ");
            if(spaceIdx<0){
                System.out.println("unknown cmd");
                return;
            }
            if (cmd.substring(0, spaceIdx).equals("message")) {
                dic.put("type","msg");
                dic.put("msg",cmd.substring(spaceIdx + 1));
                String res = socketService.request("localhost", 6789, KVSerialize.encode(dic));
                System.out.println("From Server:" + res);
            } else {
                System.out.println("unknown cmd");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
