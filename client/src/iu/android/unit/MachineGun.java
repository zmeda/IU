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

package iu.android.unit;

import iu.android.battle.BattleThread;

public class MachineGun implements Weapon
{
	private int	       range	     = 100;
	private float	   time_left	 = 0.0f;
	private Unit	   unit	         = null;
	private BattleThread	gameThread	 = null;

	private int	       currentBarrel	= 0;

	private int	       heat	         = 0;

	public MachineGun(Unit unit)
	{
		this.unit = unit;
	}

	public String getProjectileName()
	{
		return "bullet";
	}

	public int getRange()
	{
		return this.range;
	}

	public int getMinimumRange()
	{
		return 0;
	}

	public boolean isDisabled()
	{
		return false;
	}

	public boolean isReady()
	{
		return this.time_left <= 0.0;
	}

	public float timeUntilReloaded()
	{
		return this.time_left;
	}

	public void integrate(float dt)
	{
		this.time_left -= dt;
		--this.heat;
	}

	public void fireOnPosition(int x, int y)
	{

		if (!this.isReady())
		{
			return;
		}

		Vector2d pos = this.unit.position;
		Circle bounds = this.unit.bounds;
		float dx = x - pos.x, dy = y - pos.y;
		float distSq = dx * dx + dy * dy;
		float dist = (float) Math.sqrt(distSq);

		Vector2d dir = this.unit.direction;

		dx = dir.x;
		dy = dir.y;

		float startPos_x = pos.x + dx * bounds.radius;
		float startPos_y = pos.y + dy * bounds.radius;

		float barrel_x = 0.5f * bounds.radius * dy;
		float barrel_y = -0.5f * bounds.radius * dx;

		if (this.currentBarrel >= 1)
		{
			barrel_x = -barrel_x;
			barrel_y = -barrel_y;
			this.currentBarrel = 0;

			this.time_left = 0.5f;

			this.heat += 20;
		}
		else
		{
			this.currentBarrel++;

			this.heat += 5;
		}

		if (this.heat > 40)
		{
			this.time_left += 0.2;
		}

		startPos_x += barrel_x;
		startPos_y += barrel_y;

		if (dist > this.range)
		{
			dist = this.range;
		}

		this.gameThread.addProjectile(new Projectile(this, startPos_x, startPos_y, pos.x + dx * dist + barrel_x, pos.y + dy * dist + barrel_y, 5, 0.2f, 1));

		// this.unit.applyForce (-dx, -dy);
		this.unit.force.x += -dx;
		this.unit.force.y += -dy;
	}

	public void register(BattleThread gThread)
	{
		this.gameThread = gThread;
	}

}