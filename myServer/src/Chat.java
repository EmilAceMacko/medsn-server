package src;

import java.util.Scanner;

public class Chat {
    MEDSN_Server owner;
    Thread scanThread;

    public Chat (MEDSN_Server owner) {

    }

    public void message (String msg) {
        owner.handleChatString(msg);
    }

    public String checkInput () {
        return null;
    }

    public void writeChat (String msg) {
        System.out.println(msg);
    }


}
