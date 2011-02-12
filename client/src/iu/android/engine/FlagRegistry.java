package iu.android.engine;

import iu.android.explore.Flag;
import iu.android.explore.FogOfWar;

import java.util.Vector;

public class FlagRegistry
{
	private static Vector<Flag>	flagsById	= new Vector<Flag>();
	private static FogOfWar	    fogOfWar	= null;

	public static void setFogOfWar(FogOfWar fow)
	{
		FlagRegistry.fogOfWar = fow;
	}
	
	public static FogOfWar getFogOfWar ( ){
		return fogOfWar;
	}

	public static void addFlag(Flag flag)
	{
		int id = flag.getFlagId();

		if (FlagRegistry.flagsById.size() <= id)
		{
			FlagRegistry.flagsById.setSize(id + 1);
		}

		FlagRegistry.flagsById.set(id, flag);

		FlagRegistry.fogOfWar.addFlag(flag);
	}

	public static Flag getFlag(int id)
	{
		return FlagRegistry.flagsById.get(id);
	}

	public static int getPlayerCount()
	{
		return FlagRegistry.flagsById.size();
	}
}
