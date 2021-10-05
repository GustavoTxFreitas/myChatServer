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
    private boolean disconnectRequested = false;

    public ChatServerThread(SSLSocket socket, ChatServer chatServer){
        customPrint("Thread created");
        this.socket = socket;
        try{
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.chatServerConnectionListener = chatServer;
            this.username = null;
        }catch(IOException e){
//            customPrint(username+);
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
            case QUERY_SEND_MESSAGE -> {
                chatServerConnectionListener.clientMessageRecieved(this, message);
            }
            case QUERY_CLIENT_LIST -> {
                chatServerConnectionListener.clientListRequestRecieved(this);
            }
            case QUERY_EDIT_ACCOUNT_WITH_PASSWORD_AND_WITH_USERNAME -> {
                chatServerConnectionListener.editAccountWithPasswordAndWithUsernameRequestReceived(this, message);
            }
            case QUERY_EDIT_ACCOUNT_WITHOUT_PASSWORD_AND_WITH_USERNAME -> {
                chatServerConnectionListener.editAccountWithoutPasswordAndWithUsernameRequestReceived(this, message);
            }
            case QUERY_EDIT_ACCOUNT_WITH_PASSWORD_AND_WITHOUT_USERNAME -> {
                chatServerConnectionListener.editAccountWithPasswordAndWithoutUsernameRequestReceived(this, message);
            }
            case QUERY_EDIT_ACCOUNT_WITHOUT_PASSWORD_AND_WITHOUT_USERNAME -> {
                chatServerConnectionListener.editAccountWithoutPasswordAndWithoutUsernameRequestReceived(this, message);
            }
            case QUERY_DELETE_ACCOUNT -> {
                chatServerConnectionListener.deleteAccountRequestReceived(this, message);
            }
        }
    }

    private void receiveMessage(){
        try{
            while(true){
                if (objectInputStream.available() >= 0){
                    HashMap<String, Object> message = (HashMap<String, Object>)objectInputStream.readUnshared();
                    customPrint("Message recieved from "+username);
                    resolveMessage(message);
                }
            }
        }catch (Exception e){
            if (!disconnectRequested)
                chatServerConnectionListener.clientDisconnected(this, username);
        }
    }

    public void sendMessage(HashMap<String, Object> message){
        try{
            System.out.println(message);
            objectOutputStream.writeObject(message);
            objectOutputStream.reset();
        }catch(IOException e){
            if (!disconnectRequested)
                chatServerConnectionListener.clientDisconnected(this, username);
        }
    }

    public void disconnect(){
        try{
            disconnectRequested = true;
            objectOutputStream.close();
            objectInputStream.close();
            socket.close();
        }catch (IOException e){
//            Sys
        }
    }

    public void init() {
        receiveMessage();
    }

    public InetAddress getIP(){
        return socket.getInetAddress();
    }

}