package iu.server.battle;

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
public class BattleServerPlayer
{
	//
	// The number of units, for each known type
	//
	private short			numberOfHoverjets = -1;
	private short			numberOfTanks		= -1;
	private short			numberOfArtillery = -1;
	
	//
	// Hold the units' positions. Addressing is [UnitID] [0 = X, 1 = Y]
	//
	private int[][]		unitPositions	= null;

	private boolean		areUnitsSetUp	= false;

	private RemoteClient	remoteClient	= null;
	
	private int				color				= 0;
	
	private String			name				= "### no name ###";
	
	private int				nID				= -1;
	

	/**
	 * Constructor
	 */
	public BattleServerPlayer ( )
	{
		// Nothing to do!
	}


	/**
	 * Sets player's ID.-
	 * 
	 * @param nID
	 */
	public void setID (int nID)
	{
		this.nID = nID;
	}


	/**
	 * Returns this player's ID.-
	 * 
	 * @return
	 */
	public int getID ( )
	{
		return this.nID;
	}


	public void setRemoteClient (RemoteClient remoteClient)
	{
		this.remoteClient = remoteClient;
	}


	public RemoteClient getClient ( )
	{
		return this.remoteClient;
	}


	public int getColor ( )
	{
		return this.color;
	}


	public void setColor (int color)
	{
		this.color = color;
	}


	public String getName ( )
	{
		return this.name;
	}


	public void setName (String name)
	{
		this.name = name;
	}


	/**
	 * Sets the number of units this player will have. It will fail if called after units were set up.
	 * 
	 * @return
	 */
	public void setNumberOfUnits (short numberOfHoverjet, short numberOfTanks, short numberOfArtillery)
	{
		int numberOfUnits = numberOfHoverjet + numberOfTanks + numberOfArtillery;
		
		//
		// Check that units haven't been set up yet
		//
		if (!this.areUnitsSetUp)
		{
			//
			// Save the number of units of each type
			//
			this.numberOfHoverjets = numberOfHoverjet;
			this.numberOfTanks     = numberOfTanks;
			this.numberOfArtillery = numberOfArtillery;
			
			//
			// Create the array that will hold the units' coordinates
			//
			this.unitPositions = new int[numberOfUnits][2];
		}
		else
		{
			System.err.print ("*** WARNING: Trying to set up units, that were alredy created!");
		}
	}


	/**
	 * Returns the number of units this player has.
	 * 
	 * @return
	 */
	public int getNumberOfUnits ( )
	{
		return (this.unitPositions.length);
	}


	/**
	 * Generate random coordinates for each unit based on the parameters.
	 * 
	 * @param nBaseX
	 * @param nBaseY
	 */
	public void setupUnits (int nBaseX, int nBaseY)
	{
		for (int i = 0; i < this.unitPositions.length; i++)
		{
			//
			// The X component of this coordinate
			//
			this.unitPositions[i][0] = 10 + nBaseX + (int) (300.0 * java.lang.Math.random ( ));
			//
			// The Y component of this coordinate
			//
			this.unitPositions[i][1] = 10 + nBaseY + (int) (300.0 * java.lang.Math.random ( ));
		}
		//
		// The units were set up. Set the flag to true.
		//
		this.areUnitsSetUp = true;
	}


	/**
	 * 
	 * @param nUnitID
	 * @return
	 */
	public int getUnitX (int nUnitID)
	{
		if (this.areUnitsSetUp)
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
	public int getUnitY (int nUnitID)
	{
		if (this.areUnitsSetUp)
		{
			return (this.unitPositions[nUnitID][1]);
		}
		else
		{
			return -1;
		}
	}


	public short getNumberOfHoverjets ( )
	{
		return this.numberOfHoverjets;
	}


	public void setNumberOfHoverjets (short numberOfHoverjets)
	{
		this.numberOfHoverjets = numberOfHoverjets;
	}


	public short getNumberOfTanks ( )
	{
		return this.numberOfTanks;
	}


	public void setNumberOfTanks (short numberOfTanks)
	{
		this.numberOfTanks = numberOfTanks;
	}


	public short getNumberOfArtillery ( )
	{
		return this.numberOfArtillery;
	}


	public void setNumberOfArtillery (short numberOfArtillery)
	{
		this.numberOfArtillery = numberOfArtillery;
	}
}
