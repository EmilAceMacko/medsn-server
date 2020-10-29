package src;

public class Chat {
    MEDSN_Server owner;
    Thread scanThread;

    public Chat (MEDSN_Server owner) {
        scanThread = new Thread () {

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
