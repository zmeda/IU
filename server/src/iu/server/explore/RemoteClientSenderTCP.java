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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Periodically send packets to a remote client, constructed from objects on the remoteclient's outqueue
 */
public class RemoteClientSenderTCP extends Thread 
{
	private boolean running;
	
	
	//
	// buffer - FIFO queue that holds commands and data
	// for client
	//
	private LinkedList<byte[]> 	buffer;
	
	private LinkedList<byte[]> 	eventBuffer;
	
	private boolean 			bufferEvents;
	private Socket socket;



	public RemoteClientSenderTCP(RemoteClient client, Socket socket)
	{
		this.socket = socket;
		this.buffer = new LinkedList<byte[]>();
	}

	@Override
	public void run()
	{
		this.running = true;
		
		DataOutputStream socketWriter = null;
		
		try {
			socketWriter = new DataOutputStream(this.socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.running = false;
		}
		
		while (this.running)
		{
			
			try
			{
				if (this.buffer.size() > 0)
				{
					// send first command in buffer
					socketWriter.write(this.buffer.remove());
				}
				else
				{
					Thread.sleep(300);
				}
				
				
				
				

			}
			catch (IOException except)
			{
				System.err.println("Failed I/O: " + except);
			} catch (InterruptedException e) 
			{
				// thread has been interupted - buffer is not empty
			}
		}
	}

	/**
	 * Stops this thread 
	 */
	public void stopRunning()
	{
		this.running = false;
	}
	
	/**
	 * 
	 * @param data
	 */
	private void send(byte[] data)
	{
		synchronized (this.buffer) {
			
			this.buffer.add(data);
			
			//
			// notify this thread - this wil cause sending of buffered data
			//
			//this.notify();
		}
	}
	
	/**
	 * 
	 * @param data
	 */
	private void send(Collection<byte[]> data)
	{
		synchronized (this.buffer) {
			
			this.buffer.addAll(data);
			
			//
			// notify this thread - this wil cause sending of buffered data
			//
			//this.notify();
		}
	}
	
	/**
	 *  Adds data to buffer which will be sent when possible
	 * @param data
	 */
	public void sendData (byte[] data)
	{
		this.send(data);
	}
	
	/**
	 * All events are sent trought this method
	 * events can be buffered if client request it
	 * @param event
	 */
	public void sendEvent (byte[] event)
	{
		if (this.bufferEvents)
		{
			synchronized (this.eventBuffer) {
				this.eventBuffer.add(event);
			}
		}
		else
		{
			this.send(event);
		}
	}
	
	/**
	 * Calling this method causes that events will 
	 * be buffered until flushEvents() is called
	 */
	public void bufferEvents()
	{
		this.bufferEvents = true;
	}
	
	/**
	 * Sends all events that are buffered in event buffer
	 */
	public void flushEvents ()
	{
		this.bufferEvents = false;
		
		synchronized (this.eventBuffer) 
		{
			// move all events from eventsBuffer to buffer
			this.send(this.eventBuffer);
		}
	}
	

}
