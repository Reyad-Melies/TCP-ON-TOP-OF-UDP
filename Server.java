package Protocol;


import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.nio.file.*;

public class Server implements Runnable{

    private static ServerSocket serverSocket ;
    private  static Socket sender = null;
    private static int type ;
    private static int windowSize;
    private static float plp;

    public Server (Socket sender ,int type, int windowSize, float plp ) {
        this.sender = sender;
        this.type = type;
        this.windowSize = windowSize;
        this.plp = plp;
    }

    public static void main (String args[]){

                try{
                    System.out.println("Enter the type of Protocol\n0: Stop and Wait\n1: GoBack N\n2: Selective Repeat");
                    Scanner in = new Scanner(System.in);
                    int type = in.nextInt();
                    System.out.println("Opening Server...");

                    FileReader fileReader = new FileReader("C:\\Users\\Mohab\\IdeaProjects\\Network\\src\\Protocol\\server.txt");

                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                    String line = bufferedReader.readLine();

                    List<String> parameters = Arrays.asList(line.split(","));

                    int portNumber = Integer.parseInt(parameters.get(0));
                    int windowSize = Integer.parseInt(parameters.get(1));
                    float plp = Float.valueOf(parameters.get(2));

                    serverSocket = new ServerSocket(portNumber); // trying to open a server on portNumber
                    System.out.println("Server was opened successfully!");

                    while(true){

                        sender = serverSocket.accept();//trying to connect to the Client
                        System.out.println(sender);
                        Server server = new Server(sender, type, windowSize,plp);
                        new Thread(server).start();
                    }

                }catch(IOException e){
                    System.err.println(e);
                }

            }

    //work flow of each created thread
    @Override
    public void run(){

        ObjectInputStream input= null;
        ObjectOutputStream output= null;

        try{
            synchronized (sender) {
                input = new ObjectInputStream(sender.getInputStream());
                output = new ObjectOutputStream(sender.getOutputStream());
            }

        }catch (IOException e){
            System.out.println(e);
        }

        switch(type){

            case 0:
                stop_wait(plp,input,output);
                break;
            case 1:
                GoBackN(plp,input,output);
                break;
            case 2:
                SelectiveRepeat(plp,input,output);
                break;
        }
    }

    public static void stop_wait(double plp, ObjectInputStream input, ObjectOutputStream output){

            System.out.println("Connecting.... ");
            //trying to open a sender
            System.out.println("Client created successfully... ");

        if (sender != null && input != null && output != null) {

            Random ran = new Random();

            try{
                //Opening the file you want to send and convert it to byte arrays and append each packet with byte array of 500 bytes size
//                System.out.println("Openeing File...");
//                Path path = Paths.get("C:\\Users\\Mohab\\IdeaProjects\\Network\\src\\Protocol\\1.jpg");
//                byte [] data = Files.readAllBytes(path);

                System.out.println("Waiting for file configuration");
                String fileName = null;
                fileName = (String) input.readObject();

                Path path = Paths.get("C:\\Users\\Mohab\\IdeaProjects\\Network\\src\\Protocol\\"+fileName);
                byte [] data = Files.readAllBytes(path);
                System.out.println("File Opened Successfully!");
                int fileSize = data.length;
                int NoOfPackets = (fileSize / 500) + ((fileSize % 500 == 0) ? 0 : 1);
                int seqNumber = 0, startIndex = 0, endIndex = (500 > fileSize ? fileSize : 500);
                System.out.println("Packetization Process and start sending");
                System.out.println("File size: "+fileSize+", Number Of Packets to be sent: "+NoOfPackets);

                Packet ackPckt = null, dataPacket = null;
                int i = 0;
                float random;

                //Calculating time
                long startTime= System.nanoTime();
                while( i < NoOfPackets){

                    random = ((float)ran.nextInt(101))/100;

                    if(random >= plp) {
                        byte [] sendData = Arrays.copyOfRange(data, startIndex, endIndex);
                        short checksum =calcChecksum(sendData);
                        dataPacket = new Packet(seqNumber,sendData,checksum);
                        output.writeObject(dataPacket);

                    }else {
                        random = ((float)ran.nextInt(101))/100;

                        if(random >= plp) {
                            byte[] dummy = Arrays.copyOfRange(data, startIndex, endIndex);
                            short checksum = calcChecksum(dummy);
                            dummy[ran.nextInt(dummy.length)] = (byte) ran.nextInt(255);
                            dataPacket = new Packet(seqNumber, dummy, checksum);
                            output.writeObject(dataPacket);

                        }
                    }

                    try{
                        sender.setSoTimeout(100);
                        ackPckt = (Packet) input.readObject();
                    }catch(SocketTimeoutException t){
                        continue;
                    }
                    if(seqNumber == ackPckt.getSequenceNumber()){
                        startIndex = endIndex;endIndex = ((endIndex+500)>fileSize ? fileSize : endIndex+500);
                        seqNumber = (seqNumber++)% 2;
                        i++;
                    }

                }
                long endTime = System.nanoTime();
                System.out.println("Sending Process Ended!");

                System.out.println("Time Taken = "+((endTime-startTime)/1000000000)+"sec");
                System.out.println("Throughput = "+ (float)(NoOfPackets*500)/((endTime-startTime)/1000000000));

                Packet closingPckt = new Packet(-1,null,(short)0);

                output.writeObject(closingPckt);
            }catch(IOException e){
                System.err.println(e);
            }catch (ClassNotFoundException c){
                System.err.println(c);
            }
        }
    }

    public static void GoBackN (double plp, ObjectInputStream input, ObjectOutputStream output) {

            System.out.println("Connecting.... ");
            System.out.println("Client created successfully... ");

        if (sender != null && input != null && output != null) {

            try {
                //Opening File and segmentation

                System.out.println("Waiting for file configuration");
                String fileName = null;
                fileName = (String) input.readObject();
                Path path = Paths.get("C:\\Users\\Mohab\\IdeaProjects\\Network\\src\\Protocol\\"+fileName);
                byte[] data = Files.readAllBytes(path);
                System.out.println("File Opened Successfully!");
                int fileSize = data.length;
                int NoOfPackets = (fileSize / 500) + ((fileSize % 500 == 0) ? 0 : 1);
                int base = 0, nxtSeqNum = 0, seqNum = 0, receivedSeq;
                boolean flag = false;
                byte[] sendingData = null;
                short checkSum;
                Random r = new Random();
                float random;
                Packet dataToSend, dataToReceive= null;

                long startTime= System.nanoTime();

                while (true) {

                    while (nxtSeqNum - base < windowSize && nxtSeqNum < NoOfPackets) {

                        //setting the range of data to be sent
                        if(((nxtSeqNum * 500) + 500) >= fileSize)
                            sendingData = Arrays.copyOfRange(data, (nxtSeqNum * 500), fileSize);
                        else
                            sendingData = Arrays.copyOfRange(data, (nxtSeqNum * 500), (nxtSeqNum * 500) + 500);

                        checkSum = calcChecksum(sendingData);
                        random = (float)r.nextInt(101) / 100;


                        if (random >= plp) {
                            dataToSend = new Packet(seqNum, sendingData, checkSum);
                            output.writeObject(dataToSend);
                        } else {
                            random = (float)r.nextInt(101) / 100;

                            if (random >= plp) {
                                sendingData[r.nextInt(sendingData.length)] = (byte) r.nextInt(255);
                                dataToSend = new Packet(seqNum, sendingData, checkSum);
                                output.writeObject(dataToSend);
                            }
                        }
                        nxtSeqNum++;
                        seqNum ++;
                    }

                    while(true){
                        try{
                            sender.setSoTimeout(100);
                            dataToReceive = (Packet) input.readObject();
                        }catch(SocketTimeoutException t){
                            break;
                        }catch (ClassNotFoundException c){
                            System.out.println(c);
                        }

                        receivedSeq = dataToReceive.getSequenceNumber();

                        if(receivedSeq == base && !flag) {
                            base++;
                        }else
                            flag= true;// this flag is to tell the that there is a packet was lost in the middle so don't increase the base

                    }
                    flag = false;
                    nxtSeqNum = base;
                    seqNum = nxtSeqNum;

                    if (base == NoOfPackets)
                    {

                        output.writeObject(new Packet(-1,null,(short)0));
                        break;
                    }
                }
                long endTime = System.nanoTime();
                System.out.println("Sending Process Ended!");
                System.out.println("Time Taken = "+(endTime-startTime)/1000000000+"sec");
                System.out.println("Throughput = "+ (float)(NoOfPackets*500)/((endTime-startTime)/1000000000));

            } catch (IOException e) {
                System.out.println(e);
            } catch (ClassNotFoundException c){
                System.out.println(c);
            }
        }
    }

    public static void SelectiveRepeat(double plp, ObjectInputStream input, ObjectOutputStream output) {


        System.out.println("Connecting.... ");
        System.out.println("Client created successfully... ");

        if (sender != null && input != null && output != null) {

            try {
                //Opening File and segmentation
                System.out.println("Openeing File...");

                String fileName = null;
                fileName = (String) input.readObject();

                Path path = Paths.get("C:\\Users\\Mohab\\IdeaProjects\\Network\\src\\Protocol\\"+fileName);
                byte[] data = Files.readAllBytes(path);
                System.out.println("File Opened Successfully!");
                int fileSize = data.length;
                int NoOfPackets = (fileSize / 500) + ((fileSize % 500 == 0) ? 0 : 1);
                int base = 0,  nxtSeqNum = 0, seqNum = 0, counter= 0, receivedSeq, dumm, expectedNxtSeq= 0, index= 0;
                byte[] data_send = null;
                Packet dataToSend, dataToReceive= null;
                short checkSum;
                float random;
                Random r = new Random();
                boolean flag =true;
                HashMap<Integer, Integer> w8ing4Ack = new HashMap<>();
                TreeMap <Integer,Integer> packets = new TreeMap<>();

                long startTime= System.nanoTime();
                while(true){

                 while ((counter < windowSize)){

                     //setting the next to bes send by the unAcked packets
                     if(!w8ing4Ack.isEmpty()){
                         nxtSeqNum = seqNum = w8ing4Ack.get(index);
                         w8ing4Ack.remove(index++);
                         expectedNxtSeq--;

                     }else {
                         nxtSeqNum = seqNum= expectedNxtSeq;
                     }

                     if(!(nxtSeqNum< NoOfPackets))
                         break;

                     if(((nxtSeqNum)*500)+500 > fileSize){
                         data_send = Arrays.copyOfRange(data,(nxtSeqNum)*500, fileSize);
                     }else
                         data_send = Arrays.copyOfRange(data,(nxtSeqNum)*500, ((nxtSeqNum)*500)+500);

                     checkSum = calcChecksum(data_send);
                     random = (float) r.nextInt(101)/100;
                    //for every sent packet it's put in the packets and value of each -1 to indicate it's not ack yet
                     packets.put(seqNum,-1);

                     if (random >= plp){
                         dataToSend = new Packet(seqNum,data_send,checkSum);
                         output.writeObject(dataToSend);
                     }else{
                         random = (float) r.nextInt(101)/100;

                         if (random >= plp){
                             data_send[r.nextInt(data_send.length)] = (byte) r.nextInt(255);
                             dataToSend = new Packet(seqNum, data_send, checkSum);
                             output.writeObject(dataToSend);
                         }
                     }
                     seqNum++;
                     nxtSeqNum++;
                     expectedNxtSeq++;
                     counter++;
                 }

                 while(true){
                        try{
                            sender.setSoTimeout(200);
                            dataToReceive = (Packet) input.readObject();
                        }catch(SocketTimeoutException t){
                            break;
                        }catch (ClassNotFoundException c){
                            System.out.println(c);
                        }
                        receivedSeq = dataToReceive.getSequenceNumber();
                        packets.put(receivedSeq,0);
                    }

                    dumm = base;
                    w8ing4Ack.clear();
                    index= 0;
                    while(packets.get(dumm) != null){
                        //for every acked packet the value is changed to 0 and base is incremented
                        if(packets.get(dumm) == 0 && flag) {
                            base++;
                        }
                        else if(packets.get(dumm)== -1){
                            //for the packets that are not acked they are put in this data structure
                            w8ing4Ack.put(index++,dumm);
                            flag =false;
                        }
                        dumm++;
                    }
                    flag = true;

                    if (base == NoOfPackets)
                    {
                        output.writeObject(new Packet(-1,null,(short)0));
                        break;
                    }
                    counter = 0;
                    index=0;
                }
                long endTime= System.nanoTime();
                System.out.println("Time Taken = "+(endTime-startTime)/1000000000+"sec");
                System.out.println("Throughput = "+ (float)(NoOfPackets*500)/((endTime-startTime)/1000000000));
                System.out.println("Finished");

            }catch (IOException io){
                System.err.println(io);
            }catch (ClassNotFoundException c){
                System.out.println(c);
            }
        }
    }

    public static short calcChecksum (byte[] data){

        short checksum = 0;

        for (byte b : data)
            checksum += b;

        return checksum;
    }
}
