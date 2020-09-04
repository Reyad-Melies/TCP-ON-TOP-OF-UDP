package Protocol;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    private static Socket client;
    private static int sequence, filesize;
    private static ObjectInputStream input = null;
    private static ObjectOutputStream output = null;
    private static File file ;

    public static void main(String args[]) {

//        Scanner s = new Scanner(System.in);
//        System.out.println("Please the file name");
//        String fileName = s.nextLine();
//        System.out.println("Please the file name");
//        int portNum = s.nextInt();
        try {
            FileReader fileReader = new FileReader("C:\\Users\\Mohab\\IdeaProjects\\Network\\src\\Protocol\\client.txt");

            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line = bufferedReader.readLine();

            List<String> parameters = Arrays.asList(line.split(","));

            String hostIPAddress = parameters.get(0);
            int serverPortNumber = Integer.parseInt(parameters.get(1));
            int clientPortNumber = Integer.parseInt(parameters.get(2));
            String fileName = parameters.get(3);
            int windowSize = Integer.parseInt(parameters.get(4));
            float plp = Float.valueOf(parameters.get(5));

            Scanner s = new Scanner(System.in);
            System.out.println("Enter the type of Protocol\n0: Stop and Wait\n1: GoBack N\n2: Selective Repeat");
            int choice = s.nextInt();

            switch(choice){

                case 0:
                    stop_wait(plp,fileName,hostIPAddress,serverPortNumber,clientPortNumber);
                    break;
            case 1:
                GoBackN(plp,fileName,hostIPAddress,serverPortNumber,clientPortNumber);
                break;
            case 2:
                SelectiveRepeat(plp,fileName,hostIPAddress,serverPortNumber,clientPortNumber);
                break;
            }


        } catch (IOException i){
            System.out.println(i);
        }

    }
    private static void stop_wait(double plp, String fileName,String hostIPAddress, int serverPortNumber, int clientPortNumber){

        try {

            System.out.println("Connected successfully!");
            client = new Socket(hostIPAddress, serverPortNumber);//trying to open a sender
            //Input and output Streams (Object so that I can send Packet Object)
            output = new ObjectOutputStream(client.getOutputStream());
            input = new ObjectInputStream(client.getInputStream());
            synchronized (output) {
                output.writeObject(fileName);
            }
            //Collecting all received Bytes to convert them to byte array after receiving process finished
            ByteArrayOutputStream dataSrteam = new ByteArrayOutputStream();
            int seqNum = -1;
            float random;
            Random ran = new Random();
            Packet dataPckt = null;


            System.out.println("Starting to receive File");
            int noOfPckts = 0;
            while(true){
                //receive the sent packet extract the sequence Number and data from it
                try {
                    client.setSoTimeout(100);
                    dataPckt = (Packet) input.readObject();
                }catch (SocketTimeoutException t){
                    System.out.println("Time");
                    continue;
                }

                seqNum = dataPckt.getSequenceNumber();
                //if the sent packet data is null and seq number is -1 the it's an indicator that the sent process is finished
                if(dataPckt.getData() == null && dataPckt.getSequenceNumber() == -1)
                    break;
                //sending an Ack with the last received seq Num
                random = ((float)ran.nextInt(101))/100 ;
                System.out.println(random);

                if( random >= plp && checker(dataPckt.getCheckSum(),dataPckt.data)){
                    Packet ackPckt = new Packet(seqNum,null,(short)0);
                    output.writeObject(ackPckt);
                    if(seqNum == dataPckt.getSequenceNumber()) {
                        dataSrteam.write(dataPckt.getData());
                        noOfPckts++;
                        System.out.println(noOfPckts);
                    }
                }else{
                    System.out.println("Bara");
                }
            }
            System.out.println("Packets Received Successfully!");
            byte [] recievedData = dataSrteam.toByteArray();
            System.out.println("Creating the output file");
            FileOutputStream outputFile = new FileOutputStream("C:\\Users\\Mohab\\IdeaProjects\\Network\\src\\Protocol\\output"+fileName+".jpg");
            outputFile.write(recievedData);
            System.out.println("File size: "+recievedData.length+", Number Of Packet: "+noOfPckts);
            System.out.println("File created successfully");

        }catch (IOException e){
            System.err.println(e);
        }catch (ClassNotFoundException c) {
            System.err.println(c);
        }

    }


    public static void GoBackN(double plp, String fileName,String hostIPAddress, int serverPortNumber, int clientPortNumber) {

        ObjectOutputStream output = null;
        ObjectInputStream input = null;
        try {
            System.out.println("Connected successfully!");
            client = new Socket(hostIPAddress, serverPortNumber);
            //Input and output Streams (Object so that I can send Packet Object)
            output = new ObjectOutputStream(client.getOutputStream());
            input = new ObjectInputStream(client.getInputStream());
            synchronized (output) {
                output.writeObject(fileName);
            }
        } catch (IOException e) {
            System.err.println(e);
        }

        //Collecting all received Bytes to convert them to byte array after receiving process finished
        ByteArrayOutputStream dataSrteam = new ByteArrayOutputStream();
        Packet dataToReceive;
        Random r = new Random();
        float random;
        int receivedsSeqNum = 0, base = 0, windowSize = 10, noOfPckts= 0,i = 0, nxtsSeqNum= 0;
        byte[] receivedData;
        short checkSum;

        while (true) {
            try {

                random = (float) r.nextInt(101) / 100;

                try {
                    client.setSoTimeout(200);
                    dataToReceive = (Packet) input.readObject();
                }catch(SocketTimeoutException s) {
                    break;
                }
                receivedsSeqNum = dataToReceive.getSequenceNumber();
                receivedData = dataToReceive.getData();

                if(receivedsSeqNum == -1 && receivedData ==null)
                    break;


                if (checker(dataToReceive.getCheckSum(), receivedData)) {

                    if (random >= plp)
                        output.writeObject(new Packet(receivedsSeqNum, null, (short) 0));
                    if (base == receivedsSeqNum) {
                        dataSrteam.write(receivedData);
                        base++;

                        noOfPckts++;
                    }
                }

            } catch (IOException e) {
                break;
            } catch (ClassNotFoundException c) {
                System.err.println(c);
            }
        }
        System.out.println("Packets Received Successfully!");
        byte[] recievedData = dataSrteam.toByteArray();
        System.out.println("Creating the output file");
        try{
        FileOutputStream outputFile = new FileOutputStream("C:\\Users\\Mohab\\IdeaProjects\\Network\\src\\Protocol\\"+fileName);
        outputFile.write(recievedData);
        }catch (IOException io){
            System.err.println(io);

        }

        System.out.println("File size: "+recievedData.length+", Number Of Packet: "+noOfPckts);
        System.out.println("File created successfully");

    }

    public static void SelectiveRepeat (double plp, String fileName,String hostIPAddress, int serverPortNumber, int clientPortNumber){

        ObjectOutputStream output = null;
        ObjectInputStream input = null;
        try {
            System.out.println("Connected successfully!");
            client = new Socket(hostIPAddress, serverPortNumber);
            //Input and output Streams (Object so that I can send Packet Object)
            output = new ObjectOutputStream(client.getOutputStream());
            input = new ObjectInputStream(client.getInputStream());
            synchronized (output) {
                output.writeObject(fileName);
            }
        } catch (IOException e) {
            System.err.println(e);
        }

        //Collecting all received Bytes to convert them to byte array after receiving process finished
        ByteArrayOutputStream dataSrteam = new ByteArrayOutputStream();
        Packet dataToReceive;
        Random r = new Random();
        float random;
        int receivedsSeqNum = 0;
        byte[] receivedData;
        short checkSum;
        TreeMap<Integer, byte[]> data = new TreeMap<>();

        while(true){

            try{

                random = (float) r.nextInt(101)/100;
                dataToReceive = (Packet) input.readObject();
                receivedsSeqNum = dataToReceive.getSequenceNumber();
                receivedData = dataToReceive.getData();

                if(receivedsSeqNum == -1 && receivedData ==null)
                    break;

                if(checker(dataToReceive.getCheckSum(),receivedData)){

                    if(random >= plp ){
                        output.writeObject(new Packet (receivedsSeqNum,null,(short)0));
                    }
                    data.put(receivedsSeqNum,receivedData);

                }

            }catch (IOException c){
                break;
            }catch (ClassNotFoundException c){
                System.err.println(c);
            }

        }
        try {
            for (Integer entery : data.keySet()) {
                dataSrteam.write(data.get(entery));
            }
            byte [] outputData = dataSrteam.toByteArray();

            FileOutputStream outputFile = new FileOutputStream("C:\\Users\\Mohab\\IdeaProjects\\Network\\src\\Protocol\\"+fileName);
            outputFile.write(outputData);
            System.out.println("File size: "+outputData.length+", Number Of Packet: "+data.size());
            System.out.println("File created successfully");
        }catch(IOException io){
            System.err.println(io);
        }
    }

    public static boolean checker (short checkSum, byte[] recieved){

        short rec_checksum= 0;

        for(byte b: recieved){
            rec_checksum += b;
        }

        if(checkSum == rec_checksum){
            return true;
        }

        else {
            return  false;
        }
    }

}