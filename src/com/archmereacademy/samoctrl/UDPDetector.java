package com.archmereacademy.samoctrl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class UDPDetector {
	DatagramSocket c;
	public static final int port = 1901;
	public void run() {
		// Find the server using UDP broadcast
		try {
		  //Open a random port to send the package
		  c = new DatagramSocket();
		  c.setBroadcast(true);

		  byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();

		  //Try the 255.255.255.255 first
		  try {
		    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), port);
		    c.send(sendPacket);
		    System.out.println(getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
		  } catch (Exception e) {
		  }

		  // Broadcast the message over all the network interfaces
		  Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		  while (interfaces.hasMoreElements()) {
		    NetworkInterface networkInterface = interfaces.nextElement();

		    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
		      continue; // Don't want to broadcast to the loopback interface
		    }

		    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
		      InetAddress broadcast = interfaceAddress.getBroadcast();
		      if (broadcast == null) {
		        continue;
		      }

		      // Send the broadcast package!
		      try {
		        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, port);
		        c.send(sendPacket);
		      } catch (Exception e) {
		      }

		      System.out.println(getClass().getName() + ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
		    }
		  }

		  System.out.println(getClass().getName() + ">>> Done looping over all network interfaces. Now waiting for a reply!");

		  //Wait for a response
		  byte[] recvBuf = new byte[15000];
		  DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
		  c.receive(receivePacket);

		  //We have a response
		  System.out.println(getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

		  //Check if the message is correct
		  String message = new String(receivePacket.getData()).trim();
		  System.out.println("Recieved '"+message+"'.");

		  //Close the port!
		  c.close();
		} catch (IOException ex) {
		  ex.printStackTrace();
		}
	}
}
