package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import message.HandshakeMessage;
import message.Message;
import message.MessageInfo;
import peer.peerProcess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static logging.Logging.logAndPrint;
import static peer.peerProcess.messageQueue;

public class MessageHandler implements Runnable {
    private final ThreadLocal<HandshakeMessage> handshakeMessage = new ThreadLocal<>();
    String ownPeerId;
    String remotePeerId;
    private int connType;
    private InputStream socketInputStream;
    private OutputStream socketOutputStream;
    private Socket peerSocket;

    public MessageHandler(String address, int port, int connectionType, String serverPeerID) {
        boolean messageHandlerFlag=true;
        if(messageHandlerFlag==true){
            try {
                connType = connectionType;
                ownPeerId = serverPeerID;
                peerSocket = new Socket(address, port);
                socketInputStream = peerSocket.getInputStream();
                socketOutputStream = peerSocket.getOutputStream();


            } catch (IOException e) {
            }
        }
        else{
            messageHandlerFlag=true;
        }
    }

    public MessageHandler(Socket socket, int connectionType, String serverPeerID) {
        boolean messageHandlerFlag=true;
        if(messageHandlerFlag==true){
            try {
                peerSocket = socket;
                connType = connectionType;
                ownPeerId = serverPeerID;
                socketInputStream = peerSocket.getInputStream();
                socketOutputStream = peerSocket.getOutputStream();
            } catch (IOException e) {

            }
        }
        else{
            messageHandlerFlag=true;
        }
    }


    @Override
    public void run() {
        byte[] handShakeMessageInBytes = new byte[32];
        byte[] dataBufferWithoutPayload = new byte[Message.MessageConstants.MESSAGE_LENGTH + Message.MessageConstants.MESSAGE_TYPE];
        byte[] messageLengthInBytes;
        byte[] messageTypeInBytes;
        boolean hsmMessage=true;
        MessageInfo messageInfo = new MessageInfo();
        try {
            int check=Message.MessageConstants.ACTIVE_CONNECTION;
            if (connType == check ) {

                if (!(handShakeMessageSent() && hsmMessage)) {
                    logAndPrint(ownPeerId + " HANDSHAKE sending has failed");
                    System.exit(0);     
                } else {
                    logAndPrint(ownPeerId + " HANDSHAKE has been sent successfully");
                }


                while (true) {
                    socketInputStream.read(handShakeMessageInBytes);
                    handshakeMessage.set(HandshakeMessage.convertBytesToHandshakeMessage(handShakeMessageInBytes));
                    String checker=Message.MessageConstants.HANDSHAKE_HEADER;
                    if (handshakeMessage.get().getHeader().equals(checker)) {
                        if(hsmMessage==true){
                            logAndPrint(ownPeerId + " makes a connection to the Peer Id " + remotePeerId);
                            logAndPrint(ownPeerId + " Received a HANDSHAKE MESSAGE from the Peer Id " + remotePeerId);
                        }
                        peerProcess.peerToSocketMap.put(handshakeMessage.get().getPeerID(), this.peerSocket);
                        break;
                    }
                }

                Message d = new Message(Message.MessageConstants.MESSAGE_BITFIELD, peerProcess.bitFieldMessage.getBytes());
                socketOutputStream.write(Message.convertMessageToByteArray(d));
                peerProcess.remotePeerDetailsMap.get(remotePeerId).setPeerState(8);
            } else {
                while (true) {
                    socketInputStream.read(handShakeMessageInBytes);
                    handshakeMessage.set(HandshakeMessage.convertBytesToHandshakeMessage(handShakeMessageInBytes));
                    String checker=Message.MessageConstants.HANDSHAKE_HEADER;
                    boolean a=true;
                    if (handshakeMessage.get().getHeader().equals(checker) && a==true) {
                        remotePeerId = handshakeMessage.get().getPeerID();
                        if(hsmMessage==true){
                            logAndPrint(ownPeerId + " is connected from the Peer Id" + remotePeerId);
                            logAndPrint(ownPeerId + " Received a HANDSHAKE MESSAGE from the Peer Id " + remotePeerId);
                        }
                        peerProcess.peerToSocketMap.put(remotePeerId, this.peerSocket);
                        break;
                    } else {
                        continue;
                    }
                }
                if (handShakeMessageSent()==false) {
                    boolean sentFlag=false;
                    if(sentFlag==false){
                        logAndPrint(ownPeerId + " HANDSHAKE MESSAGE sending failed.");
                        System.exit(0);
                    }        

                } else {
                    boolean sentFlag=true;
                    if(sentFlag==true){
                        logAndPrint(ownPeerId + " HANDSHAKE MESSAGE has been sent successfully.");
                    }
                    
                }

                peerProcess.remotePeerDetailsMap.get(remotePeerId).setPeerState(2);
            }

            while (true) {
                int k=1;
                if (socketInputStream.read(dataBufferWithoutPayload) == -1 && k==1)
                    break;
                messageLengthInBytes = new byte[Message.MessageConstants.MESSAGE_LENGTH];
                messageTypeInBytes = new byte[Message.MessageConstants.MESSAGE_TYPE];
                System.arraycopy(dataBufferWithoutPayload, 0, messageLengthInBytes, 0, Message.MessageConstants.MESSAGE_LENGTH);
                System.arraycopy(dataBufferWithoutPayload, Message.MessageConstants.MESSAGE_LENGTH, messageTypeInBytes, 0, Message.MessageConstants.MESSAGE_TYPE);
                Message message = new Message();
                message.setMessageLength(messageLengthInBytes);
                message.setMessageType(messageTypeInBytes);
                String messageType = message.getType();
                String interested = Message.MessageConstants.MESSAGE_INTERESTED;
                String notInterested=Message.MessageConstants.MESSAGE_NOT_INTERESTED;
                String messageChoke= Message.MessageConstants.MESSAGE_CHOKE;
                String unChoke=Message.MessageConstants.MESSAGE_UNCHOKE;
                String download=Message.MessageConstants.MESSAGE_DOWNLOADED;
                if (messageType.equals(download)) {
                    messageInfo.setMessage(message);
                    messageInfo.setFromPeerID(remotePeerId);
                    int l=3;
                    if(l==3){
                        int peerState = peerProcess.remotePeerDetailsMap.get(remotePeerId).getPeerState();
                        l=4;
                    }
                    if(l==4){
                        int peerState = peerProcess.remotePeerDetailsMap.get(remotePeerId).getPeerState();
                        peerProcess.remotePeerDetailsMap.get(remotePeerId).setPreviousPeerState(peerState);
                        peerProcess.remotePeerDetailsMap.get(remotePeerId).setPeerState(15);
                        messageQueue.add(messageInfo);
                    }
                 } 
                else if (messageType.equals(interested) || messageType.equals(notInterested) || messageType.equals(messageChoke) || messageType.equals(unChoke)) {
                    int l=3;
                    if(l==3){
                        messageInfo.setMessage(message);
                        messageInfo.setFromPeerID(remotePeerId);
                        messageQueue.add(messageInfo);
                    }
                }
                else {
                    int bytesAlreadyRead = 0;
                    int bytesRead;
                    int temp = message.getMessageLengthAsInteger();
                    byte[] dataBuffPayload = new byte[temp - 1];
                    boolean flag=true;
                    while ((bytesAlreadyRead < message.getMessageLengthAsInteger() - 1) && flag==true) {
                        int temp1 = message.getMessageLengthAsInteger();
                        bytesRead = socketInputStream.read(dataBuffPayload, bytesAlreadyRead, temp - 1 - bytesAlreadyRead);
                        if (bytesRead == -1 && flag==true)
                            return;
                        bytesAlreadyRead += bytesRead;
                    }

                    int a = message.getMessageLengthAsInteger();
                    int b = Message.MessageConstants.MESSAGE_LENGTH;
                    a=a+b;
                    byte[] dataBuffWithPayload = new byte[a];
                    System.arraycopy(dataBufferWithoutPayload, 0, dataBuffWithPayload, 0, Message.MessageConstants.MESSAGE_LENGTH + Message.MessageConstants.MESSAGE_TYPE);
                    System.arraycopy(dataBuffPayload, 0, dataBuffWithPayload, Message.MessageConstants.MESSAGE_LENGTH + Message.MessageConstants.MESSAGE_TYPE, dataBuffPayload.length);
                    if(flag==true){
                        messageInfo.setMessage(Message.convertByteArrayToMessage(dataBuffWithPayload));
                        messageInfo.setFromPeerID(remotePeerId);
                        messageQueue.add(messageInfo);
                    }
                }
            }

        } catch (Exception e) {
        }
    }

    public boolean handShakeMessageSent() {
        boolean messageSent = false;
        boolean intialFlag=true;
        try {
            socketOutputStream.write(HandshakeMessage.convertHandshakeMessageToBytes(new HandshakeMessage(Message.MessageConstants.HANDSHAKE_HEADER, this.ownPeerId)));
            if(intialFlag==true){
                messageSent = true;
            }
        } catch (IOException e) {
        }
        return messageSent;
    }
}

