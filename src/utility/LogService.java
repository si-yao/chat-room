package utility;
import java.io.*;
/**
 * This class is a simple tool for recording logs.
 * Created by szeyiu on 3/4/15.
 */
public class LogService {
    private static LogService logService = null;
    private BufferedWriter writer = null;
    private LogService() throws Exception{
        System.out.println("Starting log service...");
        //the log file name is chatroom.log
        File f = new File("chatroom.log");
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
    }
    public static LogService getInstance() throws Exception{
        if(logService==null){
            logService = new LogService();
        }
        return logService;
    }

    public void log(String s) throws Exception{
        writer.write(s+'\n');
        writer.flush();
    }

    public void close() throws Exception{
        writer.flush();
        writer.close();
        logService = null;
    }
}
