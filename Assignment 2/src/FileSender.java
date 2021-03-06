// Author: Jonathan Lim Siu Chi
// Matric Number : A0110839H

import java.net.*;
import java.nio.ByteBuffer;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.CRC32;

class FileSender {

	private DatagramSocket senderSocket;
	private File fileToSend;
	private int portNumber;
	private String receiverFileName;

	private static final int MAX_PACKET_SIZE = 1000;

	public static void main(String[] args) {

		// check if the number of command line argument is 4
		if (args.length != 3) {
			System.out.println("Usage: java FileSender <path/filename> "
					+ "<UnreliPort> <rcvFileName>");
			System.exit(1);
		}

		FileSender sender = new FileSender(args[0], args[1], args[2]);
		sender.sendFile();

	}

	public FileSender(String file, String portString, String rcvFileName) {
		this.fileToSend = new File(file);
		this.portNumber = Integer.parseInt(portString);
		this.receiverFileName = rcvFileName;

	}

	public void sendFile() {
		try {
			InetAddress receiverAddress = InetAddress.getByName("localHost");

			// create client socket
			senderSocket = new DatagramSocket();

			// create fileName datagram packet to send to receiver
			byte[] fileNameByteArray = receiverFileName.getBytes();

			// create packet byte[]
			byte[] packetByteArray = new byte[MAX_PACKET_SIZE];

			packetByteArray = addIndexToPacketHeader(0, packetByteArray);
			packetByteArray = addPayloadToPacket(fileNameByteArray,
					packetByteArray);
			packetByteArray = addChecksumToPacketHeader(packetByteArray);

			DatagramPacket fileNameDatagram = new DatagramPacket(
					packetByteArray, packetByteArray.length, receiverAddress,
					portNumber);

			// send fileName datagram packet
			senderSocket.send(fileNameDatagram);
			Thread.sleep(1);

			// send fileSize datagram packet
			byte[] fileSizeBuffer = ("" + fileToSend.length()).getBytes();
			DatagramPacket fileSizePacket = new DatagramPacket(fileSizeBuffer,
					fileSizeBuffer.length, receiverAddress, portNumber);
			senderSocket.send(fileSizePacket);
			Thread.sleep(1);

			// sending file in packets of at most 1000 bytes
			FileInputStream input = new FileInputStream(fileToSend);
			byte[] fileBuffer = new byte[MAX_PACKET_SIZE];
			int numOfBytes = 0;

			while ((numOfBytes = input.read(fileBuffer)) != -1) {
				DatagramPacket packet = new DatagramPacket(fileBuffer,
						numOfBytes, receiverAddress, portNumber);
				senderSocket.send(packet);
				Thread.sleep(1);
			}

			int fileSizeInBytes = (int) fileToSend.length();
			System.out
					.println("File of " + fileSizeInBytes + " bytes is sent.");
			input.close();
			senderSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Input/Output exception encountered.");
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Interrupted exception encountered.");
		}
	}

	private byte[] addPayloadToPacket(byte[] payloadByteArray,
			byte[] packetByteArray) {
		// first payload byte starts is the 12th byte in the packet
		System.arraycopy(payloadByteArray, 0, packetByteArray, 12,
				payloadByteArray.length);
		return packetByteArray;
	}

	private byte[] addIndexToPacketHeader(int index, byte[] packetByteArray) {
		byte[] indexByteArray = ByteConversionUtil.intToByteArray(index);

		// copying indexByteArray and fileName data to packet array
		System.arraycopy(indexByteArray, 0, packetByteArray, 8,
				indexByteArray.length);

		return packetByteArray;
	}

	private byte[] addChecksumToPacketHeader(byte[] packetByteArray) {
		// computing checksum
		CRC32 checksum = new CRC32();
		checksum.update(packetByteArray, 8, packetByteArray.length - 8);
		long checksumValue = checksum.getValue();

		byte[] checksumByteArray = ByteConversionUtil
				.longToByteArray(checksumValue);

		// adding checksum into packet byte[]
		System.arraycopy(checksumByteArray, 0, packetByteArray, 0,
				checksumByteArray.length);

		return packetByteArray;
	}

	private static class ByteConversionUtil {
		private static ByteBuffer longBuffer = ByteBuffer.allocate(Long.BYTES);
		private static ByteBuffer intBuffer = ByteBuffer
				.allocate(Integer.BYTES);

		private static byte[] longToByteArray(long value) {
			longBuffer.putLong(0, value);
			return longBuffer.array();
		}

		private static long byteArrayToLong(byte[] array) {
			longBuffer.put(array, 0, array.length);
			longBuffer.flip();
			return longBuffer.getLong();
		}

		private static int byteArrayToInt(byte[] array) {
			intBuffer.put(array, 0, array.length);
			intBuffer.flip();
			return intBuffer.getInt();
		}

		private static byte[] intToByteArray(int value) {
			intBuffer.putInt(0, value);
			return intBuffer.array();
		}
	}
}