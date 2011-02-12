package iu.android.explore;

import com.google.android.maps.GeoPoint;

import iu.android.engine.FlagRegistry;
import iu.android.network.explore.Protocol;
import android.location.Location;

public class Flag
{

	public static final int		NUM_OF_UNITS_UNKNOWN		= -1;

	// 
	// TODO: A class needs to be written for each of the post types
	//

	// Not discovered yet
	public static final byte	TYPE_FLAG_UNKNOWN			= 0;

	// Each barracks gives you a certain amount of manpower
	public static final byte	TYPE_BARRACKS				= 1;

	// Training facility enables you to train your soldiers
	public static final byte	TYPE_TRAINING_FACILITY	= 2;

	// Factory enables you to build vehicles and defense structures
	public static final byte	TYPE_FACTORY				= 3;

	// Research center enables to upgrade technology of weapons your forces use or the ability of your
	// factories
	public static final byte	TYPE_RESEARCH_CENTER		= 4;

	// ???
	public static final byte	TYPE_RADAR					= 5;

	private final int				flagId;
	final Location					location;
	final GeoPoint					point;
	byte								postType;
	ExplorePlayer					owner;
	
	int fovI;
	int fovJ;

	//
	// state of flag - can be attcked, occupied, unoccupied
	//
	private byte					state = Protocol.StateIdle;
	
	public static final byte	OCCUPIED						= 10;
	public static final byte	UNOCCUPIED					= 11;
	public static final byte	ATTACKED						= 12;

	// TODO - Use generics to make adding of new unit types possible
	public  int						hovercraftCount;
	public  int						tankCount;
	public  int						artilleryCount;

	public  boolean 				unitsUpdated;

	public Flag (final Location location, final ExplorePlayer player, final int flagId)
	{
		this (location, player, flagId, Flag.NUM_OF_UNITS_UNKNOWN, Flag.NUM_OF_UNITS_UNKNOWN,
				Flag.NUM_OF_UNITS_UNKNOWN);
	}


	public Flag (final Location location, final ExplorePlayer player, final int flagId, /* final byte flagType, */
			final int hovercraft, final int tanks, final int artilery)
	{
		this.flagId = flagId;
		this.location = location;
		this.owner = player;

		this.point = new GeoPoint ((int) (location.getLatitude ( ) * 1E6d),
											(int) (location.getLongitude ( ) * 1E6d));

		this.postType = Flag./* flagType */TYPE_FLAG_UNKNOWN;

		this.hovercraftCount = hovercraft;
		this.tankCount = tanks;
		this.artilleryCount = artilery;

		FlagRegistry.addFlag (this);
	}


	public void setFieldIdx (final int i, final int j)
	{
		this.fovI = i;
		this.fovJ = j;
	}


	public double getDistance (final Location loc)
	{
		return loc.distanceTo (this.location);
	}


	//
	// Unit number getters/setters
	//

	public int getNumHovercraft ( )
	{
		return this.hovercraftCount;
	}


	public void setNumHovercraft (final int numHovercraft)
	{
		this.hovercraftCount = numHovercraft;
	}


	public int getNumTanks ( )
	{
		return this.tankCount;
	}


	public void setNumTanks (final int numTanks)
	{
		this.tankCount = numTanks;
	}


	public int getNumArtillery ( )
	{
		return this.artilleryCount;
	}


	public void setNumArtillery (final int numArtillery)
	{
		this.artilleryCount = numArtillery;
	}


	@Override
	public String toString ( )
	{
		return "Flag [" + this.flagId + "](" + this.hovercraftCount + ", " + this.tankCount + ", "
				+ this.artilleryCount + ").";

	}


	public ExplorePlayer getOwner ( )
	{
		return this.owner;
	}


	public void setOwner (final ExplorePlayer owner)
	{
		this.owner = owner;
	}


	public int getFlagId ( )
	{
		return this.flagId;
	}


	public Location getLocation ( )
	{
		return this.location;
	}


	public byte getState ( )
	{
		return this.state;
	}


	public void setState (final byte state)
	{
		this.state = state;
	}
}