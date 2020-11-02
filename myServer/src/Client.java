package src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class Client implements Runnable {
    private Client_Manager owner;
    public String username;
    public String address;
    public Socket clientSocket;
    public DataInputStream in;
    public DataOutputStream out;
    boolean listening;
    boolean admin;

    //Constructor
    public Client(Socket newClientSocket, String newUsername, Client_Manager newOwner) {
        this.clientSocket = newClientSocket;
        this.username = newUsername;
        this.owner = newOwner;
        address = clientSocket.getRemoteSocketAddress().toString();
        try {
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            }
        catch (IOException e) {
            System.err.println("Client could not get input stream");
        }
    }

    //Reads message written by client
    public void run() {
        try {
            while (listening) {
                short input = in.readShort();
                if (input == MEDSN_Server.NET_CLIENT_CHAT) {
                    byte[] array = new byte[in.readInt()];
                    in.read(array);
                    String message = Arrays.toString(array);
                    owner.handleClientInput(message, this);
                }
            }
        }
        catch (IOException e) {
            System.err.println("Client could not read short");
        }
        //Stops thread when while loop is done
        return;
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
