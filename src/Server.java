/**
 * Created by szeyiu on 3/4/15.
 */
import server.*;

public class Server {
    public static void main(String argv[]) throws Exception {
        HubService.serverPort = Integer.valueOf(argv[0]);
        HubService hubService = HubService.getInstance();
        hubService.start();
    }
}
