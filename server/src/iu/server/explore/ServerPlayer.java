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

/**
 * Implementation of a player from the server's point of view.
 */
public class ServerPlayer
{
	boolean	             bUnitsSet	     = false;
	//
	// Hold the units' positions. Addressing is [UnitID] [0 = X, 1 = Y]
	//
	int[][]	             unitPositions	 = new int[GameServer.INITIAL_NUMBER_OF_UNITS][2];

	/*
	 * FIXME: These two members were replaced by the two-dimensional array above. They should be deleted.
	 * 
	 * int [] unitX = new int [GameServer.INITIAL_NUMBER_OF_UNITS]; int [] unitY = new int [GameServer.INITIAL_NUMBER_OF_UNITS];
	 */

	private RemoteClient	remoteClient	= null;
	private int	         color	         = 0;
	private String	     name	         = "### no name ###";
	private int	         nID	         = -1;

	/**
	 * Constructor
	 */
	public ServerPlayer()
	{

	}

	/**
	 * Sets player's ID.-
	 * 
	 * @param nID
	 */
	public void setID(int nID)
	{
		this.nID = nID;
	}

	/**
	 * Returns this player's ID.-
	 * 
	 * @return
	 */
	public int getID()
	{
		return this.nID;
	}

	public void setRemoteClient(RemoteClient remoteClient)
	{
		this.remoteClient = remoteClient;
	}

	public RemoteClient getClient()
	{
		return this.remoteClient;
	}

	public int getColor()
	{
		return this.color;
	}

	public void setColor(int color)
	{
		this.color = color;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * 
	 * @return
	 */
	public int getNumUnits()
	{
		return (this.unitPositions.length);
	}

	public boolean areUnitsSetup()
	{
		return this.bUnitsSet;
	}

	/**
	 * 
	 * @param nBaseX
	 * @param nBaseY
	 */
	public void setupUnits(int nBaseX, int nBaseY)
	{
		for (int i = 0; i < this.unitPositions.length; i++)
		{
			//
			// The X component of this coordinate
			//
			this.unitPositions[i][0] = 10 + nBaseX + (int) (300.0 * java.lang.Math.random());
			//
			// The Y component of this coordinate
			//
			this.unitPositions[i][1] = 10 + nBaseY + (int) (300.0 * java.lang.Math.random());
		}
		//
		// The units were set up. Set the flag to true.
		//
		this.bUnitsSet = true;
	}

	/**
	 * 
	 * @param nUnitID
	 * @return
	 */
	public int getUnitX(int nUnitID)
	{
		if (this.bUnitsSet)
		{
			return (this.unitPositions[nUnitID][0]);
		}
		else
		{
			return -1;
		}
	}

	/**
	 * 
	 * @param nUnitID
	 * @return
	 */
	public int getUnitY(int nUnitID)
	{
		if (this.bUnitsSet)
		{
			return (this.unitPositions[nUnitID][1]);
		}
		else
		{
			return -1;
		}
	}
}
