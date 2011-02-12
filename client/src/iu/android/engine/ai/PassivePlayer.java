package iu.android.engine.ai;

import iu.android.battle.BattleEngine;
import iu.android.explore.ExplorePlayer;
import iu.android.explore.Player;
import iu.android.map.Map;
import iu.android.map.Tile;
import iu.android.order.MoveOrder;
import iu.android.unit.Unit;

/** The player who controls passive units. This player cannot win.* */
public class PassivePlayer extends AIPlayer
{
	private int	currentUnit	= 0;
	private Map	map	        = null;

	public PassivePlayer(Player exPlayer)
	{
		super(exPlayer, false);
	} 

	public void setMap(Map m)
	{
		this.map = m;
	}

	@Override
	public void dispatchOrders(BattleEngine game)
	{

		// If ended then start from the first one
		if (this.currentUnit >= this.getNumUnits())
		{
			this.currentUnit = 0;
		}

		Unit unit = null;
		int lookedAt = 0;

		// Look for a sheep with no orders that is not dead
		do
		{
			// We look a 1/8 sheep on every iteration
			if (lookedAt >= (this.getNumUnits() >> 3))
			{
				return;
			}

			if (this.currentUnit >= this.getNumUnits())
			{
				this.currentUnit = 0;
			}
			unit = this.getUnit(this.currentUnit);
			lookedAt++;
			this.currentUnit++;
		} while (unit.isDead() || (unit.getNumOrders() != 0));

		// short x = (short)(Math.random() * 2048.0);
		// short y = (short)(Math.random() * 2048.0);

		float old_x = unit.getPosition().x;
		float old_y = unit.getPosition().y;

		/*
		 * float new_x = 0.0; float new_y = 0.0;
		 * 
		 * boolean onGrass = false; boolean nearUnit = true;
		 * 
		 * while( !onGrass || nearUnit ) { float dx = ( Math.random() * 2.0 * Tile.size ) - Tile.size; float dy = ( Math.random() * 2.0 * Tile.size ) - Tile.size;
		 * 
		 * new_x = old_x + dx; new_y = old_y + dy;
		 * 
		 * onGrass = ( map.getTileAt( (int)new_x, (int)new_y ).getType() == Tile.GRASS ); nearUnit = ( map.getAreaAt( (int)new_x, (int)new_y ).getNumUnits() > 1 ); }
		 */

		// int minUnits = 1000; // big number to decrease from
		// int chosenX = 0;
		// int chosenY = 0;
		// boolean found = false;
		// Map thisMap = this.map;
		// int tileSize = Tile.Size;
		int randX = (int) (Math.random() * 3) - 1;
		int randY = (int) (Math.random() * 3) - 1;

		short chosenX = (short) ((int) unit.getPosition().x + randX * Tile.Size);
		short chosenY = (short) ((int) unit.getPosition().y + randY * Tile.Size);

		// search surrounding tiles and areas and move to the nearest grassy one with the least units
		// for (int x = (int) old_x - tileSize; x <= (int) old_x + tileSize; x += tileSize)
		// {
		// for (int y = (int) old_y - tileSize; y <= (int) old_y + tileSize; y += tileSize)
		// {
		// boolean onGrass = (thisMap.getTileAt(x, y).getType() == Tile.GRASS);
		// int numUnits = thisMap.getAreaAt(x, y).getNumUnits();
		//
		// if (onGrass && numUnits < minUnits)
		// {
		// chosenX = x;
		// chosenY = y;
		// minUnits = numUnits;
		// found = true;
		// }
		// else if (onGrass && numUnits == minUnits && (Math.random() < 0.3))
		// {
		// chosenX = x;
		// chosenY = y;
		// minUnits = numUnits;
		// found = true;
		// }
		// else if (!found && onGrass)
		// {
		// chosenX = x;
		// chosenY = y;
		// }
		// }
		// }

		MoveOrder moveOrder = MoveOrder.getNewMoveOrder(this.getID(), unit.getID(), chosenX, chosenY);
		unit.giveOrder(moveOrder);

	}

}