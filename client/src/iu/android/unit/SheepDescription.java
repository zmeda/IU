package iu.android.unit;

import iu.android.engine.BattlePlayer;
import iu.android.map.Map;

public class SheepDescription implements UnitDescription
{

	/** How much this unit is "worth". * */

	public int getPointsValue()
	{
		return 1;
	}

	/** Factory method, to allow a unit to be created anonymously. * */

	public Unit newUnitOfType(BattlePlayer player, Map map, float x, float y)
	{
		return new Sheep(player, map, x, y);
	}

}