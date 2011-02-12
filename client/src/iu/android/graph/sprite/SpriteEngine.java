package iu.android.graph.sprite;

import iu.android.engine.BattlePlayer;
import iu.android.graph.ImageManager;
import iu.android.unit.Unit;
import iu.android.unit.Weapon;

/***********************************************************************************************************************************************************************************
 * IU 1.0b, a java real time strategy game
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 **********************************************************************************************************************************************************************************/

public class SpriteEngine
{

	public static void addNewPlayerSprites(BattlePlayer player)
	{
		if (player != null)
		{
			int unitCount = player.getNumUnits();

			for (int j = 0; j < unitCount; j++)
			{
				SpriteEngine.loadSpriteFor(player.getUnit(j));
			}
		}
	}

	private static void loadSpriteFor(Unit unit)
	{

		String name = unit.getName();

		// load graphics for weapons
		Weapon[] weapons = unit.getWeapons();

		int len = weapons.length;

		for (int i = 0; i < len; i++)
		{
			String projectileName = weapons[i].getProjectileName();
			ImageManager.getImage(projectileName);
		}

		UnitSprite sprite = new StandardUnitSprite(name);
		sprite.init(unit);

		// Sets the sprite of the unit
		unit.setUnitSprite(sprite);
	}
}