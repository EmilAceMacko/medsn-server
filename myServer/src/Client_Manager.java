package src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Client_Manager
{
    public MEDSN_Server owner;
    private int port = 8000;
    private ServerSocket serverSocket;
    private ArrayList<Client> clientList;
    //private ArrayList<Runnable> threadList;

    Client_Manager(MEDSN_Server _owner)
    {
        owner = _owner;
        clientList = new ArrayList<>();
        //threadList = new ArrayList<>();
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

    // Handle input strings from the connected Clients to be sent to the entire server:
    public void handleClientInput(String msg, Client client)
    {
        owner.handleServerString(msg, client);
    }

    // Handle notifying the server that the given Client has joined the server:
    public void handleClientJoin(Client client)
    {
        // The "user has joined" message:
        String joinMsg = client.username + " has joined the server.";
        // Broadcast the notification to all Clients except the Client which the notification is about:
        for(Client c : clientList)
        {
            if(!c.equals(client)) broadcast(joinMsg, c);
        }
    }

    // Handle notifying the server that the given Client has left the server:
    public void handleClientLeave(Client client)
    {
        // The "user has left" message:
        String leaveMsg = client.username + " has left the server.";
        // Broadcast the notification to all Clients except the Client which the notification is about:
        for(Client c : clientList)
        {
            if(!c.equals(client)) broadcast(leaveMsg, c);
        }
    }

    // Broadcast a string to all connected Clients on the server:
    public void broadcast(String msg)
    {
        // Loop through Client List:
        for(Client c : clientList)
        {
            broadcast(msg, c);
        }
    }

    // Broadcast a string to a specific connected Client:
    public void broadcast(String msg, Client client)
    {
        byte[] array = msg.getBytes();
        int length = array.length;

        client.out.writeShort(owner.NET_SERVER_CHAT);
        client.out.writeInt(length);
        client.out.write(array);
    }

    // Disconnect the given Client from the server: (Only run if the Client itself initiated a disconnect!)
    public void disconnectClient(Client client)
    {
        // Broadcast to the rest of the server that this Client has left the server.
        handleClientLeave(client);
        // Stop the IO thread process in the Client:
        client.setListening(false);
        // Remove the Client from the Client List:
        clientList.remove(client);
        // Remove the Thread from the Thread List:
        //threadList.clear();
    }

    // Check and handle any Clients that request to connect to the server: (Meant to happen continuously!)
    public void receiveConnections()
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
                    // ---------------- Get client "credentials":
                    byte[] array = new byte[in.readInt()];
                    in.read(array);
                    String username = Arrays.toString(array);

                    array = new byte[in.readInt()];
                    in.read(array);
                    String password = Arrays.toString(array);

                    String address = socket.getRemoteSocketAddress().toString();

                    // ---------------- Check if this new client is allowed to connect:
                    boolean roomForClient = true;
                    boolean clientAlreadyConnected = false;
                    boolean foundNameBan = false;
                    boolean foundAddressBan = false;
                    boolean correctAdminPassword = false;

                    // Check if there is room on the server.
                    if(clientList.size() >= owner.getMaxClients())
                    {
                        roomForClient = false;

                        // Check if the Client is already connected:
                        for(Client c : clientList)
                        {
                            if(getClientIP(c).equals(address))
                            {
                                clientAlreadyConnected = true;
                                break;
                            }
                        }

                        // Check for username in Ban Lists:
                        ArrayList<String> banList = owner.banNameList;
                        for(String name : banList)
                        {
                            if(name.equals(username))
                            {
                                foundNameBan = true;
                                break;
                            }
                        }

                        if(!foundNameBan) // If the name was already found, don't check if the IP is also banned.
                        {
                            // Check for IP address in banAddressList:
                            banList = owner.banAddressList;
                            for(String ip : banList)
                            {
                                if(ip.equals(address))
                                {
                                    foundAddressBan = true;
                                    break;
                                }
                            }
                        }
                    }

                    // Evaluate join conditions:
                    if(roomForClient) // If there's room for the Client:
                    {
                        if(clientAlreadyConnected)
                        {
                            // Report back to the Client that the connection was denied because their IP Address was already connected.
                            out.writeShort(owner.NET_SERVER_JOIN_DENY_DUPLICATE);
                        }
                        else if(foundNameBan) // If Client's username has been banned:
                        {
                            // Report back to the Client that the connection was denied because their username is banned.
                            out.writeShort(owner.NET_SERVER_JOIN_DENY_BANNED_NAME);
                        }
                        else if(foundAddressBan) // If Client's IP address has been banned:
                        {
                            // Report back to the Client that the connection was denied because their IP Address is banned.
                            out.writeShort(owner.NET_SERVER_JOIN_DENY_BANNED_IP);
                        }
                        else // Client is allowed to connect:
                        {
                            // Create a new Client instance with the socket and username:
                            Client newClient = new Client(socket, username, this);
                            // Give the Client admin privileges if they provided the correct admin password:
                            if(correctAdminPassword) newClient.setAdmin(true);
                            // Add the new Client instance to the Client List:
                            clientList.add(newClient);
                            // Create a new Client Thread, add it to the Thread List, and start its process:
                            Thread clientThread = new Thread(newClient);
                            //threadList.add(clientThread);
                            clientThread.start();
                            // Report back to the Client that the connection was accepted:
                            out.writeShort(owner.NET_SERVER_JOIN_ACCEPT);
                            // Notify the server that the Client has joined:
                            handleClientJoin(newClient);
                        }
                    }
                    else // Server is full, no room for the new client:
                    {
                        // Report back to the Client that the connection was denied because the server is full.
                        out.writeShort(owner.NET_SERVER_JOIN_DENY_NO_ROOM);
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
    public void terminateConnections()
    {
        // Loop through Client List:
        for(Client c : clientList)
        {
            // Send the "server quit" message with the proper identifier:
            c.out.writeShort(owner.NET_SERVER_QUIT);
            // Stop the IO thread process in the Client:
            c.setListening(false);
        }

        // Clear the Client List and Thread List:
        clientList.clear();
        //threadList.clear();
    }

    // Kick (disconnect) a specific client (by their IP Address): (Returns false if Client is not found.)
    public boolean kickClientIP(String ip)
    {
        return false; // TODO Add the kick ip code!
    }

    // Kick (disconnect) a specific client (by their Username): (Returns false if Client is not found.)
    public boolean kickClientName(String name)
    {
        return false; // TODO Add the kick username code!
    }

    // Ban a specific client (by their IP Address):
    public boolean banClientIP(String ip)
    {
        return false; // TODO Add the ban ip code!
    }

    // Ban a specific client (by their Username):
    public boolean banClientName(String name)
    {
        return false; // TODO Add the ban username code!
    }

    // Pardon (unban) a specific client (by their IP Address):
    public boolean pardonClientIP(String ip)
    {
        return false; // TODO Add the pardon ip code!
    }

    // Pardon (unban) a specific client (by their Username):
    boolean pardonClientName(String name)
    {
        return false; // TODO Add the pardon username code!
    }

    // Get a specific Client's IP Address by either providing a name or a reference to the Client instance: (Returns null if Client is not found.)
    public String getClientIP(String name)
    {
        for(Client c : clientList)
        {
            if(c.username.equals(name)) return getClientIP(c);
        }
        return null;
    }
    public String getClientIP(Client client)
    {
        return client.clientSocket.getRemoteSocketAddress().toString();
    }

    // Get a reference to a specific Client with the given IP Address: (Returns null if Client is not found.)
    public Client findClientWithIP(String ip)
    {
        for(Client c : clientList)
        {
            if(getClientIP(c).equals(ip)) return c;
        }
        return null;
    }

    // Get a reference to a specific Client with the given username: (Returns null if Client is not found.)
    public Client findClientWithName(String name)
    {
        for(Client c : clientList)
        {
            if(c.username.equals(name)) return c;
        }
        return null;
    }
}