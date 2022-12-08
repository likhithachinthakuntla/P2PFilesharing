package server;

import config.CommonConfiguration;
import message.*;
import peer.RemotePeerInfo;
import peer.peerProcess;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Date;
import java.util.Set;

import static logging.Logging.logAndPrint;
import static peer.peerProcess.messageQueue;

public class MessageProcessingHandler implements Runnable {


    private static String currentPeerID;

    private RandomAccessFile randomAccessFile;

    public MessageProcessingHandler(String peerID) {
        boolean fl=false;
        boolean hp=true;
        if(fl == false && hp==true){
            currentPeerID = peerID;
            hp=false;
        }
    }

    private void sendDownloadCompleteMessage(Socket socket, String peerID) {
        boolean downloadFlag=true;
        boolean logFlag=true;

        if(downloadFlag==true){
            logAndPrint("Sending DOWNLOAD COMPLETE MESSAGE to the Peer ID " + peerID);
            logFlag=true;
        }

        if(logFlag==true){
            sendMessageToSocket(socket, Message.convertMessageToByteArray(new Message(Message.MessageConstants.MESSAGE_DOWNLOADED)));
        }

    }

    private void sendHaveMessage(Socket socket, String peerID) {
        boolean haveFlag=true;
        boolean logFlag=true;
        if(haveFlag==true){
            logAndPrint(peer.peerProcess.currentPeerID + " is sending HAVE MESSAGE to the Peer ID " + peerID);
            logFlag=true;
        }
        
        if(logFlag==true){
            sendMessageToSocket(socket, Message.convertMessageToByteArray(new Message(Message.MessageConstants.MESSAGE_HAVE, peerProcess.bitFieldMessage.getBytes())));
        }
    }

    private boolean hasPeerInterested(RemotePeerInfo remotePeerInfo) {
        boolean a= remotePeerInfo.getIsComplete() == 0;
        boolean b= remotePeerInfo.getIsChoked() == 0;
        boolean c= remotePeerInfo.getIsInterested() == 1;
        return a && b && c;
    }


    private int getFirstDifferentPieceIndex(String peerID) {

        BitField tempp = peerProcess.remotePeerDetailsMap.get(peerID).getBitField();
        return peerProcess.bitFieldMessage.getFirstDifferentPieceIndex(tempp);
    }

    private void sendRequestMessage(Socket socket, int pieceIndex, String remotePeerID) {
        boolean sendReqFlag=true;
        boolean logFlag=true;

        if(sendReqFlag==true){
            logAndPrint(peerProcess.currentPeerID + " is sending REQUEST MESSAGE to Peer ID " + remotePeerID + " for the piece number  " + pieceIndex);
            logFlag=true;
        }

        if(logFlag==true){
            byte[] pieceInBytes = new byte[Message.MessageConstants.PIECE_INDEX_LENGTH];
            byte[] pieceIndexInBytes = peerProcess.PeerProcessUtils.convertIntToByteArray(pieceIndex);
            System.arraycopy(pieceIndexInBytes, 0, pieceInBytes, 0, pieceIndexInBytes.length);
            sendMessageToSocket(socket, Message.convertMessageToByteArray(new Message(Message.MessageConstants.MESSAGE_REQUEST, pieceIndexInBytes)));
        }

    }


    private void sendFilePiece(Socket socket, Message message, String remotePeerID) {
        boolean sendFileFlag=true;
        boolean logFlag=true;

        if(sendFileFlag==true){
            byte[] pieceIndexInBytes = message.getPayload();
            int pieceIndex = peerProcess.PeerProcessUtils.convertByteArrayToInt(pieceIndexInBytes);
            logAndPrint("Sending a PIECE MESSAGE for the piece " + pieceIndex + " to Peer " + remotePeerID);
            logFlag=true;
        }

        if(logFlag==true){
        byte[] bytesRead = new byte[CommonConfiguration.pieceSize];
        int numberOfBytesRead;
        int pieceIndex = peerProcess.PeerProcessUtils.convertByteArrayToInt(message.getPayload());
        File file = new File(currentPeerID, CommonConfiguration.fileName);
        int pieceSize=CommonConfiguration.pieceSize;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(pieceIndex * pieceSize);
            numberOfBytesRead = randomAccessFile.read(bytesRead, 0, pieceSize);

            byte[] buffer = new byte[numberOfBytesRead + Message.MessageConstants.PIECE_INDEX_LENGTH];
            System.arraycopy(message.getPayload(), 0, buffer, 0, Message.MessageConstants.PIECE_INDEX_LENGTH);
            System.arraycopy(bytesRead, 0, buffer, Message.MessageConstants.PIECE_INDEX_LENGTH, numberOfBytesRead);
            sendMessageToSocket(socket, Message.convertMessageToByteArray(new Message(Message.MessageConstants.MESSAGE_PIECE, buffer)));
            randomAccessFile.close();

        } catch (IOException e) {
            System.out.println(e);
        }
    }
    }

    private boolean isNotPreferredAndUnchokedNeighbour(String remotePeerId) {
        String rid=remotePeerId;
        boolean e=peerProcess.preferredNeighboursMap.containsKey(rid);
        boolean f=!peerProcess.optimisticUnchokedNeighbors.containsKey(rid);
        return !e && f;
    }

    private void sendChokedMessage(Socket socket, String remotePeerID) {
        boolean chokeFlag=true;
        boolean logFlag=true;
        if(chokeFlag==true){
            logAndPrint("Is sending a CHOKE MESSAGE to Peer Id" + remotePeerID);
            logFlag=true;
        }

        if(logFlag==true){
            sendMessageToSocket(socket, Message.convertMessageToByteArray(new Message(Message.MessageConstants.MESSAGE_CHOKE)));
        }
    }

    private void sendUnChokedMessage(Socket socket, String remotePeerID) {
        boolean unchokeFlag=true;
        boolean logFlag=true;
        if(unchokeFlag==true){
            logAndPrint("sending a UNCHOKE MESSAGE to the Peer Id " + remotePeerID);
            logFlag=true;
        }

        if(logFlag==true){
            sendMessageToSocket(socket, Message.convertMessageToByteArray(new Message(Message.MessageConstants.MESSAGE_UNCHOKE)));
        }
    }

    private void sendNotInterestedMessage(Socket socket, String remotePeerID) {
        boolean notInterestFlag=true;
        boolean logFlag=true;
        if(notInterestFlag==true){
        logAndPrint("sending a NOT INTERESTED MESSAGE to the Peer Id " + remotePeerID);
        logFlag=true;
        }

        if(logFlag==true){
            sendMessageToSocket(socket, Message.convertMessageToByteArray(new Message(Message.MessageConstants.MESSAGE_NOT_INTERESTED)));
        }
    }

    private void sendInterestedMessage(Socket socket, String remotePeerID) {
        boolean interestFlag=true;
        boolean logFlag=true;
        if(interestFlag==true){
            logAndPrint("Is sending an INTERESTED MESSAGE to the Peer Id" + remotePeerID);
            logFlag=true;
        }

        if(logFlag==true){
            sendMessageToSocket(socket, Message.convertMessageToByteArray(new Message(Message.MessageConstants.MESSAGE_INTERESTED)));
        }
    }

    private void sendBitField(Socket socket, String remotePeerID) {
        boolean bitFieldFlag=true;
        boolean logFlag=true;
        if(bitFieldFlag==true){
            logAndPrint("Is sending a BITFIELD message to the Peer Id " + remotePeerID);
            logFlag=true;
        }
        
        if(logFlag==true){
        Message message = new Message(Message.MessageConstants.MESSAGE_BITFIELD, peerProcess.bitFieldMessage.getBytes());
        sendMessageToSocket(socket, Message.convertMessageToByteArray(message));
        }

    }

    private boolean isPeerInterested(Message message, String remotePeerID) {
        boolean peerInterested = false;
        peerProcess.remotePeerDetailsMap.get(remotePeerID).setBitField(BitField.decodeMessage(message.getPayload()));
        int pieceIndex = peerProcess.bitFieldMessage.getInterestingPieceIndex(BitField.decodeMessage(message.getPayload()));
        boolean pieceFlag=true;
        int field=1;
        if (pieceIndex != -1 && pieceFlag==true) {
            if (message.getType().equals(Message.MessageConstants.MESSAGE_HAVE))
                logAndPrint("did received HAVE MESSAGE from the Peer Id" + remotePeerID + " for piece number " + pieceIndex);
            if(field==1){
                peerInterested = true;
            }
        }

        return peerInterested;
    }

    private void sendMessageToSocket(Socket socket, byte[] messageInBytes) {
        boolean sendFlag=true;
        if(sendFlag==true){
        try {
            OutputStream out = socket.getOutputStream();
            byte[] message=messageInBytes;
            out.write(message);
        } catch (IOException e) {
        }
    }
    else{
        sendFlag=true;
    }
    }

    @Override
    public void run() {
        MessageInfo messageInfo;
        Message message;
        String messageType;
        String remotePeerID;

        while (true) {
            //Read message from queue
            messageInfo = messageQueue.poll();
            while (messageInfo == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                messageInfo = messageQueue.poll();
            }
            message = messageInfo.getMessage();
            messageType = message.getType();
            remotePeerID = messageInfo.getFromPeerID();
            int peerState = peerProcess.remotePeerDetailsMap.get(remotePeerID).getPeerState();

            if (messageType.equals(Message.MessageConstants.MESSAGE_HAVE) && peerState != 14) {

                if (isPeerInterested(message, remotePeerID)) {
                    logAndPrint("sending INTERESTED message to Peer " + remotePeerID);
                    sendInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                    peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(9);
                } else {
                    logAndPrint("sending NOT INTERESTED message to Peer " + remotePeerID);
                    sendNotInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                    peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                }
            } else {
                if (peerState == 2) {
                    if (messageType.equals(Message.MessageConstants.MESSAGE_BITFIELD)) {

                        logAndPrint("received a BITFIELD message from Peer " + remotePeerID);
                        sendBitField(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(3);
                    }
                } else if (peerState == 3) {
                    if (messageType.equals(Message.MessageConstants.MESSAGE_INTERESTED)) {

                        logAndPrint("receieved an INTERESTED message from Peer " + remotePeerID);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsInterested(1);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsHandShaked(1);

                        if (isNotPreferredAndUnchokedNeighbour(remotePeerID)) {
                            sendChokedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsChoked(1);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(6);
                        } else {
                            sendUnChokedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsChoked(0);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(4);
                        }
                    } else if (messageType.equals(Message.MessageConstants.MESSAGE_NOT_INTERESTED)) {
                        //Received not interested message
                        logAndPrint("receieved a NOT INTERESTED message from Peer " + remotePeerID);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsInterested(0);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsHandShaked(1);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(5);
                    }
                } else if (peerState == 4) {
                    if (messageType.equals(Message.MessageConstants.MESSAGE_REQUEST)) {
                        sendFilePiece(peerProcess.peerToSocketMap.get(remotePeerID), message, remotePeerID);

                        Set<String> remotePeerDetailsKeys = peerProcess.remotePeerDetailsMap.keySet();
                        if (!peerProcess.isFirstPeer && peerProcess.bitFieldMessage.isFileDownloadComplete()) {
                            for (String key : remotePeerDetailsKeys) {
                                if (!key.equals(peerProcess.currentPeerID)) {
                                    Socket socket = peerProcess.peerToSocketMap.get(key);
                                    if (socket != null) {
                                        sendDownloadCompleteMessage(socket, key);
                                    }
                                }
                            }
                        }
                        if (isNotPreferredAndUnchokedNeighbour(remotePeerID)) {
                            //sending choked message if the neighbor is not in unchoked neighbors or optimistically unchoked neighbors list
                            sendChokedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsChoked(1);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(6);
                        }
                    }
                } else if (peerState == 8) {
                    if (messageType.equals(Message.MessageConstants.MESSAGE_BITFIELD)) {
                        //Received bifield message
                        if (isPeerInterested(message, remotePeerID)) {
                            sendInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(9);
                        } else {
                            sendNotInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                        }
                    }
                } else if (peerState == 9) {
                    if (messageType.equals(Message.MessageConstants.MESSAGE_CHOKE)) {

                        logAndPrint("CHOKED by Peer " + remotePeerID);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsChoked(1);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(14);
                    } else if (messageType.equals(Message.MessageConstants.MESSAGE_UNCHOKE)) {

                        logAndPrint("UNCHOKED by Peer " + remotePeerID);
                        //get the piece index which is present in remote peer but not in current peer and send a request message
                        int firstDifferentPieceIndex = getFirstDifferentPieceIndex(remotePeerID);
                        if (firstDifferentPieceIndex == -1) {
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                        } else {
                            sendRequestMessage(peerProcess.peerToSocketMap.get(remotePeerID), firstDifferentPieceIndex, remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(11);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setStartTime(new Date());
                        }
                    }
                } else if (peerState == 11) {
                    if (messageType.equals(Message.MessageConstants.MESSAGE_CHOKE)) {
                        logAndPrint("CHOKED by Peer " + remotePeerID);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setIsChoked(1);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(14);
                    } else if (messageType.equals(Message.MessageConstants.MESSAGE_PIECE)) {

                        byte[] payloadInBytes = message.getPayload();

                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setEndTime(new Date());
                        long totalTime = peerProcess.remotePeerDetailsMap.get(remotePeerID).getEndTime().getTime()
                                - peerProcess.remotePeerDetailsMap.get(remotePeerID).getStartTime().getTime();
                        double dataRate = ((double) (payloadInBytes.length + Message.MessageConstants.MESSAGE_LENGTH + Message.MessageConstants.MESSAGE_TYPE) / (double) totalTime) * 100;
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setDataRate(dataRate);
                        Piece piece = FilePieceDelegate.convertByteArrayToFilePiece(payloadInBytes);

                        peerProcess.bitFieldMessage.updateBitFieldInformation(remotePeerID, piece);
                        int firstDifferentPieceIndex = getFirstDifferentPieceIndex(remotePeerID);
                        if (firstDifferentPieceIndex == -1) {
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                        } else {
                            sendRequestMessage(peerProcess.peerToSocketMap.get(remotePeerID), firstDifferentPieceIndex, remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(11);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setStartTime(new Date());
                        }

                        peerProcess.updateOtherPeerDetails();
                        for (String key : peerProcess.remotePeerDetailsMap.keySet()) {
                            RemotePeerInfo peerDetails = peerProcess.remotePeerDetailsMap.get(key);
                            if (!key.equals(peerProcess.currentPeerID) && hasPeerInterested(peerDetails)) {
                                sendHaveMessage(peerProcess.peerToSocketMap.get(key), key);
                                peerProcess.remotePeerDetailsMap.get(key).setPeerState(3);
                            }
                        }

                        if (!peerProcess.isFirstPeer && peerProcess.bitFieldMessage.isFileDownloadComplete()) {
                            for (String key : peerProcess.remotePeerDetailsMap.keySet()) {
                                if (!key.equals(peerProcess.currentPeerID)) {
                                    Socket socket = peerProcess.peerToSocketMap.get(key);
                                    if (socket != null) {
                                        sendDownloadCompleteMessage(socket, key);
                                    }
                                }
                            }
                        }
                    }
                } else if (peerState == 14) {
                    if (messageType.equals(Message.MessageConstants.MESSAGE_HAVE)) {
                        logAndPrint("contains interesting pieces from Peer " + remotePeerID);
                        if (isPeerInterested(message, remotePeerID)) {
                            sendInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(9);
                        } else {
                            sendNotInterestedMessage(peerProcess.peerToSocketMap.get(remotePeerID), remotePeerID);
                            peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(13);
                        }
                    } else if (messageType.equals(Message.MessageConstants.MESSAGE_UNCHOKE)) {
                        logAndPrint("UNCHOKED by Peer " + remotePeerID);
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(14);
                    }
                } else if (peerState == 15) {
                    try {
                        peerProcess.remotePeerDetailsMap.get(peerProcess.currentPeerID).updatePeerDetails(remotePeerID, 1);
                        logAndPrint("Peer: " + remotePeerID + " has downloaded the complete file");
                        int previousState = peerProcess.remotePeerDetailsMap.get(remotePeerID).getPreviousPeerState();
                        peerProcess.remotePeerDetailsMap.get(remotePeerID).setPeerState(previousState);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
