package mychatserver.chatnetwork.utils.interfaces;

import mychatserver.chatnetwork.main.ChatServerThread;

import java.util.HashMap;

public interface ChatServerConnectionListener {

    void clientDisconnected(ChatServerThread chatServerThread, String username);
    void clientMessageRecieved(ChatServerThread chatServerThread, HashMap<String, Object> message);
    void clientListRequestRecieved(ChatServerThread chatServerThread);
    void clientConnected(ChatServerThread chatServerThread, String username);
    void editAccountWithPasswordAndWithUsernameRequestReceived(ChatServerThread chatServerThread, HashMap<String, Object> message);
    void editAccountWithoutPasswordAndWithUsernameRequestReceived(ChatServerThread chatServerThread, HashMap<String, Object> message);
    void editAccountWithPasswordAndWithoutUsernameRequestReceived(ChatServerThread chatServerThread, HashMap<String, Object> message);
    void editAccountWithoutPasswordAndWithoutUsernameRequestReceived(ChatServerThread chatServerThread, HashMap<String, Object> message);
    void deleteAccountRequestReceived(ChatServerThread chatServerThread, HashMap<String, Object> message);

}