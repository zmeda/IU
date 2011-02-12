package iu.android.unit;

import iu.android.engine.BattlePlayer;
import iu.android.map.Map;

public class HoverJet extends StandardUnit
{
	private Weapon[]	weapons	= null;

	public HoverJet(BattlePlayer player, Map map, float x, float y)
	{
		super(Unit.LIGHT_UNIT, player, map, 0.0f, 25.0f, (float) Math.PI, x, y, 9);
		this.weapons = new Weapon[]
		{ new MachineGun(this) };
		this.setMaxSpeed(25.0f);
		this.setHovering(true);
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
		return "hoverjet";
	}
}