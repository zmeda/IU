package iu.android.unit;

import iu.android.battle.BattleThread;

public class Cannon implements Weapon
{
	private int	       range	   = 100;
	private float	   time_left	= 0.0f;
	private Unit	   unit	       = null;
	private BattleThread	gameThread	= null;

	public Cannon(Unit unit)
	{
		this.unit = unit;
	}

	public String getProjectileName()
	{
		return "shell";
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

		this.gameThread.addProjectile(new Projectile(this, startPos_x, startPos_y, pos.x + dx * dist, pos.y + dy * dist, 20, 0.5f, 3));

		// this.unit.applyForce (-dx * 50, -dy * 50);
		this.unit.force.x += -dx * 50;
		this.unit.force.y += -dy * 50;

		this.time_left = 0.75f;
	}

	public void register(BattleThread gThread)
	{
		this.gameThread = gThread;
	}

}