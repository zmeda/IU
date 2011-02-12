package iu.android.unit;

import iu.android.engine.Explosion;
import iu.android.map.Map;

/**
 * To keeps things simple, a projectile causes an explosion upon landing, but otherwise will not affect anything else until that point. It is intended, at this stage, that
 * buildings will affect projectiles, but units will not.
 */

public class Projectile
{
	final float	start_x;
	final float	start_y;
	float	    x;
	float	    y;
	final float	target_x;
	final float	target_y;
	final float	duration;
	float	    timeTaken;	                   // FIXME more uniform API (timeLeft like in Explosion)
	boolean	    exploded;
	int	        explosionRadius/* = 20 */;
	float	    explosionDuration/* = 0.5 */;
	int	        strength/* = 2 */;
	Weapon	    weapon/* = null */;

	// [] removed rect, prev_x, prev_y, sin_dir, cos_dir since they were never used

	// TODO is using a private pool (like in Explosion) a better sollution for short lived objects
	public Projectile(final Weapon weapon, final float x, final float y, final float target_x, final float target_y, final int explosionRadius, final float explosionDuration,
	        final int strength)
	{
		this.x = x;
		this.start_x = x;
		this.target_x = target_x;

		this.y = y;
		this.start_y = y;
		this.target_y = target_y;

		this.explosionRadius = explosionRadius;
		this.explosionDuration = explosionDuration;
		this.strength = strength;
		this.weapon = weapon;

		final float dx = x - target_x;
		final float dy = y - target_y;

		// [] that 'divide by 100' thingy looks like projectile speed
		this.duration = (float) Math.sqrt(dx * dx + dy * dy) * 0.01f;
		this.exploded = false;
		this.timeTaken = 0;

	}

	public Weapon getWeapon()
	{
		return this.weapon;
	}

	public boolean hasExploded()
	{
		return this.exploded;
	}

	public float getX()
	{
		return this.x;
	}

	public float getY()
	{
		return this.y;
	}

	public void update(final float dt, final Map map)
	{
		final float dx = this.timeTaken / this.duration;
		final float oneMinusDx = 1.0f - dx;
		this.x = (this.start_x * oneMinusDx + this.target_x * dx);
		this.y = (this.start_y * oneMinusDx + this.target_y * dx);
		this.timeTaken += dt;
		if (this.timeTaken >= this.duration)
		{
			map.addExplosion(Explosion.getNewExplosion((int) this.target_x, (int) this.target_y, this.explosionRadius, this.explosionDuration, this.strength));
			this.exploded = true;
		}
	}

	public float getTimeLeft()
	{
		return this.duration - this.timeTaken;
	}

	public boolean hasMoved()
	{
		return true;
	}
}