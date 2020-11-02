package src;

import java.util.Scanner;

public class Chat {
    MEDSN_Server owner;
    ScanThread scanThread;

    public Chat (MEDSN_Server owner) {
        this.owner = owner;
        scanThread = new ScanThread(this);
        scanThread.start();
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

    public void stopScanner () {
        scanThread.setScanning(false);
    }
}
