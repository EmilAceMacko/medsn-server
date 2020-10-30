package src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

class Client_Manager
{
    private MEDSN_Server owner;
    private int port = 8000;
    //private DataInputStream in;
    //private DataOutputStream out;
    private ServerSocket serverSocket;
    private ArrayList<Client> clientList;

    Client_Manager(MEDSN_Server _owner)
    {
        owner = _owner;
        clientList = new ArrayList<>();
        try
        {
            serverSocket = new ServerSocket(port); // Create the server socket.
        }
        catch(IOException e)
        {
            System.err.println("SERVER ERROR: " + e + " - Could not set up server socket on port " + port + ".");
            System.exit(-1); // Close the program.
        }
    }

    // Handle input strings from the connected Clients:
    void handleClientInput(String msg)
    {

    }

    // Check if any Clients are requesting to connect to the server (meant to happen continuously):
    void receiveConnections()
    {
        Socket socket = null;
        try
        {
            // Check if we received a connection from a client:
            socket = serverSocket.accept();

            if(socket != null)
            {
                // Get IO streams from socket:
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                // Check if this new client is attempting to establish a connection:
                if(in.readShort() == owner.NET_CLIENT_JOIN_REQUEST)
                {
                    // Get client "credentials":
                    byte[] array1 = new byte[in.readInt()];
                    in.read(array1);
                    String username = Arrays.toString(array1);

                    byte[] array2 = new byte[in.readInt()];
                    in.read(array2);
                    String password = Arrays.toString(array2);

                    String address = socket.getRemoteSocketAddress().toString();

                    // Check if this new client is allowed to connect:

                    // Check for username in banNameList:
                    ArrayList<String> banList = owner.banNameList;
                    boolean foundNameBan = false;
                    for(String name : banList)
                    {
                        if(name.equals(username))
                        {
                            foundNameBan = true;
                            break;
                        }
                    }

                    if(!foundNameBan)
                    {
                        // Check for IP address in banAddressList:
                        banList = owner.banAddressList;
                        boolean foundAddressBan = false;
                        for(String ip : banList)
                        {
                            if(ip.equals(address))
                            {
                                foundAddressBan = true;
                                break;
                            }
                        }
                    }

                    if(true) // TODO Add some conditions!
                    {
                        Client newClient = new Client(socket, username, this);
                        clientList.add(newClient);
                        //newClient.start();
                    }
                }
            }
        }
        catch(IOException e)
        {
            System.err.println("SERVER ERROR: " + e + " - Could not accept socket.");
        }
    }

    // Disconnects all connected Clients (with the message that the server is closing):
    void terminateConnections()
    {

    }

    // Kick (disconnect) a specific client (by their IP Address):
    boolean kickClientIP(String ip)
    {
        return false; // TODO Add the kick ip code
    }

    // Kick (disconnect) a specific client (by their Username):
    boolean kickClientName(String name)
    {
        return false; // TODO Add the kick username code
    }

    // Ban a specific client (by their IP Address):
    boolean banClientIP(String ip)
    {
        return false; // TODO Add the ban ip code
    }

    // Ban a specific client (by their Username):
    boolean banClientName(String name)
    {
        return false; // TODO Add the ban username code
    }

    // Pardon (unban) a specific client (by their IP Address):
    boolean pardonClientIP(String ip)
    {
        return false; // TODO Add the pardon ip code
    }

    // Pardon (unban) a specific client (by their Username):
    boolean pardonClientName(String name)
    {
        return false; // TODO Add the pardon username code
    }
}