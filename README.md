# TCP-ON-TOP-OF-UDP
Networks Project Report

Introduction
Networks are connecting millions of devices and end-systems, making millions of peoples able to communicate and reach each other.
To manage all of this links there must be protocols to monitor and make sending the packets easier and smoother.
Stop-and-wait is a method in telecommunications to send information between two connected devices. It ensures that information is not lost due to dropped packets and that packets are received in the correct order. It is the simplest mechanism. A stop-and-wait ARQ sender sends one frame at a time; it is a special case of the general sliding window protocol with transmit and receive window sizes equal to one and greater than one respectively. After sending each frame, the sender doesn't send any further frames until it receives an acknowledgement (ACK) signal. After receiving a valid frame, the receiver sends an ACK. If the ACK does not reach the sender before a certain time, known as the timeout, the sender sends the same frame again. The timeout countdown is reset after each frame transmission. The above behavior is a basic example of Stop-and-Wait. However, real-life implementations vary to address certain issues of design.

Go Back N  isa protocol in which the sending process continues to send a number of frames specified by a window size even without receiving an acknowledgement (ACK) packet from the receiver. It is a special case of the general sliding window protocol with the transmit window size of N and receive window size of 1. It can transmit N frames to the peer before requiring an ACK.

Selective Repeat the sender sends a number of frames specified by a window size even without the need to wait for individual ACK from the receiver as in Go-Back-N. The receiver may selectively reject a single frame, which may be retransmitted alone, which must send every frame from that point again. The receiver accepts out-of-order frames and buffers them. The sender individually retransmits frames that have timed o



How To run the code
1.	Set the two text files server.txt client.txt as follows in the server arguments. **I didn’t get what is the Rando generator seedvalue so please take care that the arguments of server are only 3
 
2.	Start the server process
 
3.	Specify the kind of protocol 
4.	Open the client process
 
5.	Specify the kind of protocol please select the same kind of protocol of server to avoid clashes
6.	If you want to run multi clients repeat 5 and 6 







Server 
Implementation of server started by using ServerSocket class which takes the port number you wish to start server on it, then for making the network being able to support multiple clients the server extends Runnable to be able to use threads the srever then enters an infinite loop waiting for clients to hock the client when this event happens and a client ask for connection the server runs a separate thread to handle this sessions by first creating an ObjectInputStream and ObjectOutputStream which link with the client so that sending and receiving packets and acknowledgment, linking the server with the client is by using accept() method which keeps listening for the socket till a client ask for connection.
 
 The server then ask the user for the kind of protocol he/she wish to use and start sending the file using this protocol.







Client 
The client is simpler it uses the Socket class and It must be announced that this class free the programmer from the headache of specifying the port number as it randomly assigning a port number for each client to be created by it. the client ask


Stop and Wait
In implementing stop and wait, the server start by sending a packet and wait for the acknowledgment of the packet and then start sending the next packet, as for the client it keep waiting for a packet to be sent and when it receive a packet it send an ACK. 
For sending a packet the server send a packet and then start a timer to manage the waiting time of receiving an ACK if the specified time ran out this means wither the packet was lost and the client didn’t receive it or the ACK was lost.
For receiving the packets the client keep waiting for a packet then test by using the checksum wither it’s corrupted or not and if not corrupted the client saves the packets and send an ACK.
The Stop and wait doesn’t use any kind of special data structures as the implementation as follows the uses the class Path to open the file then by using a while loop which continues until all the packets are sent correctly and ACK are received.
By using Byte[] the opened file is converted to it the server start segmenting this Byte[] by size of 500 bytes and start sending it the client keep tracking these bytes and write it in an DataStream and when sending is done the DataStream is converted back to file which is the received file.


Network analysis (S & W)
		For PLP 1%
•	Run #1 = 181secs    
•	Run #2 =  162secs
•	Run #3 = 160sec
•	Run #4 = 180secs
•	Run #5 = 180secs
Average = 172.5secs
	For PLP 5%
•	Run #1= 446sec
•	Run #2= 443sec
•	Run #3= 440sec
•	Run #4= 448sec
•	Run #5= 425sec
Average = 440.5sec
For PLP 10%
•	Run #1= 929 sec
•	Run #2 = 989sec
•	Run #3 = 971sec 
•	Run #4 = 975sec
•	Run #5 = 980sec
Average = 968.8sec
For PLP 30%
•	Run #1 = sec
•	Run #2 = sec
•	Run #3 = sec
•	Run #4 = sec
•	Run #5 = sec
Average =
//couldn’t calculate it due to the deadline 

Go Back N
In implementing Go back N the server I given a window size the server then start sending number of packets equal to window and for the Ack of this packet if any of these packets were lost the server starts sending the first lost packet and all the consecutive packets even if those packets were received.
On the receiving side the client starts the client start receiving the packets and acknowledging those packets and it detect by checking the sequence number if any packet was lost so that the server can detect lost packet and resend it.
The Go Back as Stop and wait doesn’t require special data structures only a Byte array to hold the entire file then by start partitioning the byte array to packets and then send them as mentioned the most important thing is to detect if any packet was lost if so the base of the sending window or expected packet to be received is the same of received packet then this base is incremented and no problem is done else the base stop and all the coming ack is ignored and in the next iteration we start sending from the base which we stopped at.
 








Network Analysis (GBN)
For PLP 1%
•	Run #1 = 98sec
•	Run #2 =  97sec
•	Run #3 = 101sec
•	Run #4 = 99sec
•	Run #5 = 107sec
Average = 100.4sec
For PLP 5%
•	Run #1 = 146sec
•	Run #2 = 141sec
•	Run #3 = 140sec
•	Run #4 = 150sec
•	Run #5 = 142sec
Average = 143.8sec
For PLP 10%
•	Run #1 = 228sec
•	Run #2 = 233sec
•	Run #3 = 231sec
•	Run #4 = 236sec
•	Run #5 = 232sec
Average = 232sec 
For PLP 30%
•	Run #1 = 909sec
•	Run #2 = 911 sec
•	Run #3 = 915 sec
•	Run #4 = 908sec
•	Run #5 = 913sec
Average = 911.2


Selective Repeat
In implementing Selective repeat the server start by also specifying a window size then the sender start sending those packets and wait for the acknowledgment if any packet were lost or no acknowledgment was lost the server sends the lost packet only not like the GBN sends starting from lost packet till end of the window.
	Client side starts by waiting for packets to be delivered and starts acknowledging the received packets and save them.
In selective repeat the problem was when to increment the base and by how much and when to stop it, in other words when a window of 10 is used if the packet number 5 is lost the base must point to 5 and if 5 is the only lost packet then 5 only to be sent from this window this was done by using tree map and the biggest advantage of tree map is that it arrange the entries according to their key so this  it helped in maintain the order of the packets, so when dealing with base for any sent packet it was put in the treemap called packets with value -1 which means it is not acked yet and for every ack was received this -1 change to 0 so the base iterate over this treemap if the value is -1 the base stop as the packet is not ack if 0 the base increments until -1 is encountered.
 
Also another treemap is used to carry the seq numbers of the packets which are “w8ing4Ack” which store the packets that are not acked yet.


Network Analysis
For PLP 1%
•	Run #1 =  176  
•	Run #2 =  175
•	Run #3 = 175
•	Run #4 = 173
•	Run #5 = 178
Average = 175.4
For PLP 5%
•	Run #1 = 183sec
•	Run #2 = 182sec
•	Run #3 = 183sec
•	Run #4 = 183sec
•	Run #5 = 183sec
Average = 182.8
For PLP 10%
•	Run #1 = 211sec
•	Run #2 = 212sec
•	Run #3 = 214sec
•	Run #4 = 210sec
•	Run #5 = 213sec
Average= 212 sec
For PLP 30%
•	Run #1 = 342sec
•	Run #2 = 348sec
•	Run #3 = 344sec
•	Run #4 = 349sec
•	Run #5 = 345sec
Average =345.6sec



Simulating Packet Loss
PLP is used and Random() is also used to generate a random number from 0 to 100 then divided by 100 to get the percentage if this random is lost the packet is sent else the packet is lost.
     

Simulating Packet Corruption
As the Packet structure will be discussed later the data is Byte array type so if the we in the probability of lost packet we make another probability check like of the lost then we pick a random index of byte array and assign to it a rubbish number, the check sum is calculated before corruption then packet.
            






Packet
It is implemented by creating a class extending serializable so that it can be used with Socket library it has checksum seqnum and Byte array for data.
 
The data part and the file partitioned as follow the file is converted to byte array.
 
The number of packets are calculated, then for each sending packet the packet the use a data of size 500 bytes by copying 500 byte in each iteration to use in it the packet.
 







Check Sum
First thing the check sum is calculated by summing all the 500 byte of the packet and passed to the packet check sum.
 
Then data is corrupted as described then the packet is created and sent, on the client side the client receive the packet and check wither the checksum sent is equal to the checksum calculated if true the packet is good to go else it’s corrupted.
 
