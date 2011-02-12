package iu.android.unit;

import iu.android.engine.BattlePlayer;
import iu.android.map.Map;

public class Sheep extends StandardUnit
{
	private Weapon[]	weapons	= null;

	public Sheep(BattlePlayer player, Map map, float x, float y)
	{
		super(Unit.PASSIVE_UNIT, player, map, 0.0f, 10.0f, (float) Math.PI * 0.5f, x, y, 9/* , imageManager, soundManager */);
		this.weapons = new Weapon[]
		{};
		this.setMaxSpeed(3.0f);
		// this.setNeighbourhoodDistance(); // distance depends on the maxspeed so needs setting after max speed
		// is set
		// this.setHealth (10);
		this.health = 10;
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
		return "sheep";
	}
}