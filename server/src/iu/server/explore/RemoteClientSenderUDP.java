package iu.server.explore;

/*************************************************************************************************************
 * IU 1.0b, a java real time strategy game
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 ************************************************************************************************************/

import iu.android.network.explore.Protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Periodically send packets to a remote client
 * At the moment only locations of visible players are sent trought UDP
 */
public class RemoteClientSenderUDP extends Thread
{
	private boolean	     running	        = true;
	
	private DatagramSocket		udpSend;
	
	private static int	 DATA_SIZE	        = 128;	                                                                    
	
	
	//
	// buffer for storing locations datagram packages
	//
	private final LinkedList<DatagramPacket> locationBuffer;
	
	private final LinkedList<byte[]> buffer;
	
	/////////////////////////////////////////////////
	// 0 - command
	// 1 - number of locations (1-10)
	// 2-121 - locations - each 12bytes
	// 122-127 - not used
	/////////////////////////////////////////////////
	
	//
	// if buffer flag is true all data will 
	//
	private boolean pause;
	
	
	//
	//  socket specifics
	//
	private static int	 CONNECTION_SPEED	= 14400;	                                                                // bits per second.. (modem speed)
	private static int	 PACKETS_PER_SECOND	= RemoteClientSenderUDP.CONNECTION_SPEED / (RemoteClientSenderUDP.DATA_SIZE * 8);
	private static int	 INTERPACKET_DELAY	= 1000 / RemoteClientSenderUDP.PACKETS_PER_SECOND;

	
	public RemoteClientSenderUDP(DatagramSocket socket)
	{
		this.udpSend = socket;
		this.locationBuffer = new LinkedList<DatagramPacket>();
		this.buffer = new LinkedList<byte[]> ( );
	}

	@Override
	public void run()
	{
		

		while (this.running)
		{
			// don't swamp the modem connection if a lot of packets are coming in..
			try
			{
				Thread.sleep(RemoteClientSenderUDP.INTERPACKET_DELAY); // this delay length is important
			}
			catch (InterruptedException except)
			{
				System.err.println("Warning: main thread interrupted: " + except);
			}

			try
			{
				//
				// if - else sentances implement priority of different commands
				//
				
				if (this.pause)
				{
					this.wait();
				}
				//
				// send locations to player 
				//
				else if (this.locationBuffer.size() > 0)
				{
					//
					// retrive and remove first element from queue
					//
					DatagramPacket packet = this.locationBuffer.remove();
					
					//
					// send packet
					//
					this.udpSend.send(packet);
				}
				else if (this.buffer.size() > 0)
				{
					byte[] arr = this.buffer.remove();
					
					DatagramPacket packet = new DatagramPacket(arr, arr.length);
					
					this.udpSend.send(packet);
				}
				else
				{
					// 
					// if nothing to sent go to sleep
					//
					this.wait();
				}
				
				

			}
			catch (IOException except)
			{
				System.err.println("Failed I/O: " + except);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * !!! NOT USED IN 1st release
	 * 
	 *  Creates Datagram Packages that are sent to client
	 *  Because max Datagram Package is 128 bytes maximum 10 player locations can be sent at once
	 *  (1 player location contains 12 bytes - playerId[int], rel.lan[float], rel.lon[float])
	 * @param playerLocations
	 * @return
	 */
	private Collection<DatagramPacket> createDatagramPackages (Collection<byte[]> playerLocations)
	{
		LinkedList<DatagramPacket> packets = new LinkedList<DatagramPacket>();
		
		//Safety Check
		if (playerLocations.size() == 0)
		{
			return packets;
		}
		
	
		byte[] datagram = new byte[128];
		
		//
		// add command identifier
		//
		datagram[0] = Protocol.EVENT_LOCATIONS_UPDATE;
		
		// 0 - command
		// 1 - number of locations
		// 2-121 - locations - each 12bytes
		// 122-127 - not used
		
		int index = 2; 
		for (byte[] location : playerLocations)
		{
			if (location.length != 12)
			{
				if (Debug.LUCAS)
				{
					System.out.println("createDatagramPackages - location length = "+location.length+" ... !=12");
				}
			}
			
			else
			{
				for (int i =0 ; i<11; ++i)
				{
					datagram[index+i] = location[i]; 
				}
				
				index += 12;
				if (index > 121)
				{
					//
					// set number of locations to 10 
					//
					datagram[1] = 10;
					packets.add(new DatagramPacket(datagram, datagram.length));
					index = 2;
					// create new daatagram byte array and add command into it
					datagram = new byte[128];
					datagram[0] = Protocol.EVENT_LOCATIONS_UPDATE;
					
					
				}
			}
		}
		
		//
		// if index is grater than 2 than last datagram is not completed yet
		// set number of locations and create new DatagramPacket and add it to vector
		//
		if (index > 2)
		{
			//
			// set size of last datagram
			//
			datagram [1] = (byte)((index-2)/12);
			//
			// add last datagram into vector
			//
			packets.add(new DatagramPacket(datagram, datagram.length));
		}
		
		return packets;
	}
	
	/**
	 * Byte arrays will be joined into datagram package and sent to user as soon as possible
	 * @param locations
	 */
	public void sendLocations (Collection<byte[]> locations)
	{
		synchronized (this.locationBuffer) {
			//
			// empty buffer - all data that has not been send is outdated
			//
			this.locationBuffer.clear();
			
			this.locationBuffer.addAll(this.createDatagramPackages(locations));
		}
		
		//
		// notify this thread if buffer is false
		//
		if (!this.pause)
		{
			this.notify();
		}
	}
	
	public void send( byte[] arr ) {
		
		synchronized (this.buffer) 
		{
			
			if (this.pause)
			{
				System.out.println("UPD sender paused. UPD package droped");
			}
			else
			{
				this.buffer.add(arr);
				this.notify();
			}
			
		}

	}
	
	/**
	 * When pause send is called all data will be droped
	 */
	public void pauseSend ()
	{
		this.pause = true;
	}
	
	/**
	 * Send thread will resume sending. All new data will be send.
	 */
	public void resumeSend ()
	{
		this.pause = false;
		this.notify();
	}
	
	/**
	 * Couses this thread to stop
	 */
	public void stopRunning()
	{
		this.running = false;
	}

}
