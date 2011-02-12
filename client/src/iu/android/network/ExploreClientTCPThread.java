package iu.android.network;

/*************************************************************************************************************
 * IU 1.0b, a java realtime strategy game
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

import iu.android.Debug;
import iu.android.order.Order;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;

/**
 * Makes TCP connection to keep important in-game updates (i.e. now players joining, unit deaths, etc.)
 */
public class ExploreClientTCPThread extends Thread
{
	private boolean	         running	= false;
	private boolean	         loggedIn	= false;

	private Socket	         socket;

	// private BufferedWriter writer;
	// private BufferedReader reader;

	private DataOutputStream	writer;
	private DataInputStream	 reader;

	private ArrayList<Order>	receivedOrders;

	// private ArrayList<String> dataToSend = new ArrayList<String>();
	// private ArrayList<String> receivedData = new ArrayList<String>();
	//
	// private ArrayList<String> ourDeadUnitIDs = new ArrayList<String>(); // strings of the IDs of OUR newly-dead units
	// private ArrayList<Unit> ourDeadUnits = new ArrayList<Unit>(); // references to all our dead units ever

	/**
	 * Constructor
	 * 
	 * @param serverAddress
	 * @param game
	 * @param nPortNumber
	 * @param orderQueue
	 */
	public ExploreClientTCPThread(InetAddress serverAddress, int nPortNumber, ArrayList<Order> orderQueue)
	{
		//
		// Make connection
		//
		try
		{
			this.socket = new Socket(serverAddress, nPortNumber);

			this.writer = new DataOutputStream(this.socket.getOutputStream());
			this.reader = new DataInputStream(this.socket.getInputStream());

			this.running = true;

			//
			// We'll write the orders received from other players here
			//
			this.receivedOrders = orderQueue;
		}
		catch (IOException ioe)
		{
			this.running = false;
			Log.e(Debug.TAG, "Client failed to connect TCP Thread to server " + serverAddress + ":" + nPortNumber);
		}
	}

	@Override
	public void run()
	{
		//
		// Keep this running as long as we don't receive the halt ( ) method call
		//
		// try
		// {
		// while (this.running)
		// {
		// // if (this.loggedIn)
		// // {
		// // // this.exploreMode.get
		// // this.writer.writeByte(Protocol.GPS_COORDINATES);
		// // this.writer.writeDouble(123.234);
		// // this.writer.writeDouble(-123.234);
		// // }
		// }
		//
		// //
		// // Close this socket because the halt ( ) method has been called ...
		// //
		// try
		// {
		// this.socket.close();
		// }
		// catch (IOException ioe)
		// {
		// //
		// // Socket was already closed ... it is safe to ignore this exception
		// //
		// }
		//
		// }
		// catch (IOException ioe)
		// {
		// // FIXME - no throwing of this exception
		// }
	}

	/**
	 * Stop this thread and close the connection.-
	 */
	public void halt()
	{
		this.running = false;

		//
		// Debug ONLY!
		//
		Debug.Lucas.println("ClientTCPThread received halt ( ) command.-");
	}

	public boolean isLoggedIn()
	{
		return this.loggedIn;
	}

	public void setLoggedIn(boolean loggedIn)
	{
		this.loggedIn = loggedIn;
	}
}
