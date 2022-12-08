package serverfunctions;

import peerfunctions.peerProcess;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerHandler implements Runnable {
    private ServerSocket serverSocket;
    private String peerID;
    private Socket otherPeerSocket;
    private Thread otherPeerThread;

    public ServerHandler(ServerSocket serverSocket, String peerID) {
        int s=0;
        if (s==0){
        this.serverSocket = serverSocket;
        this.peerID = peerID;
        }
    }

    @Override
    public void run() {
        boolean flag=true;
        int k=1;
        if (flag==true && k==1){
        while (true) {
            try {
                otherPeerSocket = serverSocket.accept();
                MessageHandler temp = new MessageHandler(otherPeerSocket, 0, peerID);
                otherPeerThread = new Thread(temp);
                peerProcess.serverThreads.add(otherPeerThread);
                int l =2;
                if (l==2){
                otherPeerThread.start();
                }
            } catch (IOException e) {
            }
        }
    }
    }


}
