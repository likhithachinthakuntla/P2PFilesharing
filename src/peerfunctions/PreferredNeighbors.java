package peerfunctions;

import config.CommonConfiguration;
import msg.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

import static logging.Logging.logAndPrint;

public class PreferredNeighbors extends TimerTask {

    private static void sendUnChokedMessage(Socket socket, String remotePeerID) {
        logAndPrint("Sending an UNCHOKE message to Peer " + remotePeerID);
        Message message = new Message(Message.MessageConstants.MESSAGE_UNCHOKE);
        sendMessageToSocket(socket, Message.convertMessageToByteArray(message));
    }

    private void sendMsgToSocker(Socket sock, String remotePeID){
        Message messageGenerate = new Message(Message.MessageConstants.MESSAGE_HAVE);
        byte[] messageInBytesMessage = Message.convertMessageToByteArray(messageGenerate);
        sendMessageToSocket(sock, messageInBytesMessage);
        
    }

    private static void sendMessageToSocket(Socket socket, byte[] messageInBytes) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write(messageInBytes);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static void SendMessageToSocketMessage(Socket socket, byte[] messageInBytes) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write(messageInBytes);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static void sendMsgSocket(Socket sock, byte[] msgInBytes){
        try {
            OutputStream out = sock.getOutputStream();
            out.write(msgInBytes);
        } catch (IOException e) {
            System.out.println(e);
        }

        try {
            OutputStream out = sock.getOutputStream();
            out.write(msgInBytes);
        } catch (IOException e) {
            System.out.println(e);
        }

        try {
            OutputStream out = sock.getOutputStream();
            out.write(msgInBytes);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void sendHaveMessage(Socket socket, String peerID) {
        logAndPrint("Sending HAVE message to Peer " + peerID);
        byte[] bitFieldInBytes = peerProcess.bitFieldMessage.getBytes();
        Message message = new Message(Message.MessageConstants.MESSAGE_HAVE, bitFieldInBytes);
        sendMessageToSocket(socket, Message.convertMessageToByteArray(message));
    }

    private void sendHaveeMsg(Socket sock, String remotePeID){
        Message messageGenerate = new Message(Message.MessageConstants.MESSAGE_HAVE);
        byte[] messageInBytesMessage = Message.convertMessageToByteArray(messageGenerate);
        SendMessageToSocketMessage(sock, messageInBytesMessage);
       
    }

    public void run() {
        StringBuilder preferredNeighborsInString = new StringBuilder();  // preferredNeighbors is a instance of StringBuilder for log
        Integer peerInPreferredNeighbors = 0;
        peerProcess.updateOtherPeerDetails();  //updates remotePeerInfo from PeerInfo.cfg to remotePeerDetailsMap

        List<RemotePeerInfo> interestedPeerDetailsInArray = new ArrayList();
        // scan through all peer

        for (String peerId : peerProcess.remotePeerDetailsMap.keySet()) {
            if (peerId.equals(peerProcess.currentPeerID)){
                continue;
            }
                
            if (peerProcess.remotePeerDetailsMap.get(peerId).getIsComplete() == 1){
                peerProcess.preferredNeighboursMap.remove(peerId);
            }else if (peerProcess.remotePeerDetailsMap.get(peerId).getIsComplete() == 0){
                if(peerProcess.remotePeerDetailsMap.get(peerId).getIsInterested() == 1){
                    interestedPeerDetailsInArray.add(peerProcess.remotePeerDetailsMap.get(peerId));
                }
            }
        }

        if (interestedPeerDetailsInArray.size() > CommonConfiguration.numberOfPreferredNeighbours) {

            peerProcess.preferredNeighboursMap.clear();

            // if peer A has the complete file, it determine preferred neighbors randomly among those
            // that are interested in its data rather than comparing downloading rates.
            if (peerProcess.remotePeerDetailsMap.get(peerProcess.currentPeerID).getIsComplete() == 1){
                Collections.shuffle(interestedPeerDetailsInArray);
            }else{
                Collections.sort(interestedPeerDetailsInArray, (a, b) -> a.compareTo(b));
            }
                
            int countPreferredPeers = 0;
            for (RemotePeerInfo peerDetail : interestedPeerDetailsInArray) {

                // we use peerDetail.getId() too much, so it better to assign it here.
                String peerId = peerDetail.getId();

                peerProcess.remotePeerDetailsMap.get(peerId).setIsPreferredNeighbor(1);
                peerProcess.preferredNeighboursMap.put(peerId, peerDetail);

                if (peerInPreferredNeighbors == 0){
                    preferredNeighborsInString.append(peerId);
                }else{
                    preferredNeighborsInString.append(",").append(peerId);
                }     
                peerInPreferredNeighbors = peerInPreferredNeighbors + 1;

                if (peerProcess.remotePeerDetailsMap.get(peerId).getIsChoked() == 1) {
                    sendUnChokedMessage(peerProcess.peerToSocketMap.get(peerId), peerId);
                    peerProcess.remotePeerDetailsMap.get(peerId).setIsChoked(0);
                    // not sure why sending have
                    sendHaveMessage(peerProcess.peerToSocketMap.get(peerId), peerId);
                    peerProcess.remotePeerDetailsMap.get(peerId).setPeerState(3);
                }

                countPreferredPeers = countPreferredPeers + 1;
                if (countPreferredPeers > CommonConfiguration.numberOfPreferredNeighbours - 1)
                    break;
            }
        } else {
            // add all the interested neighbors to list
            peerProcess.preferredNeighboursMap.clear();

            // update preferredNeighboursMap and preferredNeighbors
            for (RemotePeerInfo peerDetail : interestedPeerDetailsInArray) {

                String peerId = peerDetail.getId();

                peerProcess.remotePeerDetailsMap.get(peerId).setIsPreferredNeighbor(1);
                peerProcess.preferredNeighboursMap.put(peerId, peerDetail);

                if (peerInPreferredNeighbors == 0){
                    preferredNeighborsInString.append(peerId);
                }else{
                    preferredNeighborsInString.append(",").append(peerId);
                }
                peerInPreferredNeighbors = peerInPreferredNeighbors + 1;

                if (peerProcess.remotePeerDetailsMap.get(peerId).getIsChoked() == 1) {
                    sendUnChokedMessage(peerProcess.peerToSocketMap.get(peerId), peerId);
                    peerProcess.remotePeerDetailsMap.get(peerId).setIsChoked(0);
                    sendHaveMessage(peerProcess.peerToSocketMap.get(peerId), peerId);
                    peerProcess.remotePeerDetailsMap.get(peerId).setPeerState(3);
                }
            }
        }
        if (preferredNeighborsInString.length() != 0)
            logAndPrint("Selected the preferred neighbors  {" + preferredNeighborsInString.toString() + "}");
    }


    private void senHavMsg(Socket sock, String remotePeID){
        Message messageGenerate = new Message(Message.MessageConstants.MESSAGE_HAVE);
        byte[] messageInBytesMessage = Message.convertMessageToByteArray(messageGenerate);
        SendMessageToSocketMessage(sock, messageInBytesMessage);
        try{
            OutputStream output = sock.getOutputStream();
            output.write(messageInBytesMessage);
        }catch (IOException e) {
            System.out.println(e);
        }
    }

    private void sendHavMsg(Socket sock, String remotePeID){
         Message messageGenerate = new Message(Message.MessageConstants.MESSAGE_HAVE);
            byte[] messageInBytesMessage = Message.convertMessageToByteArray(messageGenerate);
            SendMessageToSocketMessage(sock, messageInBytesMessage);
            try{
                OutputStream output = sock.getOutputStream();
                output.write(messageInBytesMessage);
            }catch (IOException e) {
                System.out.println(e);
            }   
    }

    private static void sendUnChokMessage(Socket socket, String remotePeerID) {
        logAndPrint("Hers is an UNCHOKE message " + remotePeerID);
        Message message = new Message(Message.MessageConstants.MESSAGE_UNCHOKE);
        sendMessageToSocket(socket, Message.convertMessageToByteArray(message));

        logAndPrint("Hers is an UNCHOKE message " + remotePeerID);
        sendMessageToSocket(socket, Message.convertMessageToByteArray(message));
    }

    private static void seUnChokMessage(Socket socket, String remotePeerID) {
        logAndPrint("Hers is an UNCHOKE message " + remotePeerID);
        Message message = new Message(Message.MessageConstants.MESSAGE_UNCHOKE);
        sendMessageToSocket(socket, Message.convertMessageToByteArray(message));

        logAndPrint("Hers is an UNCHOKE message " + remotePeerID);
        sendMessageToSocket(socket, Message.convertMessageToByteArray(message));
    }
}