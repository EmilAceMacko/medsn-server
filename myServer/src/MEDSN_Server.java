//Valdemar
package src;
import java.util.ArrayList;

public class MEDSN_Server implements Constants {
    private short state;
    private Chat chat;
    private Client_Manager clientMgr;
    private int maxClients;
    private int chatLength;
    public String serverPassword;

    //Declaring the ArrayLists that will be used for our ban lists
    public ArrayList<String> banNameList = new ArrayList<String>();
    public  ArrayList<String> banAddressList = new ArrayList<String>();

    //main method
    public static void main(String[] args) {}

    //Setters and getters for the short 'state'
    public void setState (short newState) {
        this.state = newState;
    }
    public short getState () {
        return state;
    }

    //Setters and getters for the int maxClients
    public void setMaxClients(int _MaxClients) {
        this.maxClients = _MaxClients;
    }
    public int getMaxClients () {
        return maxClients;
    }

    //Setters and getters for the int chatLength
    public void setChatLength (int _chatLength) {
        this.chatLength = _chatLength;
    }
    public int getChatLength () {
        return chatLength;
    }

    //This method will handle the strings and chat commands.
    public void handleChatString (String chat) {
    }

    public void handleServerString (String chat, Client user) {
    }



}
