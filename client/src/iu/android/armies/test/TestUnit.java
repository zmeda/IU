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

import iu.android.engine.BattlePlayer;
import iu.android.map.Map;
import iu.android.unit.MachineGun;
import iu.android.unit.StandardUnit;
import iu.android.unit.Unit;
import iu.android.unit.Weapon;

/**
 * 
 */
public class TestUnit extends StandardUnit
{
	private Weapon[]	weapons	= null;

	public TestUnit(BattlePlayer player, Map map, float x, float y)
	{
		super(Unit.LIGHT_UNIT, player, map, 0.0f, 20.0f, (float) Math.PI, x, y, 9);
		this.setMaxSpeed(25.0f);
		this.setHovering(true);
		// this.setNeighbourhoodDistance(); // distance depends on the maxspeed so needs setting after max speed is
		// set

		Weapon[] myWeapons =
		{ new MachineGun(this) };
		this.weapons = myWeapons;
	}

	@Override
	public Weapon[] getWeapons()
	{
		return this.weapons;
	}
}
