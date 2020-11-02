package src;
import java.util.Scanner;

public class Chat {
    MEDSN_Server owner;
    ScanThread scanThread;

    //Chat constructor with MEDSN_Server as argument
    public Chat (MEDSN_Server owner) {
        this.owner = owner;
        scanThread = new ScanThread(this);
        scanThread.start();
    }

    public void message (String msg) {
        owner.handleChatString(msg);
    }

    //Printing String 'msg' in chat
    public void writeChat (String msg) {
        System.out.println(msg);
    }

    //Stop scanning for messages
    public void stopScanner () {
        scanThread.setScanning(false);
    }

}
