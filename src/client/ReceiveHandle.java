package client;
import utility.KVSerialize;
import utility.SocketService;

import java.net.*;
import java.util.*;
/**
 * Created by szeyiu on 3/4/15.
 */
public class ReceiveHandle implements Runnable{
    private Socket src;
    public ReceiveHandle(Socket src){
        this.src = src;
    }
    public void run(){
        try {
            SocketService socketService = SocketService.getInstance(SendService.listenPort);
            String msg = socketService.readSokect(src);
            Map<String, String> dic = KVSerialize.decode(msg);

            if("message".equals(dic.get("type"))) {
                String from = dic.containsKey("from")? dic.get("from"):"";
                String info = dic.containsKey("msg")? dic.get("msg"):"";
                System.out.println( from +": "+ info);
                Map<String, String> resp = new HashMap<String, String>();
                resp.put("result","ok");
                socketService.response(src, KVSerialize.encode(resp));
            }
            else if("offline".equals(dic.get("type"))){
                String from = dic.containsKey("from")? dic.get("from"):"";
                System.out.println("System: p2p user: "+from+ " is offline now.");
                if(SendService.p2pPortMap.containsKey(from)){
                    SendService.p2pPortMap.remove(from);
                }
                if(SendService.p2pIpMap.containsKey(from)){
                    SendService.p2pPortMap.remove(from);
                }
                Map<String, String> resp = new HashMap<String, String>();
                resp.put("result","ok");
                socketService.response(src, KVSerialize.encode(resp));
            }
            else if("kill".equals(dic.get("type"))){
                String reason = dic.containsKey("reason")? dic.get("reason"):"unknown reason";
                System.out.println("System: you are kicked out. Reason: " + reason);
                SendService.isLogin = false;
                Map<String, String> resp = new HashMap<String, String>();
                resp.put("result","ok");
                socketService.response(src, KVSerialize.encode(resp));
            }
            else if("ip".equals(dic.get("type"))){
                String from = dic.containsKey("from")? dic.get("from"):"";
                SendService.blockMainInput = true;
                System.out.print("System: the user: " + from + " is requesting your address. Consent(Y/N)? Y");
                while(SendService.inputLine == null){
                    Thread.sleep(10);
                }
                Map<String, String> resp = new HashMap<String, String>();
                resp.put("result","ok");
                if(SendService.inputLine.toLowerCase().equals("n")) {
                    System.out.println("Rejected");
                    resp.put("result","rej");
                } else{
                    System.out.println("Approved");
                }
                SendService.inputLine = null;
                socketService.response(src, KVSerialize.encode(resp));
            }
            else {
                Map<String, String> resp = new HashMap<String, String>();
                resp.put("result","unsupported type "+ dic.get("type"));
                socketService.response(src, KVSerialize.encode(resp));
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
