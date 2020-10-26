package src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

                    // Check if this new client is allowed to connect:
                    if(true) // TODO Add some conditions!
                    {
                        Client newClient = new Client(socket, this);
                        clientList.add(newClient);
                    }
                }


            }
        }
        catch(IOException e)
        {
            System.err.println("SERVER ERROR: " + e + " - Could not accept socket.");
        }


    }

    void terminateConnections()
    {

    }
}