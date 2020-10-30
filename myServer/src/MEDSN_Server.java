//Valdemar
package src;
import java.util.ArrayList;

public class MEDSN_Server implements Constants {
    private short state;
    private Chat chat;
    private Client_Manager clientMgr;
    private int maxClients;
    private int chatLength;

    public ArrayList<String> banNameList = new ArrayList<String>();
    public  ArrayList<String> banAdressList = new ArrayList<String>();


    public static void main(String[] args) {}

    public void setState (short newState) {
        this.state = newState;
    }

    public short getState () {
        return state;
    }

    public void setMaxClients(int _MaxClients) {
        this.maxClients = _MaxClients;
    }

    public int getMaxClients () {
        return maxClients;
    }

    public void setChatLength (int _chatLength) {
        this.chatLength = _chatLength;
    }

    public int getChatLength () {
        return chatLength;
    }

    public void handleChatString (String chat) {

    }
}
