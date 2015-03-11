package utility;
import java.util.*;
/**
 * This is just a test class.
 * Created by szeyiu on 3/4/15.
 */
public class KVSerializeTest {
    public static void main(String[] args){
        Map<String, String> map = new HashMap<String, String>();
        map.put("name","123abc");
        map.put("password","Lisiyao\\: 456\\\\::");// which is "Lisiyao\: 456\\::"
        map.put("message","hello, I am sail. I am living in NY. And my phone number is: 123456\\888");
        KVSerialize.printMap(map);
        KVSerialize.printMap(KVSerialize.decode(KVSerialize.encode(map)));
    }
}
