package Protocol;

import java.io.Serializable;

public class Packet implements Serializable{

    short checkSum= 0 ;
    short length ;
    int sequenceNumber ;
    byte[] data ;

    public short getCheckSum() {
        return checkSum;
    }

    public short getLength() {
        return length;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public byte[] getData() {
        return data;
    }

    public Packet(/*short checkSum,*/int sequenceNumber, byte[] data,short checkSum) {
        this.checkSum = checkSum;
        this.sequenceNumber = sequenceNumber;
        this.data = data;

    }
}
