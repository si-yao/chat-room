/**
 * Created by szeyiu on 3/4/15.
 */
import client.ReceiveService;
import client.SendService;

public class Client {
    public static void main(String[] args) throws Exception {
        ReceiveService receiveService = ReceiveService.getInstance();
        SendService sendService  = SendService.getInstance();
        receiveService.start();
        sendService.start();
    }
}
