package mychatserver.chatnetwork.main;

import mychatserver.chatnetwork.utils.interfaces.ChatServerConnectionListener;
import mychatserver.globalutils.DatabaseHandler;
import mychatserver.globalutils.MessageResponder;
import static mychatserver.globalutils.KeyValues.*;
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
        decrementClient();
        customPrint(username+" disconnected. Total clients: "+clients);
        activeUsers.remove(user);
        inactiveUsers.add(user);
        threads.remove(chatServerThread);
        sendMessageToEveryone(MessageResponder.respondToClientListRequestMessage(activeUsers, inactiveUsers));
    }

    @Override
    public void clientMessageRecieved(ChatServerThread chatServerThread, HashMap<String, Object> message) {
        customPrint((String) message.get(KEY_USERNAME)+" sent a message: "+message);
        existingMessages.add((ArrayList<String>) message.get(KEY_MESSAGE));
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

    @Override
    public void editAccountWithPasswordAndWithUsernameRequestReceived(ChatServerThread chatServerThread, HashMap<String, Object> message) {
        HashMap<String, Object> user = DatabaseHandler.getDetailsFromUsername(chatServerThread.username);
        String username = (String) message.get(KEY_USERNAME);
        String firstName = (String) message.get(KEY_FIRST_NAME);
        String lastName = (String) message.get(KEY_LAST_NAME);
        String email = (String) message.get(KEY_EMAIL);
        String password = (String) message.get(KEY_PASSWORD);
        switch (DatabaseHandler.editAccountWithPasswordAndWithUsername(username, firstName, lastName, password, email)){
            case DatabaseHandler.ACCOUNT_EDIT_SUCCESSFUL -> {
                chatServerThread.sendMessage(MessageResponder.respondToEditAccountRequestMessage(RESPONSE_CODE_SUCCESS, "Account edited successfully!"));
                disconnectClient(chatServerThread, user);
            }
            case DatabaseHandler.INTERNAL_SERVER_ERROR -> chatServerThread.sendMessage(MessageResponder.respondToEditAccountRequestMessage(RESPONSE_CODE_FAILURE, "Internal server error!"));
            case DatabaseHandler.ALIAS_EXISTS_ERROR -> chatServerThread.sendMessage(MessageResponder.respondToEditAccountRequestMessage(RESPONSE_CODE_FAILURE, "That username is already taken!"));
        }
    }

    @Override
    public void editAccountWithoutPasswordAndWithUsernameRequestReceived(ChatServerThread chatServerThread, HashMap<String, Object> message) {
        HashMap<String, Object> user = DatabaseHandler.getDetailsFromUsername(chatServerThread.username);
        String username = (String) message.get(KEY_USERNAME);
        String firstName = (String) message.get(KEY_FIRST_NAME);
        String lastName = (String) message.get(KEY_LAST_NAME);
        String email = (String) message.get(KEY_EMAIL);
        switch (DatabaseHandler.editAccountWithoutPasswordAndWithUsername(username, firstName, lastName, email)){
            case DatabaseHandler.ACCOUNT_EDIT_SUCCESSFUL -> {
                chatServerThread.sendMessage(MessageResponder.respondToEditAccountRequestMessage(RESPONSE_CODE_SUCCESS, "Account edited successfully!"));
                disconnectClient(chatServerThread, user);
            }
            case DatabaseHandler.INTERNAL_SERVER_ERROR -> chatServerThread.sendMessage(MessageResponder.respondToEditAccountRequestMessage(RESPONSE_CODE_FAILURE, "Internal server error!"));
            case DatabaseHandler.ALIAS_EXISTS_ERROR -> chatServerThread.sendMessage(MessageResponder.respondToEditAccountRequestMessage(RESPONSE_CODE_FAILURE, "That username is already taken!"));
        }
    }

    @Override
    public void editAccountWithPasswordAndWithoutUsernameRequestReceived(ChatServerThread chatServerThread, HashMap<String, Object> message) {
        HashMap<String, Object> user = DatabaseHandler.getDetailsFromUsername(chatServerThread.username);
        String firstName = (String) message.get(KEY_FIRST_NAME);
        String lastName = (String) message.get(KEY_LAST_NAME);
        String email = (String) message.get(KEY_EMAIL);
        String password = (String) message.get(KEY_PASSWORD);
        switch (DatabaseHandler.editAccountWithPasswordAndWithoutUsername(password, firstName, lastName, email)){
            case DatabaseHandler.ACCOUNT_EDIT_SUCCESSFUL -> {
                chatServerThread.sendMessage(MessageResponder.respondToEditAccountRequestMessage(RESPONSE_CODE_SUCCESS, "Account edited successfully!"));
                disconnectClient(chatServerThread, user);
            }
            case DatabaseHandler.INTERNAL_SERVER_ERROR -> chatServerThread.sendMessage(MessageResponder.respondToEditAccountRequestMessage(RESPONSE_CODE_FAILURE, "Internal server error!"));
            case DatabaseHandler.ALIAS_EXISTS_ERROR -> chatServerThread.sendMessage(MessageResponder.respondToEditAccountRequestMessage(RESPONSE_CODE_FAILURE, "That username is already taken!"));
        }
    }

    @Override
    public void editAccountWithoutPasswordAndWithoutUsernameRequestReceived(ChatServerThread chatServerThread, HashMap<String, Object> message) {
        HashMap<String, Object> user = DatabaseHandler.getDetailsFromUsername(chatServerThread.username);
        String firstName = (String) message.get(KEY_FIRST_NAME);
        String lastName = (String) message.get(KEY_LAST_NAME);
        String email = (String) message.get(KEY_EMAIL);
        switch (DatabaseHandler.editAccountWithoutPasswordAndWithoutUsername(firstName, lastName, email)){
            case DatabaseHandler.ACCOUNT_EDIT_SUCCESSFUL -> {
                chatServerThread.sendMessage(MessageResponder.respondToEditAccountRequestMessage(RESPONSE_CODE_SUCCESS, "Account edited successfully!"));
                disconnectClient(chatServerThread, user);
            }
            case DatabaseHandler.INTERNAL_SERVER_ERROR -> chatServerThread.sendMessage(MessageResponder.respondToEditAccountRequestMessage(RESPONSE_CODE_FAILURE, "Internal server error!"));
            case DatabaseHandler.ALIAS_EXISTS_ERROR -> chatServerThread.sendMessage(MessageResponder.respondToEditAccountRequestMessage(RESPONSE_CODE_FAILURE, "That username is already taken!"));
        }
    }

    @Override
    public void deleteAccountRequestReceived(ChatServerThread chatServerThread, HashMap<String, Object> message) {
        HashMap<String, Object> user = DatabaseHandler.getDetailsFromUsername(chatServerThread.username);
        String username = (String) message.get(KEY_USERNAME);
        switch (DatabaseHandler.deleteAccount(username)){
            case DatabaseHandler.ACCOUNT_DELETE_SUCCESSFUL -> {
                chatServerThread.sendMessage(MessageResponder.respondToDeleteAccountRequestMessage(RESPONSE_CODE_SUCCESS, ""));
                disconnectClient(chatServerThread, user);
            }
            case DatabaseHandler.INTERNAL_SERVER_ERROR ->  chatServerThread.sendMessage(MessageResponder.respondToDeleteAccountRequestMessage(RESPONSE_CODE_FAILURE, "Internal server error!"));
        }
    }

    private void disconnectClient(ChatServerThread chatServerThread, HashMap<String, Object> user){
        inactiveUsers = DatabaseHandler.getAllUsers();
        activeUsers.remove(user);
        for (HashMap<String, Object> activeUser : activeUsers)
            inactiveUsers.remove(activeUser);
        chatServerThread.disconnect();
        threads.remove(chatServerThread);
        decrementClient();
        sendMessageToEveryone(MessageResponder.respondToClientListRequestMessage(activeUsers, inactiveUsers));
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