//Valdemar
package src;

public class MEDSN_Server implements Constants {
    private short state;
    private Chat chat;
    private Client_Manager clientMgr;

    public static void main(String[] args) {}

    public void setState (short newState) {
        this.state = newState;
    }

    public short getState () {
        return state;
    }
}
