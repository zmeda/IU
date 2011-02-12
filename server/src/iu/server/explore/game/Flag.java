package iu.server.explore.game;

import java.util.List;

public class Flag
{
	
	private static final int		day						= 60 * 60 * 24;
	private static final double		hoverPerSecond			= 50.0d / Flag.day;
	private static final double		tankPerSecond			= 30.0d / Flag.day;
	private static final double		artilleryPerSecond		= 20.0d / Flag.day;
	private static final int 		maxHover = 6;
	private static final int 		maxTank = 3;
	private static final int 		maxArtillery = 2;

	public final Integer			id;
	public final World				world;
	public final Location			location;

	Player								owner;

	private short						hover;
	private short						tank;
	private short						artillery;

	// Percentage of units being built
	private transient double		hoverBuilt;
	private transient double		tankBuilt;
	private transient double		artilleryBuilt;


	public Flag (final int id, final World world, final Player owner, final Location location, final short hNum, final short tNum, final short aNum)
	{
		super ( );
		this.id = new Integer (id);
		this.world = world;
		this.owner = owner;
		this.location = location;
		this.hover = hNum;
		this.tank = tNum;
		this.artillery = aNum;

		// Put the flag to the correct Tile inside the World
		this.tile ( ).addFlag (this);

		if (this.owner.id.intValue ( ) != 0)
		{
			System.out.println ("Flag [" + this.id + "] '" + this.owner + "' (" + this.hover + "," + this.tank + "," + this.artillery + ")");
		}
	}


	public void setOwner (final Player owner)
	{
		this.owner = owner;
	}


	public Tile tile ( )
	{
		return this.location.tile (this.world);
	}


	public long field ( )
	{
		return this.location.field (this.world);
	}


	public short getHover ( )
	{
		return this.hover;
	}


	public void setHover (final short hover)
	{
		this.hover = hover;
	}


	public short getTank ( )
	{
		return this.tank;
	}


	public void setTank (final short tank)
	{
		this.tank = tank;
	}


	public short getArtillery ( )
	{
		return this.artillery;
	}


	public void setArtillery (final short artillery)
	{
		this.artillery = artillery;
	}


	@Override
	public int hashCode ( )
	{
		return this.id.intValue ( );
	}


	/**
	 * Simulate one tick
	 * 
	 * @param dt
	 *           in seconds
	 * 
	 * @param messages
	 *           Events that happen during integrate will be stored here
	 */
	public void integrate (final double dt, List<Message> messages)
	{
		boolean changed = false;
		
		// Build hover
		if (this.hover < maxHover) {
			this.hoverBuilt += (Flag.hoverPerSecond * dt);
			if (this.hoverBuilt > 1.00)
			{
				this.setHover ((short) (this.hover + 1));
				--this.hoverBuilt;
				changed = true;
				System.out.println("Hover Built");
			}
		}
		
		// Build tank
		if (this.tank < maxTank) {
			this.tankBuilt += (Flag.tankPerSecond * dt);
			if (this.tankBuilt > 1.00)
			{
				this.setTank ((short) (this.tank + 1));
				--this.tankBuilt;
				changed = true;
				System.out.println("Tank Built");
			}
		}
		
		// Build artillery
		if (this.artillery < maxArtillery) {
			this.artilleryBuilt += (Flag.artilleryPerSecond * dt);
			while (this.artilleryBuilt > 1.00)
			{
				this.setArtillery ((short) (this.artillery + 1));
				--this.artilleryBuilt;
				changed = true;
				System.out.println("Artillery Built");
			}
		}

		if (changed)
		{
			messages.add (DataFlagUnits.init(this, this.owner));
		}
	}


	/**
	 * @return the owner
	 */
	public Player getOwner ( )
	{
		return this.owner;
	}

	
	public boolean isOwnerAI ( ) {
		return this.owner.isAI ( );
	}

	@Override
	public String toString ( )
	{
		StringBuilder sb = new StringBuilder ( );
		sb.append ("Flag(T=");
		sb.append (this.tile ( ));
		sb.append (" F=");
		sb.append (Tile.fieldIndex (this.field ( )));
		sb.append (")");
		return sb.toString ( );
	}
}
