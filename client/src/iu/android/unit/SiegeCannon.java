package iu.android.unit;

import iu.android.battle.BattleThread;

public class SiegeCannon implements Weapon
{
	private int	       range	    = 250;
	private int	       minimumRange	= 150;

	private float	   time_left	= 0.0f;
	private Unit	   unit	        = null;
	private BattleThread	gameThread	= null;

	public SiegeCannon(Unit unit)
	{
		this.unit = unit;
	}

	public String getProjectileName()
	{
		return "largeshell";
	}

	public int getRange()
	{
		return this.range;
	}

	public int getMinimumRange()
	{
		return this.minimumRange;
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

		if (dist > this.range)
		{
			dist = this.range;
		}
		else if (dist < this.minimumRange)
		{
			dist = this.minimumRange;
		}

		this.gameThread.addProjectile(new Projectile(this, startPos_x, startPos_y, pos.x + dx * dist, pos.y + dy * dist, 35, 1, 4));

		// this.unit.applyForce (-dx * 100, -dy * 100);
		this.unit.force.x += -dx * 100;
		this.unit.force.y += -dy * 100;

		this.time_left = 8;
	}

	public void register(BattleThread gThread)
	{
		this.gameThread = gThread;
	}

}