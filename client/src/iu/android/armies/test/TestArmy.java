package iu.android.armies.test;

/*************************************************************************************************************
 * IU 1.0b, a java realtime strategy game
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

import iu.android.Debug;
import iu.android.army.PluggableArmy;
import iu.android.engine.BattlePlayer;
import iu.android.engine.ai.AIPlayer;
import iu.android.explore.Player;
import iu.android.map.Map;
import iu.android.unit.Squad;
import iu.android.unit.Unit;
import iu.android.unit.UnitDescription;

/**
 * A interface that provides a bunch of factory methods. The idea is that someone can implement a pluggable army, so that they can have custom (home-made) armies in their games.
 */
public class TestArmy implements PluggableArmy
{

	private UnitDescription[]	unitDescriptions	=
	                                             { new TestUnitDescription(), new TestUnitDescription(), new TestUnitDescription() };

	/**
	 * 
	 */
	public String getArmyName()
	{
		return "Test Army";
	}

	/**
	 * 
	 */
	// public AIPlayer newAI(int id)
	// {
	// return new AIPlayer((byte) id, "Computer");
	// }
	public AIPlayer newAI(Player exPlayer)
	{
		return new AIPlayer(exPlayer);
	}

	/**
	 * Returns an array of UnitDescriptions, representing what this army can field.
	 */
	public UnitDescription[] getUnitDescriptions()
	{
		return this.unitDescriptions;
	}

	/**
	 * Returns an array of UnitDescriptions, with one description per Unit to be fielded. This is method is too help make choosing an army easier, so the player has something to
	 * start with.
	 */
	public UnitDescription[] attackConfiguration(int num_points)
	{
		UnitDescription[] configuration = new UnitDescription[num_points];

		int len = configuration.length;
		for (int i = 0; i < len; i++)
		{
			int index = (2 * i) / len;
			configuration[i] = this.unitDescriptions[index];
		}
		return configuration;
	}

	/**
	 * 
	 */
	public UnitDescription[] tacticalConfiguration(int num_points)
	{
		UnitDescription[] configuration = new UnitDescription[num_points];

		int len = configuration.length;
		for (int i = 0; i < len; i++)
		{
			int index = (this.unitDescriptions.length * i) / len;
			if (Debug.JOSHUA)
			{
				index = 0;
			}
			configuration[i] = this.unitDescriptions[index];
		}
		return configuration;
	}

	/**
	 * 
	 */
	public UnitDescription[] defenceConfiguration(int num_points)
	{
		UnitDescription[] configuration = new UnitDescription[num_points];
		for (int i = 0; i < configuration.length; i++)
		{
			int index = (2 * i) / configuration.length;
			configuration[i] = this.unitDescriptions[index];
		}
		return configuration;
	}

	/**
	 * 
	 * @param player
	 * @param map
	 * @param num
	 * @param description
	 */
	private void makeSquad(BattlePlayer player, Map map, int num, UnitDescription description)
	{
		int numSquads = player.getNumSquads();
		Squad squad = new Squad(numSquads);
		player.addSquad(squad);
		for (int i = 0; i < num; i++)
		{
			Unit unit = description.newUnitOfType(player, map, -1, -1);
			player.addUnit(unit);
			squad.addUnit(unit);
		}
	}

	/**
	 * Setup the squads and units on the specified player, using up the number of points given.
	 */
	public void createArmy(BattlePlayer player, Map map)
	{
		this.makeSquad(player, map, 7, this.unitDescriptions[0]);
		this.makeSquad(player, map, 5, this.unitDescriptions[1]);
		this.makeSquad(player, map, 3, this.unitDescriptions[2]);
	}
}
