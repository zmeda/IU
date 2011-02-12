package iu.android.explore;

import iu.android.engine.CommanderRegistry;
import iu.android.engine.PlayerRegistry;

public class Commander
{
	public static final int	UNKNOWN_NUMBER	= -1;

	// Commander location
	// Location mapLocation;
	// Point mapPoint;
	//
	// Location lastLocation;
	// Point lastPoint;
	//
	// Point frontPoint;

	// Distance the player can manage the flags
	private static double	viewDistance	= 0;

	// Nearest flag to our commander
	Flag							nearestFlag		= null;

	// Commander units
	private int					hovercraftCount;
	private int					tankCount;
	private int					artilleryCount;
	ExplorePlayer				player;


	public Commander (final String name)
	{
		this (name, Commander.UNKNOWN_NUMBER, Commander.UNKNOWN_NUMBER, Commander.UNKNOWN_NUMBER);
	}


	public Commander (final String name, final int numH, final int numT, final int numA)
	{
		this.hovercraftCount = numH;
		this.tankCount = numT;
		this.artilleryCount = numA;

		this.player = PlayerRegistry.getPlayer (name);
		CommanderRegistry.addCommander (this);
		this.player.setActive (true);
		// Player is blue
		this.player.setColor (0xff0000ff);
	}


	//
	// Unit count getters and setters
	//

	public int getTotalNumberOfUnits ( )
	{
		return (this.hovercraftCount + this.tankCount + this.artilleryCount);
	}


	public short getHovercraftCount ( )
	{
		return (short) this.hovercraftCount;
	}


	public void setHovercraftCount (final int hovercraftCount)
	{
		this.hovercraftCount = hovercraftCount;
	}


	public short getTankCount ( )
	{
		return (short) this.tankCount;
	}


	public void setTankCount (final int tankCount)
	{
		this.tankCount = tankCount;
	}


	public short getArtilleryCount ( )
	{
		return (short) this.artilleryCount;
	}


	public void setArtilleryCount (final int artilleryCount)
	{
		this.artilleryCount = artilleryCount;
	}


	@Override
	public String toString ( )
	{
		return "Flag (" + this.hovercraftCount + ", " + this.tankCount + ", " + this.artilleryCount + ").";

	}


	public ExplorePlayer getPlayer ( )
	{
		return this.player;
	}


	public void setPlayer (final ExplorePlayer player)
	{
		this.player = player;
	}


	/**
	 * @return the viewDistance
	 */
	public static double getViewDistance ( )
	{
		return Commander.viewDistance;
	}


	/**
	 * @param viewDistance
	 *           the viewDistance to set
	 */
	public static void setViewDistance (final double viewDistance)
	{
		Commander.viewDistance = viewDistance;
	}
}
