package iu.android.army;

import iu.android.engine.BattlePlayer;
import iu.android.engine.ai.AIPlayer;
import iu.android.explore.Player;
import iu.android.map.Map;
import iu.android.unit.ArtilleryDescription;
import iu.android.unit.HoverJetDescription;
import iu.android.unit.Squad;
import iu.android.unit.TankDescription;
import iu.android.unit.Unit;
import iu.android.unit.UnitDescription;

public class DefaultArmy implements PluggableArmy
{
	private UnitDescription[]	unitDescriptions	= {new HoverJetDescription ( ), new TankDescription ( ),
			new ArtilleryDescription ( )				};

	private int						hoverNum;
	private int						tankNum;
	private int						artNum;


	public DefaultArmy (final int hoverNum, final int tankNum, final int artNum)
	{
		this.hoverNum = hoverNum;
		this.tankNum = tankNum;
		this.artNum = artNum;
	}


	public String getArmyName ( )
	{
		return "Default Army";
	}


	// public AIPlayer newAI(int id)
	// {
	// return new AIPlayer((byte) id, "Computer");
	// }

	public AIPlayer newAI (final Player exPlayer)
	{
		return new AIPlayer (exPlayer);
	}


	/** Returns an array of UnitDescriptions, representing what this army can field. * */

	public UnitDescription[] getUnitDescriptions ( )
	{
		return this.unitDescriptions;
	}


	/**
	 * Returns an array of UnitDescriptions, with one description per Unit to be fielded. This is method is too
	 * help make choosing an army easier, so the player has something to start with.
	 */

	public UnitDescription[] attackConfiguration (final int num_points)
	{
		UnitDescription[] configuration = new UnitDescription[num_points];

		int confLen = configuration.length;
		for (int i = 0; i < confLen; i++)
		{
			int index = (2 * i) / confLen;
			configuration[i] = this.unitDescriptions[index];
		}
		return configuration;
	}


	public UnitDescription[] tacticalConfiguration (final int num_points)
	{
		UnitDescription[] configuration = new UnitDescription[num_points];

		int confLen = configuration.length;
		for (int i = 0; i < confLen; i++)
		{
			int index = (this.unitDescriptions.length * i) / confLen;
			configuration[i] = this.unitDescriptions[index];
		}
		return configuration;
	}


	public UnitDescription[] defenceConfiguration (final int num_points)
	{
		UnitDescription[] configuration = new UnitDescription[num_points];

		int confLen = configuration.length;
		for (int i = 0; i < confLen; i++)
		{
			int index = (2 * i) / confLen;
			configuration[i] = this.unitDescriptions[index];
		}
		return configuration;
	}


	private void makeSquad (final BattlePlayer player, final Map map, final int num,
			final UnitDescription description)
	{
		if (num > 0)
		{
			int numSquads = player.getNumSquads ( );
			Squad squad = new Squad (numSquads);
			player.addSquad (squad);
			for (int i = 0; i < num; i++)
			{
				Unit unit = description.newUnitOfType (player, map, -1, -1);
				player.addUnit (unit);
				squad.addUnit (unit);
			}

			map.addUnits (squad.getUnits ( ));
		}
	}


	/**
	 * Setup the squads and units on the specified player, using up the number of points given.
	 */
	public void createArmy (final BattlePlayer player, final Map map)
	{
		this.makeSquad (player, map, this.hoverNum, this.unitDescriptions[0]);
		this.makeSquad (player, map, this.tankNum, this.unitDescriptions[1]);
		this.makeSquad (player, map, this.artNum, this.unitDescriptions[2]);
	}
}