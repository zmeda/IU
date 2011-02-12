package iu.android.unit;

import iu.android.engine.BattlePlayer;
import iu.android.map.Map;

public class Tank extends StandardUnit
{
	private Weapon[]	weapons	= null;

	public Tank(BattlePlayer player, Map map, float x, float y)
	{
		super(Unit.HEAVY_UNIT, player, map, 0.0f, 20.0f, (float) Math.PI, x, y, 12/* , imageManager, soundManager */);
		this.weapons = new Weapon[]
		{ new Cannon(this) };
		this.setMaxSpeed(10.0f);

		//
		// The distance depends on the max speed.
		// So it needs to be set after max speed is.
		//
		// this.setNeighbourhoodDistance();
	}

	@Override
	public Weapon[] getWeapons()
	{
		return this.weapons;
	}

	// @Override
	@Override
	public String getName()
	{
		return "tank";
	}
}