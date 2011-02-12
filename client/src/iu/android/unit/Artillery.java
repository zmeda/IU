package iu.android.unit;

import iu.android.engine.BattlePlayer;
import iu.android.map.Map;

public class Artillery extends StandardUnit
{
	// private Weapon[] weapons = null;

	public Artillery(BattlePlayer player, Map map, float x, float y)
	{
		super(Unit.HEAVY_UNIT, player, map, 0.0f, 10.0f, (float) Math.PI * 0.5f, x, y, 24/* , imageManager, soundManager */);
		this.weapons = new Weapon[]
		{ new SiegeCannon(this) };

		this.shortestRange = this.weapons[0].getMinimumRange() * 0.8d;
		this.shortestRangeSqr = this.shortestRange * this.shortestRange;

		this.setMaxSpeed(5.0f);
		// this.setNeighbourhoodDistance(); // distance depends on the maxspeed so needs setting after max speed
		// is set
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
		return "artillery";
	}
}