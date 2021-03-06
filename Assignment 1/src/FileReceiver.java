// Author: Jonathan Lim Siu Chi
// Matric Number : A0110839H

import java.net.*;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.BufferedOutputStream;

class FileReceiver {

    public DatagramSocket receiverSocket;
    public DatagramPacket pkt;

    private static final int MAX_PACKET_SIZE = 1000;

    public static void main(String[] args) {

	// check if the number of command line argument is 1
	if (args.length != 1) {
	    System.out.println("Usage: java FileReceiver port");
	    System.exit(1);
	}

	new FileReceiver(args[0]);

    }

    public FileReceiver(String localPort) {
	// get port number of receiver
	int portNumber = Integer.parseInt(localPort);

	try {
	    this.receiverSocket = new DatagramSocket(portNumber);
	    byte[] receiverBuffer = new byte[MAX_PACKET_SIZE];

	    // get output file name
	    DatagramPacket fileNamePacket = new DatagramPacket(receiverBuffer, receiverBuffer.length);
	    receiverSocket.receive(fileNamePacket);

	    String fileName = new String(fileNamePacket.getData(), 0, fileNamePacket.getLength());

	    // get file size
	    DatagramPacket fileSizePacket = new DatagramPacket(receiverBuffer, receiverBuffer.length);
	    receiverSocket.receive(fileSizePacket);
	    String fileSizeString = new String(fileSizePacket.getData(), 0, fileSizePacket.getLength());
	    int fileSizeInBytes = Integer.parseInt(fileSizeString);

	    // write to file as long as the file size is smaller than the expected size
	    BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(fileName, true));
	    int numOfBytes = 0;

	    while (numOfBytes < fileSizeInBytes) {
		DatagramPacket filePacket = new DatagramPacket(receiverBuffer,receiverBuffer.length);
		receiverSocket.receive(filePacket);
		
		output.write(filePacket.getData(), 0, filePacket.getLength());
		numOfBytes += filePacket.getLength();

	    }
	    
	    System.out.println("File of " + numOfBytes + " bytes is received.");
	    output.close();
	    receiverSocket.close();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }
}
