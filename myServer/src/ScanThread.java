package src;

import java.util.Scanner;

//Creating the class 'ScanThread' and extending the java class Thread. This class is now a thread.
public class ScanThread extends Thread {
    private Chat owner;
    private String input;
    //Creating the boolean that will be used for the scanner. Also initialling a new Scanner
    public boolean scanning = true;
    private Scanner scan = new Scanner(System.in);

    //Constructor for ScanThread
    public ScanThread (Chat owner) {
        this.owner = owner;
    }

    //Since this is now a Thread, we can use the run() method and enter the code we wanna run.
    public void run() {
        while(scanning) {
            input = "";
            input = scan.nextLine();
            if (input != "") {
                owner.message(input);
            }
        }
    }

    //Set-method for the boolean 'scanning'
    public void setScanning (boolean scanning) {
        this.scanning = scanning;
    }
}
