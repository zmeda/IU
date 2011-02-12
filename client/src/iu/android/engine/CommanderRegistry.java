package iu.android.engine;

import iu.android.explore.Commander;
import iu.android.explore.ExplorePlayer;

import java.util.HashMap;
import java.util.Vector;

public class CommanderRegistry
{
	
	private static HashMap<ExplorePlayer, Commander>	commandersByPlayer	 = new HashMap<ExplorePlayer, Commander>();
	
	private static Vector<Commander>	      commandersById	= new Vector<Commander>();

	public static void addCommander(Commander commander)
	{
		CommanderRegistry.commandersByPlayer.put(commander.getPlayer(), commander);
	}


	public static Commander getCommander(ExplorePlayer ePlayer)
	{
		Commander commander = CommanderRegistry.commandersByPlayer.get(ePlayer);
		
		if (commander == null)
		{
			// create commander with this id
			//
			// its units are set to UNKNOWN
			//
			commander = new Commander(ePlayer.getName());
			CommanderRegistry.addCommander(commander);
		}
		
		return commander;
	}

	public static int getCommanderCount()
	{
		return CommanderRegistry.commandersById.size();
	}
}
