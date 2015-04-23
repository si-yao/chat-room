package server;

import utility.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
/**
 * This is the thread to handle the incoming request.
 * Created by szeyiu on 3/4/15.
 */
public class HubHandle implements Runnable{
    private Socket src;
    SocketService socketService;
    LogService logService;
    String className = "[Server][HubHanle]";
    public HubHandle(Socket src) throws Exception{
        this.src = src;
        logService = LogService.getInstance();
        socketService = SocketService.getInstance(HubService.serverPort);
    }

    /**
     * Read the message from input socket, and do the routines for the request.
     */
    public void run(){
        try {
            String msg = socketService.readSokect(src);
            logService.log(className+"Receive: "+msg);
            System.out.println(className+"Receive: "+msg);

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
            else if(type.equals("online")){
                online();
            }
            else if(type.equals("broadcast")){
                broadcast(dic);
            }
            else if(type.equals("block")){
                block(dic);
            }
            else if(type.equals("unblock")){
                unblock(dic);
            }
            else if(type.equals("logout")){
                logout(dic);
            }
            else if(type.equals("address")){
                address(dic);
            }
            else if(type.equals("alive")){
                alive(dic);
            }
            else {
                error();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Handle the heartbeat from the client. Update the alive map.
     * @param dic
     * @throws Exception
     */
    private void alive(Map<String, String> dic) throws Exception {
        String from = dic.containsKey("from")? dic.get("from"):"";
        HubService.aliveMap.put(from, true);
        Map<String, String> resDic = new ConcurrentHashMap<String, String>();
        resDic.put("result","ok");
        socketService.response(src,KVSerialize.encode(resDic));
    }

    /**
     * get address. first look up if the target user is online.
     * And then ask the target user if he wants to give his address.
     * Then return the rejection to user or give the IP/PORT to user.
     * @param dic
     * @throws Exception
     */
    private void address(Map<String, String> dic) throws Exception {
        String from = dic.containsKey("from")? dic.get("from"):"";
        String target = dic.containsKey("target")? dic.get("target"):"";
        Map<String, String> resDic = new ConcurrentHashMap<String, String>();
        resDic.put("ip","null");
        resDic.put("port","null");
        if(!HubService.passwdMap.containsKey(target)){
            resDic.put("result","user does not exist");
            socketService.response(src, KVSerialize.encode(resDic));
            return;
        }
        if(blockBbyA(target, from)){
            resDic.put("result","you are blocked!");
            socketService.response(src, KVSerialize.encode(resDic));
            return;
        }
        if(!isOnline(target)){
            resDic.put("result", "the user is offline");
            socketService.response(src, KVSerialize.encode(resDic));
            return;
        }
        resDic.put("result","ok");
        String ip = HubService.ipMap.containsKey(target)? HubService.ipMap.get(target):"";
        int port = HubService.portMap.containsKey(target)? HubService.portMap.get(target):-1;


        Map<String, String> askMap = new ConcurrentHashMap<String, String>();
        askMap.put("type","ip");
        askMap.put("from",from);
        String askRes="";
        try {
            askRes = socketService.request(ip, port, KVSerialize.encode(askMap));
            if(!KVSerialize.decode(askRes).containsKey("result") || !KVSerialize.decode(askRes).get("result").equals("ok")){
                resDic.put("result","the user denied your request.");
            }
        } catch (Exception e){
            resDic.put("result","the user is offline");
        }
        if(!resDic.get("result").equals("ok")){
            socketService.response(src, KVSerialize.encode(resDic));
            return;
        }

        if(!HubService.p2pPairs.containsKey(from))
            HubService.p2pPairs.put(from, new HashSet<String>());
        if(!HubService.p2pPairs.containsKey(target))
            HubService.p2pPairs.put(target, new HashSet<String>());
        HubService.p2pPairs.get(from).add(target);
        HubService.p2pPairs.get(target).add(from);
        resDic.put("ip",ip);
        resDic.put("port",""+port);

        System.out.println(askRes);
        socketService.response(src, KVSerialize.encode(resDic));
    }

    /**
     * If a user logged out, then remove the information of this user and notify other users.
     * @param dic
     * @throws Exception
     */
    private void logout(Map<String, String> dic) throws Exception {
        String from = dic.containsKey("from")? dic.get("from"):"";
        if(HubService.ipMap.containsKey(from)){
            HubService.ipMap.remove(from);
        }
        if(HubService.portMap.containsKey(from)){
            HubService.portMap.remove(from);
        }
        if(HubService.aliveMap.containsKey(from)){
            HubService.aliveMap.remove(from);
        }
        if(HubService.p2pPairs.containsKey(from)){
            for(String u: new ArrayList<String>(HubService.p2pPairs.get(from))){
                if(HubService.p2pPairs.containsKey(u)){
                    HubService.p2pPairs.get(u).remove(from);
                }
                Thread offlineThread = new Thread(new OfflineMsg(from, u));
                offlineThread.start();
            }
            HubService.p2pPairs.remove(from);
        }
        Map<String, String> resDic = new ConcurrentHashMap<String, String>();
        resDic.put("result","ok");
        socketService.response(src, KVSerialize.encode(resDic));
    }

    /**
     * When a user logged out, then the server sends notification to every user
     * who has a p2p connection with the user.
     */
    private class OfflineMsg implements Runnable{
        String from;
        String u;
        public OfflineMsg(String from, String u){
            this.from = from;
            this.u = u;
        }
        public void run(){
            if(!isOnline(u)) return;
            try {
                Map<String, String> offDic = new ConcurrentHashMap<String, String>();
                offDic.put("type", "offline");
                offDic.put("from", from);
                String ip = HubService.ipMap.get(u);
                int port = HubService.portMap.get(u);
                socketService.request(ip, port, KVSerialize.encode(offDic));
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * unblock a user
     * @param dic
     * @throws Exception
     */
    private void unblock(Map<String, String> dic) throws Exception {
        String from = dic.containsKey("from")? dic.get("from"):"";
        String target = dic.containsKey("target")? dic.get("target"):"";
        Map<String, String> resDic = new ConcurrentHashMap<String, String>();
        if(!HubService.passwdMap.containsKey(target)){
            resDic.put("result","user does not exist");
            socketService.response(src, KVSerialize.encode(resDic));
        }
        if(!blockBbyA(from, target)){
            resDic.put("result","ok");
            socketService.response(src, KVSerialize.encode(resDic));
            return;
        }
        if(HubService.blockMap.containsKey(from)) {
            List<String> blst = HubService.blockMap.get(from);
            for(int i=0; i<blst.size(); ++i){
                if(blst.get(i).equals(target)){
                    blst.remove(i);
                    break;
                }
            }
        }
        resDic.put("result","ok");
        socketService.response(src, KVSerialize.encode(resDic));
    }

    /**
     * block a user
     * @param dic
     * @throws Exception
     */
    private void block(Map<String, String> dic) throws Exception {
        String from = dic.containsKey("from")? dic.get("from"):"";
        String target = dic.containsKey("target")? dic.get("target"):"";
        Map<String, String> resDic = new ConcurrentHashMap<String, String>();
        if(!HubService.passwdMap.containsKey(target)){
            resDic.put("result","user does not exist");
            socketService.response(src, KVSerialize.encode(resDic));
        }
        if(blockBbyA(from, target)){
            resDic.put("result","ok");
            socketService.response(src, KVSerialize.encode(resDic));
            return;
        }
        if(!HubService.blockMap.containsKey(from)){
            HubService.blockMap.put(from, new ArrayList<String>());
        }
        List<String> blst = HubService.blockMap.get(from);
        blst.add(target);
        resDic.put("result","ok");
        socketService.response(src, KVSerialize.encode(resDic));
    }

    /**
     * broadcast the message, except the users who block the sender.
     * @param dic
     * @throws Exception
     */
    private void broadcast(Map<String, String> dic) throws Exception {
        String from = dic.containsKey("from")? dic.get("from"):"";
        String message = dic.containsKey("msg")? dic.get("msg"):"";
        Map<String, String> reqDic = new ConcurrentHashMap<String, String>();
        reqDic.put("type","message");
        reqDic.put("from",from);
        reqDic.put("msg",message);
        for(String u: new ArrayList<String>(HubService.passwdMap.keySet())){
            if(u.equals(from)) continue;
            if(isOnline(u)){
                if(blockBbyA(u, from)) continue;
                reqDic.put("to", u);
                Thread sendThread = new Thread(new GroupMsg(KVSerialize.encode(reqDic),u));
                sendThread.start();
            }
        }
        Map<String, String> resDic = new ConcurrentHashMap<String, String>();
        resDic.put("result", "ok");
        socketService.response(src, KVSerialize.encode(resDic));
    }

    /**
     * This is the thread to sent the broadcast message.
     * Using a runnable class could make it concurrent.
     */
    private class GroupMsg implements Runnable{
        String req;
        String to;
        GroupMsg(String req, String to){
            this.req = req;
            this.to = to;
        }
        public void run(){
            String IP = HubService.ipMap.containsKey(to)? HubService.ipMap.get(to):"";
            int port = HubService.portMap.containsKey(to)? HubService.portMap.get(to): -1;
            if(IP.equals("")||port==-1) return;
            try {
                socketService.request(IP, port, req);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * judge a user is online or not.
     * @param u
     * @return
     */
    private boolean isOnline(String u){
        return HubService.ipMap.containsKey(u) && !HubService.ipMap.get(u).equals("");
    }

    /**
     * judge if user A blocks user B
     * @param a
     * @param b
     * @return
     */
    private boolean blockBbyA(String a, String b){
        if(HubService.blockMap.containsKey(a)){
            List<String> blst = HubService.blockMap.get(a);
            for(String u: blst){
                if(u.equals(b)) return true;
            }
        }
        return false;
    }


    /**
     * get the list of online users.
     * @throws Exception
     */
    private void online() throws Exception {
        Map<String, String> kvmap = new ConcurrentHashMap<String, String>();
        for(String u: new ArrayList<String>(HubService.ipMap.keySet())){
            kvmap.put(u,u);
        }
        socketService.response(src, KVSerialize.encode(kvmap));
    }

    /**
     * authentication for user login.
     * If the user successfully logs in, then send offline messages to him.
     * @param dic
     * @throws Exception
     */
    private void auth(Map<String, String> dic) throws Exception {
        Map<String, String> kvmap = new ConcurrentHashMap<String, String>();
        String u = dic.containsKey("username")? dic.get("username"): "";
        String p = dic.containsKey("password")? dic.get("password"): "";
        int port = dic.containsKey("port")? Integer.valueOf(dic.get("port")):0;

        if(HubService.passwdMap.containsKey(u) && p.equals(HubService.passwdMap.get(u))){
            if(isOnline(u)){
                String oldIP = HubService.ipMap.get(u);
                int oldPort = HubService.portMap.get(u);
                Map<String, String> killMap = new ConcurrentHashMap<String, String>();
                killMap.put("type","kill");
                killMap.put("reason","you have logged in at another place.");
                try {
                    socketService.request(oldIP, oldPort, KVSerialize.encode(killMap));
                } catch (Exception e){
                    System.out.println("the user seems has been offline already");
                }
            }
            kvmap.put("result","ok");
            socketService.response(src,KVSerialize.encode(kvmap));
            String IP = src.getInetAddress().getHostAddress();
            HubService.ipMap.put(u,IP);
            HubService.portMap.put(u,port);
            HubService.aliveMap.put(u, true);
            if(HubService.offlineReq.containsKey(u)){
                List<String> reqlist = HubService.offlineReq.get(u);
                for(String req: reqlist){
                    socketService.request(IP, port, req);
                }
                reqlist.clear();
            }
        } else {
            kvmap.put("result","fail");
            socketService.response(src,KVSerialize.encode(kvmap));
        }
    }

    /**
     * a simple response with error message if the request is unknown
     * @throws Exception
     */
    private void error() throws Exception{
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        map.put("result", "unknown error");
        socketService.response(src, KVSerialize.encode(map));
    }

    /**
     * send the message from userA to userB
     * Same it to offline message queue, if the target user is offline
     * @param dic
     * @throws Exception
     */
    private void message(Map<String, String> dic) throws Exception{
        String from = dic.containsKey("from")? dic.get("from"): "";
        String to = dic.containsKey("to")? dic.get("to"): "";
        String message = dic.containsKey("msg")? dic.get("msg"): "";
        String toIP;
        int toPort;
        Map<String, String> kvmap = new ConcurrentHashMap<String, String>();
        if(!HubService.passwdMap.containsKey(to)){
            kvmap.put("result", "user does not exist!");
            socketService.response(src,KVSerialize.encode(kvmap));
            return;
        }
        if(HubService.blockMap.containsKey(to)){
            List<String> blocklist = HubService.blockMap.get(to);
            for(String u: blocklist){
                if(u.equals(from)){
                    kvmap.put("result", "you are blocked!");
                    socketService.response(src,KVSerialize.encode(kvmap));
                    return;
                }
            }
        }

        kvmap.put("type","message");
        kvmap.put("from",from);
        kvmap.put("msg",message);
        String reqStr = KVSerialize.encode(kvmap);

        if(HubService.ipMap.containsKey(to)) {
            toIP = HubService.ipMap.get(to);
            toPort = HubService.portMap.get(to);
            try {
                socketService.request(toIP, toPort, KVSerialize.encode(kvmap));
            } catch(ConnectException e){
                kvmap = new ConcurrentHashMap<String, String>();
                kvmap.put("result", to + " is offline now. Msg will be delivered when online.");
                Map<String, List<String>> offMap = HubService.offlineReq;
                if(!offMap.containsKey(to)){
                    offMap.put(to, new ArrayList<String>());
                }
                offMap.get(to).add(reqStr);
                socketService.response(src, KVSerialize.encode(kvmap));
                return;
            }
            kvmap = new ConcurrentHashMap<String, String>();
            kvmap.put("result","ok");
        } else {
            Map<String, List<String>> offMap = HubService.offlineReq;
            if(!offMap.containsKey(to)){
                offMap.put(to, new ArrayList<String>());
            }
            offMap.get(to).add(reqStr);
            kvmap = new ConcurrentHashMap<String, String>();
            kvmap.put("result",to + " is offline now. Msg will be delivered when online.");
        }
        socketService.response(src, KVSerialize.encode(kvmap));
    }

}
