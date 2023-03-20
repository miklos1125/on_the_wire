package on_the_wire;

import java.io.*;
import java.net.*;

abstract class Connection{
    Socket socket;
    DataInputStream incoming;
    DataOutputStream outgoing;
    
    public String getMessage() throws IOException{
        return incoming.readUTF();
    }
    
    public void sendMessage(String toSend) throws IOException{
        outgoing.writeUTF(toSend);
        outgoing.flush();
    }
    
    //???!!!
    public boolean isConnected(){
        return socket.isBound();
    }
    
    public void closeConnection() throws IOException{
        incoming.close();
        outgoing.close();
        socket.close();
    }
    
}

class ServerConnection extends Connection{

    ServerSocket serverSocket;

    ServerConnection() throws IOException{
        serverSocket = new ServerSocket(2008);
        //serverSocket.setSoTimeout(5000);
        socket = serverSocket.accept();
        incoming = new DataInputStream(socket.getInputStream());
        outgoing = new DataOutputStream(socket.getOutputStream());
    }
    
    @Override
    public void closeConnection() throws IOException {
        
        super.closeConnection();
        serverSocket.close();
        socket = null;
    }

    @Override //???!!!
    public boolean isConnected(){
        return serverSocket.isBound();
    }
}

class ClientConnection extends Connection{

    ClientConnection(String hostAddress) throws IOException{
        if (hostAddress.equals("")) hostAddress = "localhost";
        socket = new Socket(hostAddress, 2008);
        incoming = new DataInputStream(socket.getInputStream());
        outgoing = new DataOutputStream(socket.getOutputStream());
    }
}