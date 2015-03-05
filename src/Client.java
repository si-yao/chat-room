/**
 * Created by szeyiu on 3/4/15.
 */
import client.ReceiveService;
import client.SendService;

public class Client {
    public static void main(String[] args) throws Exception {
        String serverIP = args[0];
        int serverPort = Integer.valueOf(args[1]);
        if(args.length>2){
            SendService.listenPort = Integer.valueOf(args[2]);
        }
        SendService.setAddr(serverIP, serverPort);
        ReceiveService receiveService = ReceiveService.getInstance();
        SendService sendService  = SendService.getInstance();
        receiveService.start();
        sendService.start();
    }
}
