//Valdemar
package src;
import java.util.ArrayList;

public class MEDSN_Server implements Constants {
    private static short state;
    private static Chat chat;
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
                    setState(STATE_SERVER_OPEN);
                    break;
                }
                case(STATE_SERVER_OPEN):
                {
                    clientMgr.receiveConnections();
                    break;
                }
                case(STATE_SERVER_CLOSING):
                {
                    setState(STATE_SERVER_CLOSED);
                    break;
                }

            }
        }
    }

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
                    if(adminPassword != "")
                    {
                        chat.writeChat("Opening the server...");
                        setState(STATE_SERVER_OPENING);
                    }
                    else chat.writeChat("You cannot open the server without an admin password. Please set an admin password with /setadminpass <adminpass>");
                }
                case("/close"):
                {
                    sendToClients = false;
                    if(state == STATE_SERVER_OPEN)
                    {
                        clientMgr.terminateConnections();
                        chat.writeChat("Closing the server...");
                        setState(STATE_SERVER_CLOSING);
                    }
                    else chat.writeChat("You cannot close the server because it is not open.");
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
                }
                case("/setadminpass"):
                {
                    sendToClients = false;
                    if(param.length > 1)
                    {
                        adminPassword = param[1];
                        chat.writeChat("The admin password has been set.");
                    }
                }
                case("/quit"):
                case("/exit"):
                {
                    sendToClients = false;
                    clientMgr.terminateConnections();
                    setState(STATE_NULL);
                    break;
                }
            }

            if(sendToClients && state == STATE_SERVER_OPEN)
            {
                handleServerString(chatStr, null);
            }
        }
    }

    public static void handleServerString (String chatStr, Client client) {
        // Get privileges for this client
        boolean isAdmin = false;
        if(client == null || client.isAdmin())
        {
            isAdmin = true;
        }

        boolean broadcastToAll = false;
        // Check if string is command:
        if(chatStr.startsWith("/"))
        {
            String[] param = chatStr.split("\\s+");

            switch(param[0])
            {
                case("/serverhelp"): // List server-side commands:
                {

                    break;
                }
                case("/listclients"): // List all client names on the server:
                {

                    break;
                }
                case("/say"):
                {
                    break;
                }
                case("/clientip"):
                {
                    if(isAdmin)
                    {
                        if(param.length > 1)
                        {

                        }
                        else
                        {
                            String err = "";
                            if(client == null) chat.writeChat(err);
                            else clientMgr.broadcast(err, client);
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


            }
        }
    }

    // Sends a string message to either a specific client or the server chat.
    public static void outputLocal(String msg, Client client)
    {
        if(client == null) chat.writeChat(msg);
        else clientMgr.broadcast(msg, client);
    }
    // Sends a string message to both
    public static void outputGlobal(String msg)
    {
        chat.writeChat(msg);
        clientMgr.broadcast(msg);
    }
}
