package src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Client {
    private Client_Manager owner;
    private String username;
    private String address;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;

    public Client(Socket newClientSocket, Client_Manager newOwner) {
        this.clientSocket = newClientSocket;
        this.owner = newOwner;
    }

    public <type> void Method () {
    }

}
