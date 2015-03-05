package utility;

import sun.rmi.runtime.Log;

import java.net.*;
import java.io.*;

/**
 * Created by szeyiu on 3/4/15.
 */
public class SocketService{
    private static SocketService socketService = null;
    private LogService logService = null;
    private String className =  "[SocketServe]";
    private ServerSocket welcomeSocket;
    private SocketService(int port) throws Exception{
        logService = LogService.getInstance();
        logService.log(className + " Init Singleton");
        welcomeSocket = new ServerSocket(port);//listening port 6789
    }

    public static SocketService getInstance(int port) throws Exception{
        if(socketService==null){
            socketService = new SocketService(port);
        }
        return socketService;
    }


    /**
     * send msg to addr at port, and return the response msg
     * @param addr
     * @param port
     * @param msg
     * @return
     * @throws Exception
     */
    public String request(String addr, int port, String msg) throws Exception{
        Socket clientSocket = new Socket(addr, port);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outToServer.writeBytes(msg+"\n");
        logService.log(className + "[addr: " + addr + "][port: " + port + "][msg: " + msg + "]");
        StringBuilder sb = new StringBuilder();
        sb.append(inFromServer.readLine());
        clientSocket.close();
        return sb.toString();
    }

    /**
     * Server codes
     * @return
     * @throws Exception
     */
    public Socket listen() throws Exception{
        Socket s = welcomeSocket.accept();
        return s;
    }

    public String readSokect(Socket source) throws Exception{
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(source.getInputStream()));
        StringBuilder sb = new StringBuilder();
        sb.append(inFromClient.readLine());
        return sb.toString();
    }

    public void response(Socket source, String msg) throws Exception{
        DataOutputStream outToClient = new DataOutputStream(source.getOutputStream());
        outToClient.writeBytes(msg+"\n");
    }


}
