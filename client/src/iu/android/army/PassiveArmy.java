package iu.android.army;

import iu.android.engine.BattlePlayer;
import iu.android.engine.ai.AIPlayer;
import iu.android.engine.ai.PassivePlayer;
import iu.android.explore.Player;
import iu.android.map.Map;
import iu.android.unit.SheepDescription;
import iu.android.unit.UnitDescription;

public class PassiveArmy implements PluggableArmy
{
	private UnitDescription[]	unitDescriptions	= {new SheepDescription ( )};


	public String getArmyName ( )
	{
		return "Passive Army";
	}


	// public AIPlayer newAI(int id)
	// {
	// return new PassivePlayer((byte) id, "Passive");
	// }

	public AIPlayer newAI (final Player exPlayer)
	{
		return new PassivePlayer (exPlayer);
	}


	/** Returns an array of UnitDescriptions, representing what this army can field. * */

	public UnitDescription[] getUnitDescriptions ( )
	{
		return this.unitDescriptions;
	}


	/**
	 * Returns an array of UnitDescriptions, with one description per Unit to be fielded. This method is to
	 * help make choosing an army easier, so the player has something to start with.
	 */

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


	public UnitDescription[] attackConfiguration (final int num_points)
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
			int index = (this.unitDescriptions.length * i) / confLen;

			configuration[i] = this.unitDescriptions[index];
		}
		return configuration;
	}


	public void createArmy (final BattlePlayer player, final Map map)
	{

	}
}