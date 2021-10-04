package mychatserver.credentialnetwork.main;

import mychatserver.credentialnetwork.utils.interfaces.CredentialConnectionListener;

import java.io.IOException;
import java.util.concurrent.*;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import static mychatserver.globalutils.Misc.*;

/*
    Runs on a different port rather than the chat server. Clients get connected to
    this server first while attempting to use the app. After logging in/ signing up
    with the right credentials, they are redirected the the chat other server
*/
public class CredentialServer implements CredentialConnectionListener {

    private final SSLServerSocket serverSocket;
    private final ExecutorService executorService;
    private int clients;
    private final int MAX_CONNECTIONS = 5;

    public CredentialServer() throws IOException{
        customPrint("Initializing the credential server.");
        serverSocket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(5000);
        executorService = Executors.newFixedThreadPool(MAX_CONNECTIONS);
        clients = 0;
        customPrint("Done initializing the credential server.");
    }

    // Starts the server
    public void startServer() throws IOException{
        customPrint("Credential server up and and listening at port 5000...");
        while(true){
            if(clients < MAX_CONNECTIONS){
                SSLSocket socket = (SSLSocket) serverSocket.accept();
                executorService.submit(new CredentialServerThread(this, socket));
            }
        }
    }

    private synchronized void incrementClient(){
        ++clients;
    }

    private synchronized void decrementClient(){
        --clients;
    }

    @Override
    public void clientConnected() {
        incrementClient();
        customPrint("Client connected. Total connected: "+clients);
    }

    @Override
    public void clientDisconnected() {
        decrementClient();
        customPrint("Client disconnected. Total connected: "+clients);
    }

}