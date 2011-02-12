package iu.android.unit;

import iu.android.engine.BattlePlayer;
import iu.android.engine.ai.Brain;
import iu.android.engine.ai.BrainGraphics;
import iu.android.engine.ai.DefaultBrain;
import iu.android.map.Map;

/**
 * Unit that uses a sprite (image) for display, and plays a message on receiving an order.
 */
public abstract class StandardUnit extends Unit
{
	BrainGraphics	brainGraphics	= null;

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param player
	 * @param map
	 * @param direction
	 * @param acceleration_force
	 * @param turning_speed
	 * @param x
	 * @param y
	 * @param radius
	 */
	public StandardUnit(UnitType type, BattlePlayer player, Map map, float direction, float acceleration_force, float turning_speed, float x, float y, float radius)
	{
		super(type, player, map, direction, acceleration_force, turning_speed, x, y, radius);
	}

	/**
	 * This will give the unit a <CODE>DefaultBrain</CODE>. If you want the unit to have a different brain override this method.
	 */
	@Override
	protected Brain createBrain()
	{
		return new DefaultBrain(this);
	}
}