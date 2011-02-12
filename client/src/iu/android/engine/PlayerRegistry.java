package iu.android.engine;

import iu.android.explore.ExplorePlayer;

import java.util.HashMap;
import java.util.Iterator;

public class PlayerRegistry
{
	private static HashMap<String, ExplorePlayer>	explorePlayers			= new HashMap<String, ExplorePlayer> ( );

	// private static Vector<ExplorePlayer> explorePlayersById = new Vector<ExplorePlayer>();

	private static HashMap<Integer, ExplorePlayer>	explorePlayersById	= new HashMap<Integer, ExplorePlayer> ( );

	private static String									localPlayerName		= null;


	public static void addPlayer (final ExplorePlayer player)
	{
		PlayerRegistry.explorePlayers.put (player.getName ( ), player);

		PlayerRegistry.explorePlayersById.put (player.getId ( ), player);

		// Replaced vector with hashmap

		// int id = player.getId ( );
		// if (PlayerRegistry.explorePlayersById.size ( ) <= id)
		// {
		// PlayerRegistry.explorePlayersById.setSize (id + 1);
		// // PlayerRegistry.explorePlayersById.
		// }
		//
		// PlayerRegistry.explorePlayersById.set (id, player);
	}


	public static ExplorePlayer getPlayer (final String name)
	{
		return PlayerRegistry.explorePlayers.get (name);
	}


	public static ExplorePlayer getPlayer (final int id)
	{
		ExplorePlayer ep = PlayerRegistry.explorePlayersById.get (id);

		if (ep == null)
		{
			// TODO send command to server to receive player

			// create player with this id
			ep = new ExplorePlayer (id);
			PlayerRegistry.addPlayer (ep);

			// when server returns date, player will be updated
		}

		return PlayerRegistry.explorePlayersById.get (id);
	}


	public static Iterator<ExplorePlayer> getIterator ( )
	{
		return PlayerRegistry.explorePlayers.values ( ).iterator ( );
	}


	public static int getPlayerCount ( )
	{
		return PlayerRegistry.explorePlayersById.size ( );
	}


	public static void setLocalPlayer (String name)
	{
		PlayerRegistry.localPlayerName = name;
	}


	public static ExplorePlayer getLocalPlayer ( )
	{
		return PlayerRegistry.explorePlayers.get (PlayerRegistry.localPlayerName);
	}
}
