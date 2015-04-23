package client;
import utility.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the class listening all keyboard input.
 * And save all information about current user.
 * Created by szeyiu on 3/4/15.
 */
public class SendService implements Runnable {
    private Thread t;
    private String threadname = "[Client][SendService]";
    private static SendService sendService = null;
    private LogService logService;
    private SocketService socketService;
    public static String serverAddr = "localhost";// configurable
    public static int serverPort = 6789;// configurable
    public static int listenPort = 5678;
    public static int BLOCK_TIME = 60;// configurable
    public static boolean isLogin = false;
    public static Map<String, String> p2pIpMap;
    public static Map<String, Integer> p2pPortMap;
    private boolean isBlock;
    public static String username = "";
    public static String lastuser = "";
    public static boolean blockMainInput = false;
    public static String inputLine = null;

    private SendService() throws Exception{
        logService = LogService.getInstance();
        socketService = SocketService.getInstance(listenPort);
        logService.log(threadname + "Init Singlton");
        p2pIpMap = new ConcurrentHashMap<String, String>();
        p2pPortMap = new ConcurrentHashMap<String, Integer>();
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

    /**
     * This is the thread for blocking the login if user failed to login for 3 times.
     */
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

    /**
     * Main thread for client, listening keyboard input.
     */
    public void run(){
        Scanner scanner = new Scanner(System.in);
        while(true) {
            boolean success = false;
            int wrongtime = 0;
            isBlock = false;
            String user = "";
            while (!success) {
                if (wrongtime >= 3) {
                    isBlock = true;
                    System.out.println("Invalid Password. Your account has been blocked. Please try again after " + BLOCK_TIME + "s");
                    Thread blockThread = new Thread(new Block());
                    blockThread.start();
                    wrongtime = 0;
                }
                System.out.print("Username: ");
                user = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();
                if (isBlock) {
                    System.out.println("Due to multiple login failures, your account has been blocked. Please try again after sometime.");
                    continue;
                }
                Map<String, String> param = new ConcurrentHashMap<String, String>();
                param.put("type", "auth");
                param.put("username", user);
                param.put("password", password);
                param.put("port", "" + listenPort);
                try {
                    Map<String, String> res = KVSerialize.decode(socketService.request(serverAddr, serverPort, KVSerialize.encode(param)));
                    success = res.containsKey("result") ? res.get("result").equals("ok") : false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(!success) System.out.println("Login failed. Try again.");
                wrongtime++;
            }
            isLogin = true;
            System.out.println("Welcome to simple chat server!");
            username = user;
            lastuser = user;
            Thread lifeKeeper = new Thread(new LifeKeeper());
            lifeKeeper.start();
            while (isLogin) {
                String line = scanner.nextLine();
                inputLine = null;
                if(!isLogin) break;
                //If the input is blocked by other thread, then
                //the main thread will transfer the input to the target thread.
                if(blockMainInput){
                    inputLine = line;
                    blockMainInput = false;
                    continue;
                }
                try {
                    //for every keyboard command, start a new thread to handle it.
                    Thread thread = new Thread(new SendHandle(line));
                    thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
