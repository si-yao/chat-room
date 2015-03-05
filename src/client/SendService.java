package client;
import utility.*;

import java.util.*;

/**
 * Created by szeyiu on 3/4/15.
 */
public class SendService implements Runnable {
    private Thread t;
    private String threadname = "[Client][SendService]";
    private static SendService sendService = null;
    private LogService logService;
    private SocketService socketService;
    public static String serverAddr = "localhost";// configurable
    public static int serverPort = 5678;// configurable
    public static int BLOCK_TIME = 60;// configurable
    private boolean isBlock;


    private SendService() throws Exception{
        logService = LogService.getInstance();
        socketService = SocketService.getInstance();
        logService.log(threadname + "Init Singlton");
        t = null;
    }

    public static void setAddr(String addr, int port){
        serverAddr = addr;
        serverPort = port;
    }

    public static SendService getInstance() throws Exception{
        if(sendService == null){
            sendService = new SendService();
        }
        return sendService;
    }

    private class Block implements Runnable{
        public void run(){
            try {
                Thread.sleep(BLOCK_TIME * 1000);
            } catch (Exception e){
                e.printStackTrace();
            }
            isBlock = false;
        }
    }

    public void run(){
        Scanner scanner = new Scanner(System.in);
        boolean success = false;
        int wrongtime = 0;
        isBlock = false;
        while(!success) {
            if(wrongtime>=3){
                isBlock = true;
                System.out.println("Invalid Password. Your account has been blocked. Please try again after "+ BLOCK_TIME +"s");
                Thread blockThread = new Thread(new Block());
                blockThread.start();
                wrongtime = 0;
            }
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            if(isBlock){
                System.out.println("Due to multiple login failures, your account has been blocked. Please try again after sometime.");
                continue;
            }
            Map<String, String> param = new HashMap<String, String>();
            param.put("type", "auth");
            param.put("username", username);
            param.put("password", password);
            try {
                Map<String, String> res = KVSerialize.decode(socketService.request(serverAddr, serverPort, KVSerialize.encode(param)));
                success = res.get("msg").equals("ok");
            } catch (Exception e) {
                e.printStackTrace();
            }
            wrongtime ++;
        }
        System.out.println("Welcome to simple chat server!");

        while(true){
            String line = scanner.nextLine();
            try {
                Thread thread = new Thread(new SendHandle(line));
                thread.start();
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
