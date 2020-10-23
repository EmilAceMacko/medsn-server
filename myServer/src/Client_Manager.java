package src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;

class Client_Manager
{
    private MEDSN_Server owner;
    private int port = 8000;
    private DataInputStream in;
    private DataOutputStream out;
    private ServerSocket serverSocket;
    private Client[] clientList;

    Client_Manager(MEDSN_Server _owner)
    {
        owner = _owner;
    }
}