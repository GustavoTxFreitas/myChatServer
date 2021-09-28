package mychatserver.chatnetwork.main;

import mychatserver.chatnetwork.utils.interfaces.ChatServerConnectionListener;
import static mychatserver.globalutils.KeyValues.*;
import static mychatserver.globalutils.Misc.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.HashMap;

import javax.net.ssl.SSLSocket;

public class ChatServerThread{

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    public String username;
    private ChatServerConnectionListener chatServerConnectionListener;
    private final SSLSocket socket;

    public ChatServerThread(SSLSocket socket, ChatServer chatServer){
        this.socket = socket;
        try{
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.chatServerConnectionListener = chatServer;
            this.username = null;
        }catch(IOException e){
            System.out.println(e);
        }
    }

    private void resolveMessage(HashMap<String, Object> message) throws IOException {
        int queryCode = getQueryCode(message);
        String sender = message.get(KEY_USERNAME).toString();
        switch (queryCode){
            case QUERY_HANDSHAKE -> {
                username = message.get(KEY_USERNAME).toString();
                chatServerConnectionListener.clientConnected(this, username);
            }
            case QUERY_SEND_MESSAGE -> chatServerConnectionListener.clientMessageRecieved(this, message);
            case QUERY_CLIENT_LIST -> chatServerConnectionListener.clientListRequestRecieved(this);
        }
    }

    private void receiveMessage(){
        try{
            while(true){
                if (objectInputStream.available() >= 0){
                    HashMap<String, Object> message = (HashMap<String, Object>)objectInputStream.readObject();
                    customPrint("Message recieved from "+username);
                    resolveMessage(message);
                }
            }
        }catch (Exception e){
            chatServerConnectionListener.clientDisconnected(this, username);
            System.out.println(username+" disconnected");
        }
    }

    public void sendMessage(HashMap<String, Object> message){
        try{
            objectOutputStream.writeObject(message);
        }catch(IOException e){
            chatServerConnectionListener.clientDisconnected(this, username);
        }
    }

    public void init() {
        receiveMessage();
    }

    public InetAddress getIP(){
        return socket.getInetAddress();
    }

}