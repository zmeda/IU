package iu.android.network.explore;

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

import iu.android.engine.CommanderRegistry;
import iu.android.explore.Commander;
import iu.android.explore.ExplorePlayer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


/*
 * Wait for Object on queue. When one arrives, send a datagram.
 */

public class Sender 
{

	private Socket 			socket;
	
	private	DataOutputStream socketWriter;
	
	
	public Sender(Socket sendSocket)
	{
		this.socket = sendSocket;
		try 
		{
			socketWriter = new DataOutputStream(this.socket.getOutputStream());
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/**
	 *  Pauses event thread and 'start buffering' command is send to server 
	 *  which causes that new events are not send to client but they are buffered
	 *  and will be tranfered when 'flush buffer' command is send
	 * @throws IOException 
	 */
	public void bufferEvents() throws IOException 
	{
		synchronized (this.socketWriter) 
		{
			this.socketWriter.writeByte(Protocol.BUFFER_EVENTS);
		}
	}
	
	/**
	 *  Resume event and send command 'flush buffer' to server
	 *  which causes sending of all buffered events on server
	 *  to client
	 * @throws IOException 
	 */
	public void flushEvents() throws IOException
	{
		synchronized (this.socketWriter) 
		{
			this.socketWriter.writeByte(Protocol.FLUSH_EVENTS);	
		}
	}
	
	/**
	 * Sends request for flag units on flag-flagId
	 * @param flagId
	 * @throws IOException
	 */
	public void getFlagUnits(int flagId) throws IOException
	{
		synchronized (this.socketWriter) 
		{
			this.socketWriter.write(Protocol.GET_FLAG_UNITS);
			this.socketWriter.writeInt(flagId);
		}
	}
	
	/**
	 * Sets number of units at some flag to desired values 
	 * Server will check if this is possible and client to because this is can be done
	 * with commander editor
	 * 
	 * @param flagId
	 * @param unit1
	 * @param unit2
	 * @param unit3
	 * @throws IOException
	 */
	public void setFlagUnits(int flagId, short unit1, short unit2, short unit3) throws IOException
	{
		synchronized (this.socketWriter) 
		{
			this.socketWriter.write(Protocol.SET_FLAG_UNITS);
			this.socketWriter.writeInt(flagId);
			this.socketWriter.writeShort(unit1);
			this.socketWriter.writeShort(unit2);
			this.socketWriter.writeShort(unit3);
		}
	}
	
	
	/**
	 * Request player properties (name, ...) by its id
	 * 
	 * @param playerId
	 * @throws IOException
	 */
	public void getPlayerProperties(int playerId) throws IOException
	{
		synchronized (this.socketWriter) 
		{
			this.socketWriter.write(Protocol.GET_PLAYER_PROPERTIES);
			this.socketWriter.writeInt(playerId);
		}
	}
	
	/**
	 * 
	 * @param ePlayer
	 * @throws IOException
	 */
	public void setPlayerProperties(ExplorePlayer ePlayer) throws IOException 
	{
		synchronized (this.socketWriter) 
		{
			this.socketWriter.write(Protocol.SET_PLAYER_PROPERTIES);
			this.socketWriter.writeInt(ePlayer.getId());
			
			// write units
			Commander commander = CommanderRegistry.getCommander(ePlayer);
			this.socketWriter.writeShort(commander.getHovercraftCount());
			this.socketWriter.writeShort(commander.getTankCount());
			this.socketWriter.writeShort(commander.getArtilleryCount());
			
		}
	}
	
	public void attackFlag (int flagId, short unit1, short unit2, short unit3) throws IOException
	{
		synchronized (this.socketWriter) 
		{
			this.socketWriter.write(Protocol.ATTACK_FLAG);
			this.socketWriter.writeInt(flagId);
			this.socketWriter.writeShort(unit1);
			this.socketWriter.writeShort(unit2);
			this.socketWriter.writeShort(unit3);
		}
	}
	
	public void deffendFlag (int battleId, byte accept, short unit1, short unit2, short unit3) throws IOException
	{
		synchronized (this.socketWriter) 
		{
			this.socketWriter.write(Protocol.DEFFEND_FLAG);
			this.socketWriter.writeInt(battleId);
			this.socketWriter.write(accept);
			this.socketWriter.writeShort(unit1);
			this.socketWriter.writeShort(unit2);
			this.socketWriter.writeShort(unit3);
		}
	}
	
	
	public void getDataFlagBattle (int battleId) throws IOException
	{
		synchronized (this.socketWriter) 
		{
			this.socketWriter.write(Protocol.GET_BATTLE);
			this.socketWriter.writeInt(battleId);
		}
	}

	 


	public void notifyLogout() throws IOException
	{
		synchronized (this.socketWriter) 
		{
			this.socketWriter.writeByte(Protocol.LOGOUT);
		}
	}
	
	

	
}
