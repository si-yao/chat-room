package utility;
import java.util.*;
import java.util.concurrent.*;
/**
 * This is the protocol for socket.
 * It convert a Map to a serialized string for messaging via socket.
 * Created by szeyiu on 3/4/15.
 * This class serializes the key value pairs (Map) into string.
 */
public class KVSerialize {
    private static char delimiter = ':';
    public static String encode(Map<String, String> kvMap){
        StringBuilder sb = new StringBuilder();
        for(String key: new ArrayList<String>(kvMap.keySet())){
            encode(sb, key);
            sb.append(delimiter);
            encode(sb, kvMap.get(key));
            sb.append(delimiter);
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    /**
     * encode a string from a Map.
     */
    private static void encode(StringBuilder sb, String s){
        for(int i=0; i<s.length(); ++i){
            if(s.charAt(i)=='\\' || s.charAt(i)==delimiter){
                sb.append('\\');
            }
            sb.append(s.charAt(i));
        }
    }

    /**
     * decode the string to Map
     * @param s
     * @return
     */
    public static Map<String, String> decode(String s){
        Map<String, String> rstMap = new ConcurrentHashMap<String, String>();
        boolean isKey = true;
        StringBuilder buffer = new StringBuilder();
        String curKey = "";
        for(int i=0; i<s.length(); ++i){
            if(s.charAt(i)=='\\'){
                i++;
                buffer.append(s.charAt(i));
            } else if(s.charAt(i)==delimiter){
                if(isKey){
                    curKey = buffer.toString();
                } else {
                    rstMap.put(curKey, buffer.toString());
                }
                isKey = !isKey;
                buffer = new StringBuilder();
            } else {
                buffer.append(s.charAt(i));
            }
        }
        if(!isKey){
            rstMap.put(curKey, buffer.toString());
        }
        return rstMap;
    }

    /**
     * print the elements in the Map for debugging.
     * @param map
     */
    public static void printMap(Map<String, String> map){
        System.out.println("------------");
        for(String k: new ArrayList<String>(map.keySet())){
            System.out.println(k);
            System.out.println(map.get(k));
            System.out.println("------------");
        }
    }
}
