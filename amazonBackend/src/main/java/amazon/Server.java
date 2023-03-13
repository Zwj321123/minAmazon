package amazon;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements AutoCloseable{
    ServerSocket serverSocket;

    public Server() {

    }

    public Server(int portNum) throws IOException {
        serverSocket = new ServerSocket(portNum);
        serverSocket.setSoTimeout(3000);
    }

    public Socket accept(){
        try{
            return serverSocket.accept();
        }catch (IOException e){
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
    }
}
