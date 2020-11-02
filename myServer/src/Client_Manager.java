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
    //public MEDSN_Server owner;
    private int port = 8000;
    private ServerSocket serverSocket;
    private ArrayList<Client> clientList;
    //private ArrayList<Runnable> threadList;

    Client_Manager()
    {
        //owner = _owner;
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
        MEDSN_Server.handleServerString(msg, client);
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
        try
        {
            byte[] array = msg.getBytes();
            int length = array.length;

            client.out.writeShort(MEDSN_Server.NET_SERVER_CHAT);
            client.out.writeInt(length);
            client.out.write(array);
        }
        catch(IOException e)
        {
            System.err.println(e + " - could not broadcast message.");
        }
    }

    // Disconnect the given Client from the server: (Does NOT notify Client that it has been disconnected!)
    public void disconnectClient(Client client) throws IOException
    {
        disconnectClient(client, (short)0);
    }
    public void disconnectClient(Client client, short cond) throws IOException
    {
        String leaveMsg;
        // Notify the Clients that a Client has left the server (for some reason)
        // Set "user has left" message and broadcast to Client the reason for disconnect: (Switch-case doesn't work here because Java)
        if(cond == MEDSN_Server.NET_SERVER_JOIN_DENY_KICK) // Client was kicked:
        {
            leaveMsg = client.username + " has been kicked from the server.";
            client.out.writeShort(MEDSN_Server.NET_SERVER_JOIN_DENY_KICK);
        }
        else if(cond == MEDSN_Server.NET_SERVER_JOIN_DENY_BANNED_IP) // Client was banned (IP):
        {
            leaveMsg = client.username + " has been banned from the server.";
            client.out.writeShort(MEDSN_Server.NET_SERVER_JOIN_DENY_BANNED_IP);
        }
        else if(cond == MEDSN_Server.NET_SERVER_JOIN_DENY_BANNED_NAME) // Client was banned (username):
        {
            leaveMsg = client.username + " has been banned from the server.";
            client.out.writeShort(MEDSN_Server.NET_SERVER_JOIN_DENY_BANNED_NAME);
        }
        else // Client left on its own:
        {
            leaveMsg = client.username + " has left the server.";
        }

        // Stop the IO thread process in the Client:
        client.setListening(false);
        // Remove the Client from the Client List:
        clientList.remove(client);
        // Remove the Thread from the Thread List:
        //threadList.clear();

        // Broadcast the notification to all Clients except the Client with which the notification is about:
        for(Client c : clientList)
        {
            if(!c.equals(client)) broadcast(leaveMsg, c);
        }
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
                if(in.readShort() == MEDSN_Server.NET_CLIENT_JOIN_REQUEST)
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
                    if(clientList.size() < MEDSN_Server.getMaxClients())
                    {
                        // Check if the Client is already connected:
                        for(Client c : clientList)
                        {
                            if(c.address.equals(address))
                            {
                                clientAlreadyConnected = true;
                                break;
                            }
                        }

                        // Check for username in Ban Lists:
                        for(String name : MEDSN_Server.banNameList)
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
                            for(String ip : MEDSN_Server.banAddressList)
                            {
                                if(ip.equals(address))
                                {
                                    foundAddressBan = true;
                                    break;
                                }
                            }
                        }
                    }
                    else roomForClient = false; // No room.


                    // Check if admin password is correct:
                    if(password.equals(MEDSN_Server.adminPassword)) correctAdminPassword = true;


                    // Evaluate join conditions:
                    if(roomForClient) // If there's room for the Client:
                    {
                        if(clientAlreadyConnected)
                        {
                            // Report back to the Client that the connection was denied because their IP Address was already connected.
                            out.writeShort(MEDSN_Server.NET_SERVER_JOIN_DENY_DUPLICATE);
                        }
                        else if(foundNameBan) // If Client's username has been banned:
                        {
                            // Report back to the Client that the connection was denied because their username is banned.
                            out.writeShort(MEDSN_Server.NET_SERVER_JOIN_DENY_BANNED_NAME);
                        }
                        else if(foundAddressBan) // If Client's IP address has been banned:
                        {
                            // Report back to the Client that the connection was denied because their IP Address is banned.
                            out.writeShort(MEDSN_Server.NET_SERVER_JOIN_DENY_BANNED_IP);
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
                            out.writeShort(MEDSN_Server.NET_SERVER_JOIN_ACCEPT);
                            // Notify the server that the Client has joined:
                            handleClientJoin(newClient);
                        }
                    }
                    else // Server is full, no room for the new client:
                    {
                        // Report back to the Client that the connection was denied because the server is full.
                        out.writeShort(MEDSN_Server.NET_SERVER_JOIN_DENY_NO_ROOM);
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
        try
        {
            // Loop through Client List:
            for (Client c : clientList)
            {
                // Send the "server quit" message with the proper identifier:
                c.out.writeShort(MEDSN_Server.NET_SERVER_QUIT);
                // Stop the IO thread process in the Client:
                c.setListening(false);
            }
        }
        catch(IOException e)
        {
            System.err.println(e + " - Could not terminate connections.");
        }
        // Clear the Client List and Thread List:
        clientList.clear();
        //threadList.clear();
    }

    // Kick (disconnect) a specific client (by their IP Address): (Returns false if Client is not found.)
    public boolean kickClientIP(String ip)
    {
        boolean found = false;
        Client client = findClientWithIP(ip);
        if(client != null)
        {
            kickClient(client);
            found = true;
        }
        return found;
    }

    // Kick (disconnect) a specific client (by their Username): (Returns false if Client is not found.)
    public boolean kickClientName(String name)
    {
        boolean found = false;
        Client client = findClientWithName(name);
        if(client != null)
        {
            kickClient(client);
            found = true;
        }
        return found;
    }

    // Kick (disconnect) a specific client:
    public void kickClient(Client client)
    {
        try
        {
            disconnectClient(client, MEDSN_Server.NET_SERVER_JOIN_DENY_KICK);
        }
        catch(IOException e)
        {
            System.err.println("Could not kick client with username " + client.username + " and IP " + client.address);
        }
    }

    // Ban a specific client (by their IP Address): (Returns false if the Client was not connected when banned.)
    public boolean banClientIP(String ip)
    {
        // Add the IP Address to the ban list:
        MEDSN_Server.banAddressList.add(ip);
        // Find and disconnect the client:
        boolean found = false;
        Client client = findClientWithIP(ip);
        if(client != null)
        {
            try
            {
                disconnectClient(client, MEDSN_Server.NET_SERVER_JOIN_DENY_BANNED_IP);
                found = true; // The client was successfully disconnected when banned.
            }
            catch(IOException e)
            {
                System.err.println("Could not disconnect client with username " + client.username + " and IP " + client.address + " when IP-banned.");
            }
        }
        return found;
    }

    // Ban a specific client (by their Username): (Returns false if the Client was not connected when banned.)
    public boolean banClientName(String name)
    {
        // Add the IP Address to the ban list:
        MEDSN_Server.banNameList.add(name);
        // Find and disconnect the client:
        boolean found = false;
        Client client = findClientWithIP(name);
        if(client != null)
        {
            try
            {
                disconnectClient(client, MEDSN_Server.NET_SERVER_JOIN_DENY_BANNED_NAME);
                found = true; // The client was successfully disconnected when banned.
            }
            catch(IOException e)
            {
                System.err.println("Could not disconnect client with username " + client.username + " and IP " + client.address + " when name-banned.");
            }
        }
        return found;
    }

    // Pardon (unban) a specific client (by their IP Address): (Returns false if Client is not found.)
    public boolean pardonClientIP(String ip)
    {
        boolean found = false;
        for(String s : MEDSN_Server.banAddressList)
        {
            if(s.equals(ip))
            {
                MEDSN_Server.banAddressList.remove(s);
                found = true;
            }
        }
        return found;
    }

    // Pardon (unban) a specific client (by their Username): (Returns false if Client is not found.)
    boolean pardonClientName(String name)
    {
        boolean found = false;
        for(String s : MEDSN_Server.banNameList)
        {
            if(s.equals(name))
            {
                MEDSN_Server.banNameList.remove(s);
                found = true;
            }
        }
        return found;
    }

    // Get a reference to a specific Client with the given IP Address: (Returns null if Client is not found.)
    public Client findClientWithIP(String ip)
    {
        for(Client c : clientList)
        {
            if(c.address.equals(ip)) return c;
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