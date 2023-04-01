import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;

import javax.sound.sampled.*;

public class App {
    public static void main(String[] args) throws Exception {
        final int ART_NET_PORT = 6454, ART_NET_LEN = 530, sACN_PORT = 5568, sACN_LEN = 638;

		//get file paths
		String
			filePath = args.length > 0 ? args[0] : "",
			ipString = args.length > 1 ? args[1] : "",
			audioPath = args.length > 2 ? args[2] : "";

		// test for no dmx file
		if (filePath == "") {
			System.out.println("Show File Player");
			System.out.println("----------------------");
			System.out.println("java App [DMX file path] [IP Address, set the last number to 255 to broadcast] [Audio file path (optional)]");
			System.out.println("java App Bells.dmx 192.168.1.255 Bells.wav");
			return;
		}

		//outgoing IP address
		InetAddress ip;

		try {
			ip = InetAddress.getByName(ipString);
		} catch (Exception e) {
			System.out.println("Invalid IP address: " + e.getMessage());
			return;
		}

		// test for invalid file path
		if (!new File(filePath).exists()) {
			System.out.println("Invalid DMX file path. File does not exist.");
			return;
		}

		// open DMX file
		RandomAccessFile f = new RandomAccessFile(filePath, "r"); 
		
		//get if file is Art-Net
		f.seek(4);
		boolean isArtNet = f.readChar() == 'A' && f.readChar() == 'r'; 
		int recordLen = (isArtNet ? ART_NET_LEN : sACN_LEN) + 4;
		byte[] packet = new byte[isArtNet ? ART_NET_LEN : sACN_LEN];

		//open network connection
		DatagramSocket udp = new DatagramSocket();
		udp.setBroadcast(true);
		udp.setReuseAddress(true);

		//audio player
		Clip c = null;

		// test for audio path passed
		if (audioPath != "") {
			// invalid audio file
			if (!new File(audioPath).exists()) {
				System.out.println("Invalid audio file path. File does not exist.");
			} else {
				// open audio file and start playback
				c = AudioSystem.getClip();
				c.open(AudioSystem.getAudioInputStream(new File(audioPath)));
				c.start();

				//wait for audio file to start
				while(c != null && !c.isRunning())
					Thread.sleep(1);
			}
		}

		//get current time
		long startTime = c == null ? System.currentTimeMillis() : 0; 

		//print start time
		System.out.println("Start: " + filePath + " at " + LocalDateTime.now().toString() + " IP:" + ip.toString());

		long now = 0, next = 0, index = 0;	

		//go to end of DMX file or Audio file
		while (next >= 0 && f.getFilePointer() < f.length() && (c == null || c.isRunning())) {
			f.seek(index++ * recordLen); //goto next record

			//get next frame
			next = f.read();
			next = next | f.read() * 0x100L;
			next = next | f.read() * 0x100L * 0x100L;
			next = next | f.read() * 0x100L * 0x100L * 0x100L;

			//get packet contents from file
			f.read(packet, 0, packet.length);
			
			do {
				//get current time
				now = startTime > 0 ? System.currentTimeMillis() - startTime : (c.getMicrosecondPosition() / 1000);
				Thread.sleep(1);
			} while (now < next); // wait for frame cue

			//send DMX packet
			udp.send(new DatagramPacket(packet, packet.length, ip, isArtNet ? ART_NET_PORT : sACN_PORT));
		}

		//close streams
		f.close();
		udp.close();

		//close audio file
		if (c != null)
			c.close();

		//print start time
		System.out.println("  End: " + filePath + " at " + LocalDateTime.now().toString());
    }
}
