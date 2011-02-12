package iu.server.explore.game;

import java.util.List;

public interface IExploreDao
{
	
	
	public void connect();
	public void disconnect();
	
	
	//
	// Flag related
	//
	
	/**
	 * Get the flag identified by id
	 * 
	 * @param id 
	 * @return a Flag or <code>null</code>;
	 */
	public Flag flagById(int id);
	
	/**
	 * Update the properties of a flag. Flag is identified by id
	 * 
	 * @param flag Flag to update
	 */
	public void updateFlag (Flag flag);
	
	
	//
	// Player related
	// 
	
	
	/**
	 * Get the Player identified by id
	 * 
	 * @param id
	 * @return a Player or <code>null</code>
	 */
	public Player playerById(int id);
	
	/**
	 * Update the properties of a Player. Player is identified by id
	 * 
	 * @param player Player to updated
	 */
	public void updatePlayer(Player player);
	
	
	/**
	 * Create a new user for the chosen World
	 * 
	 * @param name username
	 * @param password 
	 * @param world
	 * @return
	 */
	public Player newPlayer (String name, String password, Game game);
	
	
	
	
	//
	// Fog-of-war related
	//
	
	
	/**
	 * What is visible to the Player
	 * 
	 * @param player 
	 * @return ExploredArea belonging to player or <code>null</code>
	 */
	public ExploredArea exploredArea (Player player);
	
	/**
	 * A list o players that see the Tile
	 * 
	 * @param tile
	 * @return A List or <code>null</code>
	 */
	public List<Player> whoSeesTile (Tile tile);
	
	
	/**
	 * Update fog-of-war for the Player according to his current location.
	 * 
	 * @param player
	 */
	public void updateFog (Player player);
}
