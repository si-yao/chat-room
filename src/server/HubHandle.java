package server;
import utility.*;

import java.net.*;
import java.util.*;

/**
 * Created by szeyiu on 3/4/15.
 */
public class HubHandle implements Runnable{
    private Socket src;
    SocketService socketService;
    public HubHandle(Socket src){
        this.src = src;
    }

    public void run(){
        try {
            socketService = SocketService.getInstance(HubService.serverPort);
            String msg = socketService.readSokect(src);
            Map<String, String> dic = KVSerialize.decode(msg);
            if(!dic.containsKey("type")) {
                socketService.response(src, "invalid request!");
                return;
            }
            String type = dic.get("type");
            if(type.equals("message")){
                message(dic);
            }
            else if(type.equals("auth")){
                auth(dic);
            }
            else {
                error();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void auth(Map<String, String> dic) throws Exception {
        Map<String, String> kvmap = new HashMap<String, String>();
        String u = dic.containsKey("username")? dic.get("username"): "";
        String p = dic.containsKey("password")? dic.get("password"): "";
        int port = dic.containsKey("port")? Integer.valueOf(dic.get("port")):0;

        if(HubService.passwdMap.containsKey(u) && p.equals(HubService.passwdMap.get(u))){
            kvmap.put("result","ok");
            String IP = src.getInetAddress().getHostAddress();
            HubService.ipMap.put(u,IP);
            HubService.portMap.put(u,port);
        } else {
            kvmap.put("result","fail");
        }
        socketService.response(src,KVSerialize.encode(kvmap));
    }

    private void error() throws Exception{
        Map<String, String> map = new HashMap<String, String>();
        map.put("result", "unknown error");
        socketService.response(src, KVSerialize.encode(map));
    }
    private void message(Map<String, String> dic) throws Exception{
        String from = dic.containsKey("from")? dic.get("from"): "";
        String to = dic.containsKey("to")? dic.get("to"): "";
        String message = dic.containsKey("msg")? dic.get("msg"): "";
        String toIP;
        int toPort;
        Map<String, String> kvmap = new HashMap<String, String>();
        if(!HubService.passwdMap.containsKey(to)){
            kvmap.put("result", "user does not exist!");
            socketService.response(src,KVSerialize.encode(kvmap));
            return;
        }


        kvmap.put("type","message");
        kvmap.put("from",from);
        kvmap.put("msg",message);
        String reqStr = KVSerialize.encode(kvmap);

        if(HubService.ipMap.containsKey(to)) {
            toIP = HubService.ipMap.get(to);
            toPort = HubService.portMap.get(to);
            socketService.request(toIP, toPort, KVSerialize.encode(kvmap));
            kvmap = new HashMap<String, String>();
            kvmap.put("result","ok");
        } else {
            Map<String, List<String>> offMap = HubService.offlineReq;
            if(!offMap.containsKey(to)){
                offMap.put(to, new ArrayList<String>());
            }
            offMap.get(to).add(reqStr);
            kvmap = new HashMap<String, String>();
            kvmap.put("result",to + " is offline now. Msg will be delivered when online.");
        }
        socketService.response(src, KVSerialize.encode(kvmap));
    }

}
