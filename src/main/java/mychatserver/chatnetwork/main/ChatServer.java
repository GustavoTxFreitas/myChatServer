package mychatserver.chatnetwork.main;

import mychatserver.chatnetwork.utils.interfaces.ChatServerConnectionListener;
import mychatserver.globalutils.DatabaseHandler;
import mychatserver.globalutils.KeyValues;
import mychatserver.globalutils.MessageResponder;

import static mychatserver.globalutils.Misc.*;

import java.io.IOException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLSocket;

public class ChatServer implements ChatServerConnectionListener {

    private final SSLServerSocket serverSocket;
    private final ExecutorService executorService;
    private final ArrayList<ChatServerThread> threads;
    private static ArrayList<HashMap<String, Object>> activeUsers = null;
    private static ArrayList<HashMap<String, Object>> inactiveUsers = null;
    private int clients;
    private final int MAX_CONNECTIONS = 5;

    /* Message Format = {
    ("senderA", "messageA"),
    ("senderB", "messageB"),
    ...
    }
     */
    private final ArrayList<ArrayList<String>> existingMessages;

    public ChatServer()throws IOException{
        customPrint("Initializing the chat server.");
        serverSocket = (SSLServerSocket)((SSLServerSocketFactory) SSLServerSocketFactory.getDefault()).createServerSocket(6000);
        executorService = Executors.newFixedThreadPool(MAX_CONNECTIONS);
        clients = 0;
        threads = new ArrayList<>();
        activeUsers = new ArrayList<>();
        existingMessages = new ArrayList<>();
        inactiveUsers = DatabaseHandler.getAllUsers();
        customPrint("Done initializing the chat server.");
    }

    public void startServer()throws IOException{
        customPrint("Chat server up and and listening at port 6000...");
        while(true){
            SSLSocket socket = (SSLSocket) serverSocket.accept();
            ChatServerThread cst = new ChatServerThread(socket, this);
            threads.add(cst);
            executorService.submit(new Runnable(){
                @Override
                public void run() {
                    cst.init();
                }
            });
        }
    }

    @Override
    public void clientDisconnected(ChatServerThread chatServerThread, String username) {
        HashMap<String, Object> user = DatabaseHandler.getDetailsFromUsername(username);
        System.out.println(user);
        decrementClient();
        customPrint(username+" disconnected. Total clients: "+clients);
        activeUsers.remove(user);
        inactiveUsers.add(user);
        threads.remove(chatServerThread);
        sendMessageToEveryone(MessageResponder.respondToClientListRequestMessage(activeUsers, inactiveUsers));
    }

    @Override
    public void clientMessageRecieved(ChatServerThread chatServerThread, HashMap<String, Object> message) {
        customPrint((String) message.get(KeyValues.KEY_USERNAME)+" sent a message: "+message);
        existingMessages.add((ArrayList<String>) message.get(KeyValues.KEY_MESSAGE));
        sendMessageToEveryoneExcept(message, chatServerThread);
    }

    @Override
    public void clientListRequestRecieved(ChatServerThread chatServerThread) {
        customPrint("Client list requested by "+chatServerThread.username);
        sendMessageToUser(MessageResponder.respondToClientListRequestMessage(activeUsers, inactiveUsers), chatServerThread);
    }

    @Override
    public void clientConnected(ChatServerThread chatServerThread, String username) {
        HashMap<String, Object> user = DatabaseHandler.getDetailsFromUsername(username);
        incrementClient();
        customPrint(username+" connected: User IP: "+chatServerThread.getIP()+". Total connected: "+clients);
        activeUsers.add(user);
        inactiveUsers.remove(user);
        sendMessageToUser(MessageResponder.respondToHandshakeMessage(existingMessages, user), chatServerThread);
        sendMessageToEveryoneExcept(MessageResponder.respondToClientListRequestMessage(activeUsers, inactiveUsers), chatServerThread);
    }

    private void sendMessageToEveryone(HashMap<String, Object> message){
        for (ChatServerThread chatServerThread : threads) {
            customPrint("Sending message to " + chatServerThread.username);
            chatServerThread.sendMessage(message);
        }
    }

    private void sendMessageToEveryoneExcept(HashMap<String, Object> message, ChatServerThread cst){
        for (ChatServerThread chatServerThread : threads) {
            customPrint("Sending message to " + chatServerThread.username);
            if (chatServerThread != cst)
                chatServerThread.sendMessage(message);
        }
    }

    private void sendMessageToUser(HashMap<String, Object> message, ChatServerThread cst){
        customPrint("Sending message to " + cst.username);
        cst.sendMessage(message);
    }

    private synchronized void incrementClient(){
        ++clients;
    }

    private synchronized void decrementClient(){
        --clients;
    }

}