package iu.android.comm;

import iu.android.engine.CommanderRegistry;
import iu.android.engine.FlagRegistry;
import iu.android.engine.PlayerRegistry;
import iu.android.explore.Commander;
import iu.android.explore.Flag;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;


public class BundleUtils
{
	private static final String	FlagOwner		= "flag.owner";
	private static final String	FlagId			= "flag.id";
	private static final String	FlagHover		= "flag.hover";
	private static final String	FlagTank			= "flag.tank";
	private static final String	FlagArtilery	= "flag.artilery";
	private static final String	FlagLocation	= "flag.location";

	private static final String	PlayerId			= "cmd.owner";
	private static final String	CmdHover			= "cmd.hover";
	private static final String	CmdTank			= "cmd.tank";
	private static final String	CmdArtilery		= "cmd.artilery";

	public static final String		GwLat				= "gw.lat";
	public static final String		GwLon				= "gw.lon";
	public static final String		GwHeight			= "gw.height";
	public static final String		GwWidth			= "gw.width";
	public static final String		GwFowSizePow	= "gw.fowSizePow";

	public static final String		Ntf				= "ntf";
	public static final String		NtfTTL			= "ntf.ttl";
	private static final String 	TCPPort 		= "tcp.port";
	


	/**
	 * Put a Flag to this Intent's extras
	 * 
	 * @param flag
	 */
	public static void putFlag (final Flag flag, final Bundle extras)
	{
		extras.putInt (BundleUtils.FlagId, new Integer (flag.getFlagId ( )));
//		extras.putString (BundleUtils.FlagOwner, flag.getOwner ( ).getName ( ));
//		extras.putInteger (BundleUtils.FlagId, new Integer (flag.getFlagId ( )));
//		extras.putInteger (BundleUtils.FlagHover, new Integer (flag.getNumHovercraft ( )));
//		extras.putInteger (BundleUtils.FlagTank, new Integer (flag.getNumTanks ( )));
//		extras.putInteger (BundleUtils.FlagArtilery, new Integer (flag.getNumArtillery ( )));
//		extras.putParcelable (BundleUtils.FlagLocation, flag.getLocation ( ));
	}


	/**
	 * Retrieve a Flag from extras
	 * 
	 * @return the flag that was put last or <code>null</code>
	 */
	public static Flag getFlag (final Bundle extras)
	{
//		int id, hover, tank, artilery;
//		String name;
//		Location location;

		if (!extras.containsKey (BundleUtils.FlagId))
		{
			throw new IllegalArgumentException ("Flag is not in the bundle");
		}
		else
		{
			int id = extras.getInt (BundleUtils.FlagId);
//			hover = extras.getInteger (BundleUtils.FlagHover).intValue ( );
//			tank = extras.getInteger (BundleUtils.FlagTank).intValue ( );
//			artilery = extras.getInteger (BundleUtils.FlagArtilery).intValue ( );
//			name = extras.getString (BundleUtils.FlagOwner);
//			location = (Location) extras.getParcelable (BundleUtils.FlagLocation);
//
//			ExplorePlayer player = PlayerRegistry.getPlayer (name);
//
//			return new Flag (location, player, id, hover, tank, artilery);
			
			Flag flag = FlagRegistry.getFlag(id);
			
			return flag;
		}
	}


	/**
	 * Put a Commander to this Intent's extras
	 * 
	 * @param commander
	 */
	public static void putCommander (final Commander commander, final Bundle extras)
	{
		
		extras.putInt (BundleUtils.PlayerId, commander.getPlayer ( ).getId());
//		extras.putInteger (BundleUtils.CmdHover, new Integer (commander.getHovercraftCount ( )));
//		extras.putInteger (BundleUtils.CmdTank, new Integer (commander.getTankCount ( )));
//		extras.putInteger (BundleUtils.CmdArtilery, new Integer (commander.getArtilleryCount ( )));
	}


	/**
	 * Retrieve a Flag from extras
	 * 
	 * @return the flag that was put last or <code>null</code>
	 */
	public static Commander getCommander (final Bundle extras)
	{
		Commander commander;
		int hover, tank, artilery;
		String name;

		if (!extras.containsKey (BundleUtils.PlayerId))
		{
			throw new IllegalArgumentException ("Commander is not in the bundle");
		}

//		hover = extras.getInteger (BundleUtils.CmdHover).intValue ( );
//		tank = extras.getInteger (BundleUtils.CmdTank).intValue ( );
//		artilery = extras.getInteger (BundleUtils.CmdArtilery).intValue ( );
		int playerId = extras.getInt (BundleUtils.PlayerId);

//		commander = new Commander (name, hover, tank, artilery);
//		commander.setPlayer (PlayerRegistry.getPlayer (name));
//		return commander;
		return CommanderRegistry.getCommander(PlayerRegistry.getPlayer(playerId));
	}
	
	public static void putTCPPort (int port, final Bundle extras)
	{
		extras.putInt(BundleUtils.TCPPort, port);	
	}
	
	public static int getTCPPort (final Bundle extras)
	{
		if (!extras.containsKey (BundleUtils.TCPPort))
		{
			throw new IllegalArgumentException ("TCP port is not in the bundle");
		}
		else
		{
			return extras.getInt(BundleUtils.TCPPort);
		}
	}


	public static void putGameWorld (final GameWorld world, final Intent intent)
	{
		intent.putExtra (BundleUtils.GwLat, 		 new Double (world.minLocation ( ).getLatitude ( )));
		intent.putExtra (BundleUtils.GwLon, 		 new Double (world.minLocation ( ).getLongitude ( )));
		intent.putExtra (BundleUtils.GwHeight, 	 new Double (world.height ( )));
		intent.putExtra (BundleUtils.GwWidth, 		 new Double (world.width ( )));
		intent.putExtra (BundleUtils.GwFowSizePow, new Byte ((byte) world.tileCountPow));
	}


	public static GameWorld getGameWorld (final Intent intent)
	{
		GameWorld world;
		double dx, dy, h, w;
		int fowTileCountPower;
		
		Bundle extras = intent.getExtras ( );

		if (!extras.containsKey (BundleUtils.GwLat))
		{
			throw new IllegalArgumentException ("GameWorld is not in the bundle");
		}
		else
		{
			dx = extras.getDouble (BundleUtils.GwLat);
			dy = extras.getDouble (BundleUtils.GwLon);
			h = extras.getDouble (BundleUtils.GwHeight);
			w = extras.getDouble (BundleUtils.GwWidth);
			fowTileCountPower = extras.getByte (BundleUtils.GwFowSizePow);

			world = new GameWorld (dx, dy, h, w, fowTileCountPower);
			return world;
		}
	}


	public static void putNotification (final Notification notification, final Bundle extras)
	{
		extras.putParcelable (BundleUtils.Ntf, notification);
	}


	public static Notification getNotification (final Bundle extras)
	{
		Parcelable p = extras.getParcelable (BundleUtils.Ntf);
		if (p == null)
		{
			throw new IllegalArgumentException ("Notification is not in the bundle");
		}
		else
		{
			return (Notification) p;
		}
	}


	public static void putNotificationTTL (final long ttl, final Bundle extras)
	{
		extras.putLong (BundleUtils.NtfTTL, new Long (ttl));
	}


	public static long getNotificationTTL (final Bundle extras)
	{
		Long l = extras.getLong (BundleUtils.NtfTTL);
		if (l == null)
		{
			throw new IllegalArgumentException ("Notification Time to Live is not in the bundle");
		}
		else
		{
			return l.longValue ( );
		}
	}
}
