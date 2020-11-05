package src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketThread extends Thread
{
    Client_Manager owner;
    Socket socket = null;
    boolean accepting = true;

    public SocketThread(Client_Manager _owner)
    {
        owner = _owner;
    }

    public void run()
    {
        /*try
        {
            while (accepting)
            {
                socket = null;
                socket = owner.serverSocket.accept();
                owner.newSocket = socket;
            }
        }
        catch(IOException e)
        {
            System.err.println("SERVER ERROR: " + e + " - Could not accept socket.");
        }*/

        while (accepting)
        {
            Socket socket = null;
            try
            {
                // Check if we received a connection from a client:
                if(owner.serverSocket != null && !owner.serverSocket.isClosed()) socket = owner.serverSocket.accept();

                if (socket != null)
                {
                    // Get IO streams from socket:
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                    // Check if this new client is attempting to establish a connection:
                    if (in.readShort() == MEDSN_Server.NET_CLIENT_JOIN_REQUEST)
                    {
                        // ---------------- Get client "credentials":
                        byte[] array = new byte[in.readInt()];
                        in.read(array);
                        String username = new String(array, StandardCharsets.UTF_8);

                        array = new byte[in.readInt()];
                        in.read(array);
                        String password = new String(array, StandardCharsets.UTF_8);

                        String address = socket.getRemoteSocketAddress().toString();

                        // ---------------- Check if this new client is allowed to connect:
                        boolean roomForClient = true;
                        boolean clientAlreadyConnected = false;
                        boolean foundNameBan = false;
                        boolean foundAddressBan = false;
                        boolean correctAdminPassword = false;

                        // Check if there is room on the server.
                        if (owner.clientList.size() < MEDSN_Server.getMaxClients())
                        {
                            // Check if the Client is already connected:
                            for (Client c : owner.clientList)
                            {
                                if (c.address.equals(address))
                                {
                                    clientAlreadyConnected = true;
                                    break;
                                }
                            }

                            // Check for username in Ban Lists:
                            for (String name : MEDSN_Server.banNameList)
                            {
                                if (name.equals(username))
                                {
                                    foundNameBan = true;
                                    break;
                                }
                            }

                            if (!foundNameBan) // If the name was already found, don't check if the IP is also banned.
                            {
                                // Check for IP address in banAddressList:
                                for (String ip : MEDSN_Server.banAddressList)
                                {
                                    if (ip.equals(address))
                                    {
                                        foundAddressBan = true;
                                        break;
                                    }
                                }
                            }
                        } else roomForClient = false; // No room.

                        // Check if admin password is correct:
                        if (password.equals(MEDSN_Server.adminPassword)) correctAdminPassword = true;

                        // Evaluate join conditions:
                        if (roomForClient) // If there's room for the Client:
                        {
                            if (clientAlreadyConnected)
                            {
                                // Report back to the Client that the connection was denied because their IP Address was already connected.
                                out.writeShort(MEDSN_Server.NET_SERVER_JOIN_DENY_DUPLICATE);
                            } else if (foundNameBan) // If Client's username has been banned:
                            {
                                // Report back to the Client that the connection was denied because their username is banned.
                                out.writeShort(MEDSN_Server.NET_SERVER_JOIN_DENY_BANNED_NAME);
                            } else if (foundAddressBan) // If Client's IP address has been banned:
                            {
                                // Report back to the Client that the connection was denied because their IP Address is banned.
                                out.writeShort(MEDSN_Server.NET_SERVER_JOIN_DENY_BANNED_IP);
                            } else// Client is allowed to connect:
                            {
                                // Check for (and fix) duplicate names:
                                String newUsername = username;
                                int num = 0;
                                for(Client c : owner.clientList)
                                {
                                    if(c.realName.equals(username))
                                    {
                                        num++;
                                    }
                                }
                                if(num > 0) newUsername = username + Integer.toString(num + 1);

                                System.out.println("The new client is allowed to connect.");
                                // Create a new Client instance with the socket and username:
                                Client newClient = new Client(socket, newUsername, owner);
                                newClient.realName = username; // Give a REAL username, that the client wished to be known as. (The "username" variable in Client is what the server will see.)
                                // Give the Client admin privileges if they provided the correct admin password:
                                if (correctAdminPassword) newClient.setAdmin(true);
                                // Add the new Client instance to the Client List:
                                owner.clientList.add(newClient);
                                // Create a new Client Thread, add it to the Thread List, and start its process:
                                Thread clientThread = new Thread(newClient);
                                //threadList.add(clientThread);
                                clientThread.start();
                                // Report back to the Client that the connection was accepted:
                                out.flush();
                                out.writeShort(MEDSN_Server.NET_SERVER_JOIN_ACCEPT);
                                out.flush();
                                // Notify the server that the Client has joined:
                                owner.handleClientJoin(newClient);
                            }
                        } else // Server is full, no room for the new client:
                        {
                            // Report back to the Client that the connection was denied because the server is full.
                            out.writeShort(MEDSN_Server.NET_SERVER_JOIN_DENY_NO_ROOM);
                        }
                    }
                }
            } catch (IOException e)
            {
                System.err.println("SERVER ERROR: " + e + " - Could not accept socket.");
            }
        }
    }

    public void setAccepting(boolean _accepting)
    {
        accepting = _accepting;
    }

    public Socket getSocket()
    {
        return socket;
    }
}
