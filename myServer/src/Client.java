package src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    private Client_Manager owner;
    public String username;
    public String address;
    public Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    boolean listening;
    boolean admin;

    //Constructor
    public Client(Socket newClientSocket, String newUsername, Client_Manager newOwner) {
        this.clientSocket = newClientSocket;
        this.username = newUsername;
        this.owner = newOwner;
        address = clientSocket.getRemoteSocketAddress().toString();
    }

    public void run() {
        while (isListening()) {

        }
    }

    //Booleans are listed below
    public boolean isListening() {
        return listening;
    }

    public boolean isAdmin() {
        return admin;
    }

    //Setters are listed below
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void setListening(Boolean listening) {
        this.listening = listening;
    }
}
