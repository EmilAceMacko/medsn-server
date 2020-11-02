package src;

import java.util.Scanner;

public class ScanThread extends Thread {
    private Chat owner;
    private String input;
    public boolean scanning = true;

    private Scanner scan = new Scanner(System.in);

    public ScanThread (Chat owner) {
        this.owner = owner;
    }

    public void run() {
        while(scanning) {
            input = "";
            input = scan.nextLine();
            if (input != "") {
                owner.message(input);
            }
        }
    }

    public void setScanning (boolean scanning) {
        this.scanning = scanning;
    }



}
