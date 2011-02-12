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
import iu.android.unit.Unit;
import iu.android.unit.UnitDescription;

public class TestUnitDescription implements UnitDescription
{
	/**
	 * How much this unit is "worth".
	 */
	public int getPointsValue()
	{
		return 1;
	}

	/**
	 * Image for display to user.
	 * 
	 * TODO: This method is not used so far ...
	 * 
	 * public java.awt.Image getIcon ( ) { return null; }
	 */

	/**
	 * Factory method, to allow a unit to be created anonymously.
	 */
	public Unit newUnitOfType(BattlePlayer player, Map map, float x, float y)
	{
		return new TestUnit(player, map, x, y);
	}
}
