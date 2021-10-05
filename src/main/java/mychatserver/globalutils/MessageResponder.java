package mychatserver.globalutils;

import static mychatserver.globalutils.KeyValues.*;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageResponder {


    public static HashMap<String, Object> respondToLoginRequestMessage(int responseCode, String responseMessage){
        HashMap<String, Object> msg = new HashMap<>();
        msg.put(KEY_QUERY, QUERY_LOGIN_REQUEST);
        msg.put(KEY_RESPONSE_CODE, responseCode);
        msg.put(KEY_RESPONSE_MESSAGE, responseMessage);
        return msg;
    }

    public static HashMap<String, Object> respondToSignupRequestMessage(int responseCode, String responseMessage){
        HashMap<String, Object> msg = new HashMap<>();
        msg.put(KEY_QUERY, QUERY_SIGNUP_REQUEST);
        msg.put(KEY_RESPONSE_CODE, responseCode);
        msg.put(KEY_RESPONSE_MESSAGE, responseMessage);
        return msg;
    }

    public static HashMap<String, Object> respondToHandshakeMessage(ArrayList<ArrayList<String>> existingMessages, HashMap<String, Object> userDetails){
        HashMap<String, Object> msg = new HashMap<>();
        msg.put(KEY_QUERY, QUERY_HANDSHAKE);
        msg.put(KEY_EXISTING_MESSAGES, existingMessages);
        msg.put(KEY_USER_DETAILS, userDetails);
        return msg;
    }

    public static HashMap<String, Object> respondToClientListRequestMessage(ArrayList<HashMap<String, Object>> activeUsersList, ArrayList<HashMap<String, Object>> inactiveUsersList){
        HashMap<String, Object> msg = new HashMap<>();
        msg.put(KEY_QUERY, QUERY_CLIENT_LIST);
        msg.put(KEY_ACTIVE_USERS_LIST, activeUsersList);
        msg.put(KEY_INACTIVE_USERS_LIST, inactiveUsersList);
        return msg;
    }

    public static HashMap<String, Object> respondToDeleteAccountRequestMessage(int responseCode, String responseMessage){
        HashMap<String, Object> msg = new HashMap<>();
        msg.put(KEY_QUERY, QUERY_DELETE_ACCOUNT);
        msg.put(KEY_RESPONSE_CODE, responseCode);
        msg.put(KEY_RESPONSE_MESSAGE, responseMessage);
        return msg;
    }

    public static HashMap<String, Object> respondToEditAccountRequestMessage(int responseCode, String responseMessage){
        HashMap<String, Object> msg = new HashMap<>();
        msg.put(KEY_QUERY, QUERY_EDIT_ACCOUNT);
        msg.put(KEY_RESPONSE_CODE, responseCode);
        msg.put(KEY_RESPONSE_MESSAGE, responseMessage);
        return msg;
    }

}