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

import iu.android.Debug;
import iu.android.engine.CommanderRegistry;
import iu.android.engine.FlagRegistry;
import iu.android.engine.PlayerRegistry;
import iu.android.explore.Commander;
import iu.android.explore.ExploreMode;
import iu.android.explore.ExplorePlayer;
import iu.android.explore.Flag;
import iu.android.explore.Rank;
import iu.android.explore.event.EventBattleStarted;
import iu.android.network.NetworkResources;
import iu.android.util.Converter;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

import android.location.Location;
import android.location.LocationManager;
import android.os.Message;
import android.util.Log;

/*
 * Wait for Datagram. When one arrives, put the contents on the 'queue' vector.
 */
public class ReceiverThread extends Thread
{
	private boolean	running	= true;
	private Socket		socket;


	public ReceiverThread (Socket socket)
	{
		this.socket = socket;
	}


	@Override
	public void run ( )
	{
		Debug.Joshua.print ("ReceiverThread running");
		try
		{
			DataInputStream socketReader = new DataInputStream (this.socket.getInputStream ( ));

			while (this.running)
			{
				//
				// read command
				//
				byte command = socketReader.readByte ( );

				Debug.Joshua.print ("Command received = " + command);

				switch (command)
				{
					case (Protocol.EVENT_FLAG_OWNER_CHANGED):
						{
							int flagId = socketReader.readInt ( );
							int ownerId = socketReader.readInt ( );
							this.eventFlagOwnerChanged (flagId, ownerId);
							break;
						}

					case (Protocol.EVENT_FLAG_STATE_CHANGED):
						{
							int flagId = socketReader.readInt ( );
							byte state = socketReader.readByte ( );
							this.eventFlagStateChanged (flagId, state);
							break;
						}

					case (Protocol.EVENT_EXPLORED):
						{
							// [cmdId[byte], tile.x[short], tile.y[short], field[byte]
							short tile_x = socketReader.readShort ( );
							short tile_y = socketReader.readShort ( );
							byte field_i = socketReader.readByte ( );
							this.eventFieldExplored (tile_x, tile_y, field_i);
							break;
						}
					case (Protocol.EVENT_FLAG_UNITS_CHANGED):
						{
							int flagId = socketReader.readInt ( );
							short unit1 = socketReader.readShort ( );
							short unit2 = socketReader.readShort ( );
							short unit3 = socketReader.readShort ( );
							this.eventFlagUnitsChanged (flagId, unit1, unit2, unit3);
							break;
						}

					case (Protocol.DATA_FLAG_UNITS):
						{
							int flagId = socketReader.readInt ( );
							short unit1 = socketReader.readShort ( );
							short unit2 = socketReader.readShort ( );
							short unit3 = socketReader.readShort ( );
							this.flagUnits (flagId, unit1, unit2, unit3);
							break;
						}
					case (Protocol.DATA_PLAYER_PROPERTIES):
						{
							int playerId = socketReader.readInt ( );

							//
							// read name - first read length of name than read x num of bytes and
							// convert it into string
							//
							byte nameLen = socketReader.readByte ( );
							byte[] byteArr = new byte[nameLen];
							socketReader.readFully (byteArr);
							String name = new String (byteArr);

							//
							// read number of units (hovercraft, tank, artirery)
							//
							short unit1 = socketReader.readShort ( );
							short unit2 = socketReader.readShort ( );
							short unit3 = socketReader.readShort ( );

							this.playerProperties (playerId, name, unit1, unit2, unit3);

							break;
						}
					case (Protocol.EVENT_FLAG_ATTACKED):
						{
							int battleId = socketReader.readInt ( );
							this.eventUnderAttack (battleId);
							break;
						}

					case (Protocol.DATA_FLAG_BATTLE):
						{
							int attackerId = socketReader.readInt ( );
							int flagId = socketReader.readInt ( );

							short attHover = socketReader.readShort ( );
							short attTank = socketReader.readShort ( );
							short attArtillery = socketReader.readShort ( );

							short defHover = socketReader.readShort ( );
							short defTank = socketReader.readShort ( );
							short defArtillery = socketReader.readShort ( );

							this.dataFlagBatle (attackerId, flagId, attHover, attTank, attArtillery, defHover,
									defTank, defArtillery);

							break;
						}

					case (Protocol.EVENT_START_BATTLE):
						{
							int battleId = socketReader.readInt ( );
							int tcpPort = socketReader.readInt ( );

							this.eventBattleStarted (battleId, tcpPort);
							break;
						}
					case (Protocol.EVENT_FLAG_DISCOVERED):
						{
							// [cmdId[byte], flagId[int], lat[float], lon[float], playerId[int]]

							byte[] b = new byte[16];
							socketReader.readFully (b);

							int flagId = Converter.bytesToInt (b, 0);
							float lat = Converter.bytesToFloat (b, 4);
							float lon = Converter.bytesToFloat (b, 8);
							int playerId = Converter.bytesToInt (b, 12);

							Location l = new Location (LocationManager.GPS_PROVIDER);
							l.setLatitude (lat);
							l.setLongitude (lon);

							ExplorePlayer p = PlayerRegistry.getPlayer (playerId);
							Flag flag = new Flag (l, p, flagId);

							break;
						}
					case (Protocol.EVENT_LOCATION_UPDATE):
						{

							// [cmdId[byte], playerId[int], lat[float], lon[float]]

							byte[] b = new byte[12];
							socketReader.readFully (b);

							int playerId = Converter.bytesToInt (b, 0);
							float lat = Converter.bytesToFloat (b, 4);
							float lon = Converter.bytesToFloat (b, 8);

							Location l = new Location (LocationManager.GPS_PROVIDER);
							l.setLatitude (lat);
							l.setLongitude (lon);

							ExplorePlayer p = PlayerRegistry.getPlayer (playerId);
							p.setActive (true);
							p.setLocation (l);

							break;
						}
					case (Protocol.EVENT_PLAYER_LOGGED_OUT):
						{
							int playerId = socketReader.readInt ( );
							ExplorePlayer p = PlayerRegistry.getPlayer (playerId);
							p.setActive (false);
							break;
						}
					case (Protocol.EVENT_PLAYER_PROMOTED):
						{
							int playerId = socketReader.readInt ( );
							ExplorePlayer p = PlayerRegistry.getPlayer (playerId);
							byte rank = socketReader.readByte ( );
							p.setRank (Rank.values ( )[rank]);
							break;
						}
					
					case (Protocol.EVENT_BATTLE_FINISHED):
					{
						int battleId = socketReader.readInt ( );
						
						Log.d ("IU", "Explore client received event BATTLE_FINISHED with ID " + String.valueOf (battleId));
						break;
					}
					
					default:
						{
							// For any commands that I can't handle, at least I need to know their length
							// so I can ignore just the right amount of data
							int len = Protocol.length (command);

							if (len == 0)
							{
								Log.e ("IU", "Unknown command: " + command);
							}
							else
							{
								Log.w ("IU", "Not implemented command: " + command + ", bytes ignored: " + len);
							}

							// I've already read the command byte
							byte[] bytesToIgnore = new byte[len - 1];
							socketReader.readFully (bytesToIgnore);
						}
						break;

				}
			}

		}
		catch (IOException e)
		{
			Debug.Joshua.print (e.getMessage ( ));

		}
	}


	/**
	 * Flag owner has been changed
	 * 
	 * @param flagId
	 * @param newOwnerId
	 */
	private void eventFlagOwnerChanged (int flagId, int newOwnerId)
	{
		Flag flag = FlagRegistry.getFlag (flagId);
		flag.setOwner (PlayerRegistry.getPlayer (newOwnerId));
	}


	/**
	 * State of flag changed - flag can be occupied, unoccupied or attecked
	 * 
	 * @param flagId
	 * @param state
	 */
	private void eventFlagStateChanged (int flagId, byte state)
	{
		Flag flag = FlagRegistry.getFlag (flagId);
		flag.setState (state);
	}


	private void eventUnderAttack (int battleId)
	{
		//
		// not needed - id will be sent trought msg
		//
		// synchronized (this.listBattleId)
		// {
		// listBattleId.add(battleId);
		// }

		ExploreMode eMode = NetworkResources.getClient ( ).getExploreMode ( );

		Message m = new Message ( );
		m.what = ExploreMode.FLAG_UNDER_ATTACK;
		m.arg1 = battleId;

		Debug.Joshua.print ("EventUnderAttack battleId=" + battleId);

		eMode.handler.sendMessage (m);
	}


	/**
	 * 
	 * @param idAttacker
	 * @param idFlag
	 * @param attHover
	 * @param attTank
	 * @param attArtillery
	 * @param defHover
	 * @param defTank
	 * @param defArtillery
	 */
	private void dataFlagBatle (int idAttacker, int idFlag, short attHover, short attTank, short attArtillery,
			short defHover, short defTank, short defArtillery)
	{
		FlagBattle dfb = new FlagBattle (idAttacker, idFlag);
		dfb.attHover = attHover;
		dfb.attTank = attTank;
		dfb.attArtillery = attArtillery;

		dfb.defHover = defHover;
		dfb.defTank = defTank;
		dfb.defArtillery = defArtillery;

		this.listDataFlagBattle.add (dfb);

		//
		// notify thread that are waiting for data
		//
		synchronized (this.MonitorDataFlagBattle)
		{
			this.MonitorDataFlagBattle.notify ( );
		}

	}


	private void eventBattleStarted (int battleId, int tcpPort)
	{
		EventBattleStarted ebs = new EventBattleStarted (battleId, tcpPort);

		synchronized (this.listEventBattleStarted)
		{
			this.listEventBattleStarted.add (ebs);
		}

		synchronized (this.MonitorEventBattleStarted)
		{
			this.MonitorEventBattleStarted.notify ( );
		}

	}


	/**
	 * 
	 * @param flagId
	 * @param unit1
	 * @param unit2
	 * @param unit3
	 */
	private void eventFlagUnitsChanged (int flagId, short unit1, short unit2, short unit3)
	{
		//
		// sets num of units
		//
		Flag flag = FlagRegistry.getFlag (flagId);
		flag.setNumHovercraft (unit1);
		flag.setNumTanks (unit2);
		flag.setNumArtillery (unit3);
	}


	private void eventFieldExplored (short tile_x, short tile_y, byte field_i)
	{
		FlagRegistry.getFogOfWar ( ).clearField (tile_x, tile_y, field_i);
	}


	/**
	 * Some activity has requested flag units trought sender thread.
	 * 
	 * @param flagId
	 * @param unit1
	 * @param unit2
	 * @param unit3
	 */
	private void flagUnits (int flagId, short unit1, short unit2, short unit3)
	{
		//
		// sets num of units
		//
		Flag flag = FlagRegistry.getFlag (flagId);

		flag.setNumHovercraft (unit1);
		flag.setNumTanks (unit2);
		flag.setNumArtillery (unit3);

		flag.unitsUpdated = true;

	}


	/**
	 * 
	 * @param playerId
	 * @param name
	 * @param unit1
	 * @param unit2
	 * @param unit3
	 */
	private void playerProperties (int playerId, String name, short unit1, short unit2, short unit3)
	{
		ExplorePlayer ePlayer = PlayerRegistry.getPlayer (playerId);
		ePlayer.setName (name);

		Commander commander = CommanderRegistry.getCommander (ePlayer);
		commander.setHovercraftCount (unit1);
		commander.setTankCount (unit2);
		commander.setArtilleryCount (unit3);

	}


	public void stopRunning ( )
	{
		this.running = false;
	}

	//
	// Waiters for events
	// This stops the waiting thread until event or data is received
	//

	//
	// BATTLE START EVENT listener
	//
	private Object							MonitorEventBattleStarted	= new Object ( );
	LinkedList<EventBattleStarted>	listEventBattleStarted		= new LinkedList<EventBattleStarted> ( );


	public EventBattleStarted waitForEventBattleStarted ( )
	{
		EventBattleStarted ebs = null;
		synchronized (this.MonitorEventBattleStarted)
		{

			try
			{
				this.MonitorEventBattleStarted.wait (35000);
				synchronized (this.listEventBattleStarted)
				{
					if (this.listEventBattleStarted.size ( ) > 0)
					{
						ebs = this.listEventBattleStarted.remove ( );
					}
				}

			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				Debug.Joshua.print (e.getMessage ( ));
			}

		}

		return ebs;
	}

	//
	// DATA FLAG BATTLE listener
	//

	private Object				MonitorDataFlagBattle	= new Object ( );
	LinkedList<FlagBattle>	listDataFlagBattle		= new LinkedList<FlagBattle> ( );


	public FlagBattle waitForDataFlagBattle ( )
	{
		FlagBattle dfb = null;
		synchronized (this.MonitorDataFlagBattle)
		{

			try
			{
				this.MonitorDataFlagBattle.wait ( );
				synchronized (this.listDataFlagBattle)
				{
					if (this.listDataFlagBattle.size ( ) > 0)
					{
						dfb = this.listDataFlagBattle.remove ( );
					}
				}

			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				Debug.Joshua.print (e.getMessage ( ));
			}

		}

		return dfb;
	}

	LinkedList<Integer>	listBattleId	= new LinkedList<Integer> ( );


	public int getBattleId ( )
	{
		if (this.listBattleId.size ( ) > 0)
		{
			return this.listBattleId.remove ( );
		}
		else
		{
			return -1;
		}
	}

}
