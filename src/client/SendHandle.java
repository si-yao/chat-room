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
            SocketService socketService = SocketService.getInstance(SendService.listenPort);
            Map<String, String> dic = new HashMap<String, String>();
            int spaceIdx = cmd.indexOf(" ");
            if(spaceIdx<0){
                System.out.println("unknown cmd");
                return;
            }
            if (cmd.substring(0, spaceIdx).equals("message")) {
                dic.put("type","message");
                int userIdx = cmd.indexOf(" ", spaceIdx+1);
                if(userIdx<0){
                    System.out.println("please type username");
                    return;
                }
                String toUser = cmd.substring(spaceIdx+1, userIdx);
                dic.put("msg",cmd.substring(userIdx + 1));
                dic.put("from", SendService.username);
                dic.put("to", toUser);
                String res = socketService.request(SendService.serverAddr, SendService.serverPort, KVSerialize.encode(dic));
                //System.out.println(res);
                Map<String, String> resDic = KVSerialize.decode(res);
                if(resDic.containsKey("result") && !resDic.get("result").equals("ok")){
                    System.out.println(resDic.get("result"));
                } else if(resDic.containsKey("result") && resDic.get("result").equals("ok")){
                    System.out.println("(delivered)");
                }
            }
            else {
                System.out.println("unknown cmd");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
