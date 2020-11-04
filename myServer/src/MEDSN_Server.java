package src;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MEDSN_Server implements Constants {
    private static short state;
    public static Chat chat;
    private static Client_Manager clientMgr;
    private static int maxClients;
    private static int chatLength;
    public static String adminPassword = "";

    //Declaring the ArrayLists that will be used for our ban lists
    public static ArrayList<String> banNameList = new ArrayList<String>();
    public static ArrayList<String> banAddressList = new ArrayList<String>();

    //main method
    public static void main(String[] args) {
        chat = new Chat();
        clientMgr = new Client_Manager();
        setState(STATE_SERVER_CLOSED);

        maxClients = 32;

        // Add shutdown hook (runs a last thread when application is shut down):
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if(state == STATE_SERVER_OPEN) MEDSN_Server.clientMgr.terminateConnections();
            }
        });

        while(state != STATE_NULL)
        {
            switch(state)
            {
                case(STATE_SERVER_CLOSED):
                {
                    break;
                }
                case(STATE_SERVER_OPENING):
                {
                    if(clientMgr.open()) setState(STATE_SERVER_OPEN);
                    else setState(STATE_SERVER_CLOSED);

                    break;
                }
                case(STATE_SERVER_OPEN):
                {
                    clientMgr.receiveConnections();
                    break;
                }
                case(STATE_SERVER_CLOSING):
                {
                    System.out.println("SYS: reached start of closing state");
                    clientMgr.close();
                    setState(STATE_SERVER_CLOSED);
                    System.out.println("SYS: reached end of closing state");
                    break;
                }

            }
        }
    } // End of main.

    //Setters and getters for the short 'state'
    public static void setState (short newState) {
        state = newState;
    }
    public static short getState () {
        return state;
    }

    //Setters and getters for the int maxClients
    public static void setMaxClients(int _MaxClients) {
        maxClients = _MaxClients;
    }
    public static int getMaxClients () {
        return maxClients;
    }

    //Setters and getters for the int chatLength
    public static void setChatLength (int _chatLength) {
        chatLength = _chatLength;
    }
    public static int getChatLength () {
        return chatLength;
    }

    //This method will handle the strings and chat commands.
    public static void handleChatString (String chatStr) {
        boolean sendToClients = true;

        if(chatStr.startsWith("/"))
        {
            String[] param = chatStr.split("\\s+");

            switch(param[0])
            {
                case("/open"):
                {
                    sendToClients = false;
                    if(state == STATE_SERVER_CLOSED)
                    {
                        if (!adminPassword.equals(""))
                        {
                            chat.writeChat("Opening the server...");
                            setState(STATE_SERVER_OPENING);
                        } else
                            chat.writeChat("You cannot open the server without an admin password. Please set an admin password with /setadminpass <adminpass>");
                    }
                    else chat.writeChat("You cannot open the server because it is not closed.");
                    break;
                }
                case("/close"):
                {
                    sendToClients = false;
                    if(state == STATE_SERVER_OPEN)
                    {
                        clientMgr.terminateConnections();
                        chat.writeChat("Closing the server...");
                        setState(STATE_SERVER_CLOSING);
                        System.out.println("SYS: Reached end of /close command case");
                    }
                    else chat.writeChat("You cannot close the server because it is not open.");
                    break;
                }
                case("/state"):
                {
                    sendToClients = false;
                    if(state == STATE_SERVER_OPEN) chat.writeChat("SERVER IS OPEN");
                    if(state == STATE_SERVER_CLOSED) chat.writeChat("SERVER IS CLOSED");
                    if(state == STATE_SERVER_CLOSING) chat.writeChat("SERVER IS CLOSING");
                    if(state == STATE_SERVER_OPENING) chat.writeChat("SERVER IS OPENING");
                    break;
                }
                case("/help"):
                {
                    sendToClients = false;
                    chat.writeChat("Here's all the commands:\n" +
                            "/help - Display this list of commands.\n" +
                            "/quit or /exit - Quits the program.\n" +
                            "/open - Open up the server for clients to establish connections.\n" +
                            "/close - Close the server and terminate all connections.\n" +
                            "/setadminpass <adminpass> - Change the password for admin privileges.");
                    break;
                }
                case("/setadminpass"):
                {
                    sendToClients = false;
                    if(param.length > 1)
                    {
                        adminPassword = param[1];
                        chat.writeChat("The admin password has been set.");
                    }
                    break;
                }
                case("/quit"):
                {
                    sendToClients = false;
                    clientMgr.terminateConnections();
                    setState(STATE_NULL);
                    break;
                }
                case("/exit"):
                {
                    sendToClients = false;
                    clientMgr.terminateConnections();
                    setState(STATE_NULL);
                    break;
                }
            }
        }

        if(sendToClients && state == STATE_SERVER_OPEN)
        {
            handleServerString(chatStr, null);
        }
    }

    public static void handleServerString (String chatStr, Client client) {
        // Get privileges for this Client
        boolean isAdmin = false;
        if(client == null || client.isAdmin())
        {
            isAdmin = true;
        }

        // Get the username of the calling Client:
        String name = "SERVER";
        if(client != null) name = client.username;

        boolean broadcastToAll = false;
        // Check if string is command:
        if(chatStr.startsWith("/"))
        {
            String[] param = chatStr.split("\\s+");

            switch(param[0])
            {
                case("/serverhelp"): // List server-side commands:
                {
                    StringBuilder list = new StringBuilder();
                    list.append("Here's all the server commands:\n" +
                                "/serverhelp - Display this list of server commands.\n" +
                                "/listclients - Get a list of all the currently connected clients.\n" +
                                "/say <expression> - Express a feeling or state of being.");
                    if(isAdmin)
                    {
                        list.append("\n" + "Admin Commands:\n" +
                                    "/kickname <username> - Kick a player by their username.\n" +
                                    "/kickip <ip> - Kick a player by their IP address.\n" +
                                    "/banname <username> - Ban a player by their username.\n" +
                                    "/banip <ip> - Ban a player by their IP address.\n" +
                                    "/pardonname <username> - Pardon (unban) a player by their username.\n" +
                                    "/pardonip <ip> - Pardon (unban) a player by their IP address.\n" +
                                    "/clientip <username> - Retrieve the IP address of a client by their username.");
                    }
                    outputLocal(client, list.toString(),
                            "Server: " + name + " used /serverhelp.");
                    break;
                }
                case("/listclients"): // List all client names on the server:
                {
                    StringBuilder list = new StringBuilder();
                    for(Client c : clientMgr.clientList)
                    {
                        list.append("\n").append(c.username);
                    }
                    list.deleteCharAt(0); // Delete the extra new-line at the start.
                    outputLocal(client, list.toString(), "Server: " + name + " used /listclients.");
                    break;
                }
                case("/say"): // Express a feeling or state of being:
                {
                    if(param.length > 1)
                    {
                        String msg = chatStr.split("\\s+", 2)[1];
                        outputGlobal(name + " " + msg);
                    }
                    else // Not enough parameters:
                    {
                        outputLocal(client,
                                "No valid username was given. /say <expression-goes-here>",
                                "Server: " + name + " used /say unsuccessfully.");
                    }
                    break;
                }
                case("/clientip"):
                {
                    if(isAdmin)
                    {
                        if(param.length > 1)
                        {
                            // Get the IP address of the Client by the given username.
                            String cName = param[1];
                            Client c = clientMgr.findClientWithName(cName);
                            if(c != null)
                            {
                                String ip = c.address;
                                outputLocal(client,
                                        "IP: " + ip,
                                        "Server: " + name + " used /clientip on " + cName + " successfully.");
                            }
                            else // No Client by that username was found:
                            {
                                String msg = "No Client by the username " + name + " was found.";
                                outputLocal(client,
                                        "No Client by the username " + name + " was found.",
                                        "Server: " + name + " used /clientip on " + cName + " (not found).");
                            }
                        }
                        else // Not enough parameters:
                        {
                            // Broadcast error message back to caller:
                            outputLocal(client,
                                    "No valid username was given. /clientip <username>",
                                    "Server: " + name + " used /clientip unsuccessfully.");
                        }
                    }
                    break;
                }
                case("/banname"):
                {
                    if(isAdmin)
                    {

                    }
                    break;
                }
                case("/banip"):
                {
                    if(isAdmin)
                    {

                    }
                    break;
                }
                case("/kickname"):
                {
                    if(isAdmin)
                    {

                    }
                    break;
                }
                case("/kickip"):
                {
                    if(isAdmin)
                    {

                    }
                    break;
                }
                case("/pardonname"):
                {
                    if(isAdmin)
                    {

                    }
                    break;
                }
                case("/pardonip"):
                {
                    if(isAdmin)
                    {

                    }
                    break;
                }
                default:
                {

                    break;
                }
            }
        }
        else // Not a command - chat message string:
        {
            outputGlobal(name + ": " + chatStr);
        }
    }

    // Sends a string message either to a specific client (with a server log) or directly to the server chat.
    public static void outputLocal(Client client, String msg, String log)
    {
        if(client == null) chat.writeChat(msg);
        else
        {
            chat.writeChat(log);
            clientMgr.broadcast(msg, client);
        }
    }
    // Sends a string message to all clients and the server chat.
    public static void outputGlobal(String msg)
    {
        chat.writeChat(msg);
        clientMgr.broadcast(msg);
    }
}
