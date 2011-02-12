package iu.android.engine;

import iu.android.unit.Unit;
import iu.android.unit.Vector2d;

import java.util.ArrayList;

public class Explosion
{
	private static ArrayList<Explosion>	explosions	= new ArrayList<Explosion>();

	private int	                        x;
	private int	                        y;
	private int	                        radius;
	// [] removed rectangle since it was not used anywhere
	private float	                    timeLeft;
	private float	                    duration;
	private int	                        strength;

	private Explosion()
	{
		this.timeLeft = 0;
		this.duration = 1.0f;
		this.strength = 2;
	}

	private void init(final int x_i, final int y_i, final int r_i, final float duration_i, final int strength_i)
	{
		this.x = x_i;
		this.y = y_i;
		this.radius = r_i;
		this.duration = duration_i;
		this.timeLeft = duration_i;
		this.strength = strength_i;
	}

	public static Explosion getNewExplosion(final int x, final int y, final int radius, final float duration, final int strength)
	{

		ArrayList<Explosion> explosionsVec = Explosion.explosions;

		if (explosionsVec.size() == 0)
		{
			final Explosion explosion = new Explosion();
			explosion.init(x, y, radius, duration, strength);
			return explosion;
		}
		final Explosion explosion = explosionsVec.get(explosionsVec.size() - 1);
		explosionsVec.remove(explosionsVec.size() - 1);
		explosion.init(x, y, radius, duration, strength);

		return explosion;
	}

	/**
	 * Adds the explosion to the pool
	 */
	public void dispose()
	{
		Explosion.explosions.add(this);
	}

	public float getDuration()
	{
		return this.duration;
	}

	public float getTimeLeft()
	{
		return this.timeLeft;
	}

	public int getX()
	{
		return this.x;
	}

	public int getY()
	{
		return this.y;
	}

	public float getRadius()
	{
		return this.radius;
	}

	public boolean isAffecting(final Unit unit)
	{
		// final Circle bounds = unit.getBounds();
		return unit.getBounds().isColliding(this.x, this.y, this.radius);
	}

	public void displace(final Unit unit)
	{
		unit.takeDamageFrom(this);
		final Vector2d pos = unit.getPosition();
		float dx = pos.x - this.x, dy = pos.y - this.y;

		final float distSq = dx * dx + dy * dy;

		int r = this.radius;

		if (distSq < ((r * r) >> 2))
		{
			return;
		}

		// final float dist = Math.sqrt(distSq);
		// dx /= dist;
		// dy /= dist;
		// unit.applyForce(r * this.getStrength() * dx, r * this.getStrength() * dy);

		final float distInvXstrength = this.getStrength() / (float) Math.sqrt(distSq);
		dx *= distInvXstrength;
		dy *= distInvXstrength;

		unit.applyForce(r * dx, r * dy);
	}

	public int getStrength()
	{
		return this.strength;
	}

	/** Has this object moved since it's last redraw? */
	public boolean hasMoved()
	{
		return true;
	}

	public void update(final float dt)
	{
		this.timeLeft -= dt;
	}

	public boolean hasFinished()
	{
		return this.timeLeft < 0.0;
	}
}