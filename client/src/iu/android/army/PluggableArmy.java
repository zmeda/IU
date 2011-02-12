package iu.android.army;

import iu.android.engine.BattlePlayer;
import iu.android.engine.ai.AIPlayer;
import iu.android.explore.Player;
import iu.android.map.Map;
import iu.android.unit.UnitDescription;

/**
 * A interface that provides a bunch of factory methods. The idea is that someone can implement a pluggable army, so that they can have custom (home-made) armies in their games.
 */

public interface PluggableArmy
{

	public String getArmyName();

	// public AIPlayer newAI(int id);
	public AIPlayer newAI(Player exPlayer);

	/** Returns an array of UnitDescriptions, representing what this army can field. * */

	public UnitDescription[] getUnitDescriptions();

	/**
	 * Returns an array of UnitDescriptions, with one description per Unit to be fielded. This is method is to help make choosing an army easier, so the player has something to
	 * start with.
	 */
	public UnitDescription[] attackConfiguration(int num_points);

	public UnitDescription[] tacticalConfiguration(int num_points);

	public UnitDescription[] defenceConfiguration(int num_points);

	/**
	 * Setup the squads and units on the specified player, using up the number of points given.
	 */

	public void createArmy(BattlePlayer player, Map map/* , int HoverNum, int tankNum, int artNum */);
}