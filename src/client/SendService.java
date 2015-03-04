package client;
import utility.*;

import java.util.Scanner;

/**
 * Created by szeyiu on 3/4/15.
 */
public class SendService implements Runnable {
    private Thread t;
    private String threadname = "[Client][SendService]";
    private static SendService sendService = null;
    private LogService logService;
    private SocketService socketService;

    private SendService() throws Exception{
        logService = LogService.getInstance();
        socketService = SocketService.getSendInstance();
        logService.log(threadname + "Init Singlton");
        t = null;
    }

    public static SendService getInstance() throws Exception{
        if(sendService == null){
            sendService = new SendService();
        }
        return sendService;
    }

    public void run(){
        Scanner scanner = new Scanner(System.in);
        while(true){
            String line = scanner.nextLine();
            try {
                String res = socketService.request("localhost", 6789, line);
                System.out.println("From Server:" + res);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public void start(){
        System.out.println(threadname+" Starting");
        if(t == null){
            t = new Thread(this, threadname);
            t.start();
        }
    }

}
