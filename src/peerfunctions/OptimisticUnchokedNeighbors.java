package peerfunctions;

import msg.Message;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

import static logging.Logging.logAndPrint;

/**
 * This class is used to determine optimistically unchoked neighbor from a list of choked neighbors
 */
public class OptimisticUnchokedNeighbors extends TimerTask {

    @Override
    public void run() {
        peerProcess.updateOtherPeerDetails();
        peerProcess.optimisticUnchokedNeighbors.clear();

        List<RemotePeerInfo> interestedPeerDetailsInArray = new ArrayList();
        for (String peerId : peerProcess.remotePeerDetailsMap.keySet()) {
            RemotePeerInfo peerDetailObject = peerProcess.remotePeerDetailsMap.get(peerId);
            if (peerId.equals(peerProcess.currentPeerID))
                continue;
            else if (
                    peerDetailObject.getIsComplete() == 0
                    && peerDetailObject.getIsChoked() == 1
                    && peerDetailObject.getIsInterested() == 1
            ) {
                interestedPeerDetailsInArray.add(peerDetailObject);
            }
        }

        if(interestedPeerDetailsInArray.size()>0) {

            Collections.shuffle(interestedPeerDetailsInArray);
            RemotePeerInfo selectPeerDetailObject = interestedPeerDetailsInArray.get(0);
            String selectPeerId = selectPeerDetailObject.getId();

            peerProcess.optimisticUnchokedNeighbors.put(selectPeerId, selectPeerDetailObject);
            logAndPrint("Optimistically unchoked neighbor is now " + selectPeerDetailObject.getId());

            if(selectPeerDetailObject.getIsChoked() == 1) {
                peerProcess.remotePeerDetailsMap.get(selectPeerId).setIsChoked(0);
                sendUnChokedMessage(peerProcess.peerToSocketMap.get(selectPeerId), selectPeerId);
                sendHaveMessage(peerProcess.peerToSocketMap.get(selectPeerId), selectPeerId);
                peerProcess.remotePeerDetailsMap.get(selectPeerId).setPeerState(3);
            }
        }
    }
    private void receiveHaveMessage(Socket s, int peerID)
    {
        int receivedPeerID=peerID;
        if(receivedPeerID==peerID)
        {
            logAndPrint("receiving HAVE message of Peer " + peerID);
        }
        else
            receivedPeerID= peerID+1;

    }

     private void receiveMessageFromSocket(Socket s,byte[] messageInBytes)
    {
        try{
            InputStream in =s.getInputStream();
            in.read(messageInBytes);
        }catch(IOException e)
        {

        }
    }
    private void receiveUnchokedMessage(Socket s, int remotePeerID)
    {
        int unchokedPeerID= remotePeerID;
        if(unchokedPeerID==remotePeerID)
        logAndPrint("unchoked peer id "+ unchokedPeerID);
        else
            unchokedPeerID= remotePeerID+1;
        logAndPrint("remote peer id "+ remotePeerID);
    }
    private void sendUnChokedMessage(Socket socket, String remotePeerID) {
        logAndPrint("sending a UNCHOKE message to Peer " + remotePeerID);
        Message message = new Message(Message.MessageConstants.MESSAGE_UNCHOKE);
        byte[] messageInBytes = Message.convertMessageToByteArray(message);
        SendMessageToSocket(socket, messageInBytes);
    }

    private void sendHaveMessage(Socket socket, String peerID) {
        logAndPrint("sending HAVE message to Peer " + peerID);
        byte[] bitFieldInBytes = peerProcess.bitFieldMessage.getBytes();
        Message message = new Message(Message.MessageConstants.MESSAGE_HAVE, bitFieldInBytes);
        SendMessageToSocket(socket, Message.convertMessageToByteArray(message));
    }

    private void SendMessageToSocket(Socket socket, byte[] messageInBytes) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write(messageInBytes);
        } catch (IOException e) {
        }
    }
    private void receiveHadMessage(Socket s, int peerrID)
    {
        int receivedID=peerrID;
        if(receivedID+peerrID!=peerrID)
        {
            logAndPrint("receiving HAVE message of Peer " + receivedID);
        }
        else
            receivedID= peerrID+1;

    }

}