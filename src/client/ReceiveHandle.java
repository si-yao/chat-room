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
            SocketService socketService = SocketService.getInstance();
            String msg = socketService.readSokect(src);
            Map<String, String> dic = KVSerialize.decode(msg);

            if("msg".equals(dic.get("type"))) {
                System.out.println("Receive a msg:" + msg);
                socketService.response(src, dic.get("msg").toUpperCase());
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
