package mychatserver.credentialnetwork.main;

import mychatserver.credentialnetwork.utils.interfaces.CredentialConnectionListener;
import mychatserver.globalutils.DatabaseHandler;
import mychatserver.globalutils.MessageResponder;

import static mychatserver.globalutils.KeyValues.*;
import static mychatserver.globalutils.Misc.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import javax.net.ssl.SSLSocket;
/*

    This class spawns everythime a connection to the Credential server
    is made. This class facilitates the actual mechanism of login/signup
    of the chat application.

*/
public class CredentialServerThread implements Runnable{

    private final CredentialConnectionListener connectionListener;
    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;

    public CredentialServerThread(CredentialServer cs, SSLSocket socket) throws IOException{
        this.connectionListener = cs;
        connectionListener.clientConnected();
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    // Gets the query type of the message and acts accordingly
    private void resolveMessage(HashMap<String, Object> message)throws IOException, ClassNotFoundException{
        int query = getQueryCode(message);
        switch (query) {
            case QUERY_LOGIN_REQUEST -> validateLogin(message);
            case QUERY_SIGNUP_REQUEST -> commitSignup(message);
        }
    }

    // Sends the hashmap to the client
    private void sendMessageToClient(HashMap<String, Object> message)throws IOException{
        objectOutputStream.writeObject(message);
    }


    // Gets called if a login query was requested
    private void validateLogin(HashMap<String, Object> message)throws IOException{
        String username = message.get(KEY_USERNAME).toString();
        String password = message.get(KEY_PASSWORD).toString();
        HashMap<String, Object> responseMessage = null;
        if(DatabaseHandler.isAliasValid(username)){
            if(DatabaseHandler.isLoginValid(username, password)){
                responseMessage = MessageResponder.respondToLoginRequestMessage(RESPONSE_CODE_SUCCESS, "");
            }else{
                responseMessage = MessageResponder.respondToLoginRequestMessage(RESPONSE_CODE_FAILURE, "Incorrect password!");
            }
        }else{
            responseMessage = MessageResponder.respondToLoginRequestMessage(RESPONSE_CODE_FAILURE, "Username doesn't exist!");
        }
        sendMessageToClient(responseMessage);
    }

    private void commitSignup(HashMap<String, Object> message) throws IOException{
        String firstName = (String)message.get(KEY_FIRST_NAME);
        String lastName = (String)message.get(KEY_LAST_NAME);
        String email = (String)message.get(KEY_EMAIL);
        String password = (String)message.get(KEY_PASSWORD);
        String alias = (String)message.get(KEY_USERNAME);
        int result = DatabaseHandler.createAccount(firstName, lastName, email, password, alias);
        HashMap<String, Object> responseMessage = null;
        if (result == DatabaseHandler.SIGNUP_ALIAS_EXISTS_ERROR){
            responseMessage = MessageResponder.respondToSignupRequestMessage(RESPONSE_CODE_FAILURE, "The specified username already exists!");
        }else if (result == DatabaseHandler.SIGNUP_EMAIL_EXISTS_ERROR){
            responseMessage = MessageResponder.respondToSignupRequestMessage(RESPONSE_CODE_FAILURE, "The specified email already exists!");
        }else if (result == DatabaseHandler.SIGNUP_INTERNAL_ERROR){
            responseMessage = MessageResponder.respondToSignupRequestMessage(RESPONSE_CODE_FAILURE, "Internal server error! Please try again.");
        }else if (result == DatabaseHandler.SIGNUP_SUCCESS){
            responseMessage = MessageResponder.respondToSignupRequestMessage(RESPONSE_CODE_SUCCESS, "");
        }
        sendMessageToClient(responseMessage);
    }

    // Keeps checking for client input
    private void getInputFromClient(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                while(true){
                    try{
                        if(objectInputStream.available()>=0){
                            HashMap m = (HashMap) objectInputStream.readObject();
                            resolveMessage(m);
                        }
                    }catch(Exception e){
                        connectionListener.clientDisconnected();
                        break;
                    }
                }
            }
        }).start();
    }

    @Override
    public void run(){
        getInputFromClient();
    }
}