package client;
import utility.*;
import java.net.*;
/**
 * Created by szeyiu on 3/4/15.
 */
public class ReceiveService implements Runnable{
    private Thread t;
    private String threadname = "[Client][ReceiveService]";
    private static ReceiveService receiveService = null;
    private LogService logService;
    private SocketService socketService;

    private ReceiveService() throws Exception{
        logService = LogService.getInstance();
        socketService = SocketService.getInstance();
        logService.log(threadname + "Init Singlton");
        t = null;
    }

    public static ReceiveService getInstance() throws Exception{
        if(receiveService == null){
            receiveService = new ReceiveService();
        }
        return receiveService;
    }

    public void run(){
        while(true){
            try {
                Socket src = socketService.listen();
                Thread thread = new Thread(new ReceiveHandle(src));
                thread.start();
            } catch(Exception e){
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
