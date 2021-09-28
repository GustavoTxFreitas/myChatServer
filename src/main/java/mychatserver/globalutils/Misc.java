package mychatserver.globalutils;

import java.util.Date;
import java.util.HashMap;

public class Misc {

    public static void customPrint(String message){
        System.out.println("["+new Date()+"] "+message);
    }

    public static int getQueryCode(HashMap<String, Object> message){
        return (int) message.get(KeyValues.KEY_QUERY);
    }

}
