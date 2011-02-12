package iu.android.unit;

import iu.android.engine.BattlePlayer;
import iu.android.map.Map;

public interface UnitDescription
{

	/** How much this unit is "worth". * */

	public int getPointsValue ( );


	/** Factory method, to allow a unit to be created anonymously. * */

	public Unit newUnitOfType (BattlePlayer player, Map map, float x, float y);

}