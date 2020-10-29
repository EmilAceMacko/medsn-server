package src;

import java.util.Scanner;

public class Chat {
    MEDSN_Server owner;
    Thread scanThread;

    public Chat (MEDSN_Server owner) {
        scanThread = new Thread () {
            private Chat owner;
            private String input;
            public Boolean scanning;

            private Scanner scan = new Scanner(System.in);

            public void run() {

            }

            public void writeChat (String msg) {

            }

        };
    }

    public String checkInput () {
        return null;
    }

    public void writeChat (String msg) {
        System.out.println(msg);
    }










    //public <type> void Method () {}








}
