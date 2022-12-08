package message;

import peerfunctions.peerProcess;

public class SendFilePiece {
    
    int isPresent;
    int isAdded;
    String fromPeerID;
    byte[] content;
    int pieceIndex;

    public SendFilePiece() {
    }

    public static Piece convertByteArrayToFilePiece(byte[] payloadInBytes) {
        byte[] indexInBytes = new byte[Message.MessageConstants.PIECE_INDEX_LENGTH];
        Piece piece = new Piece();
        System.arraycopy(payloadInBytes, 0, indexInBytes, 0, Message.MessageConstants.PIECE_INDEX_LENGTH);
        piece.setPieceIndex(peerProcess.PeerProcessUtils.convertByteArrayToInt(indexInBytes));
        piece.setContent(new byte[payloadInBytes.length - Message.MessageConstants.PIECE_INDEX_LENGTH]);
        System.arraycopy(payloadInBytes, Message.MessageConstants.PIECE_INDEX_LENGTH, piece.getContent(), 0, payloadInBytes.length - Message.MessageConstants.PIECE_INDEX_LENGTH);
        return piece;
    }

    public int getIsAdded(){
        return isAdded;
    }

    public int setIsAdded(int isAdded){
        return this.isAdded = isAdded;
    }

    public int getIsPresent() {
        return isPresent;
    }

    public void setIsPresent(int isPresent) {
        this.isPresent = isPresent;
    }

    public int getPieceIndex() {
        return pieceIndex;
    }

    public void setPieceIndex(int pieceIndex) {
        this.pieceIndex = pieceIndex;
    }

    public String getFromPeerID() {
        return fromPeerID;
    }

    public void setFromPeerID(String fromPeerID) {
        this.fromPeerID = fromPeerID;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

   
}