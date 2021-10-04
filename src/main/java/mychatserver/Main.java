package mychatserver;

import mychatserver.chatnetwork.main.ChatServer;
import mychatserver.chatnetwork.utils.classes.ChatServerDatabaseHandler;
import mychatserver.credentialnetwork.main.CredentialServer;
import mychatserver.credentialnetwork.utils.classes.CredentialDatabaseHandler;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            CredentialDatabaseHandler.getConnection();
            ChatServerDatabaseHandler.getConnection();
            System.setProperty("javax.net.ssl.keyStore", "src/main/java/myChatKeyStore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "cPPMq4IXThdRK1gf");
            CredentialServer credentialServer = new CredentialServer();
            ChatServer chatServer = new ChatServer();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        credentialServer.startServer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        chatServer.startServer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
