package mychatserver.chatnetwork.utils.interfaces;

import mychatserver.chatnetwork.main.ChatServerThread;

import java.util.HashMap;

public interface ChatServerConnectionListener {

    public void clientDisconnected(ChatServerThread chatServerThread, String username);

    public void clientMessageRecieved(ChatServerThread chatServerThread, HashMap<String, Object> message);

    public void clientListRequestRecieved(ChatServerThread chatServerThread);

    public void clientConnected(ChatServerThread chatServerThread, String username);

}