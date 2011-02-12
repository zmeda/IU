package iu.android.unit;

import android.graphics.Rect;

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

/**
 * 
 */
public class MovingBody
{

	static final int	MAX_SPEED	= 50;

	final Vector2d	 prev_position	= new Vector2d();
	final Vector2d	 position	   = new Vector2d();
	final Vector2d	 velocity	   = new Vector2d();
	final Vector2d	 force	      = new Vector2d();
	Circle	       bounds	      = null;

	final float	     radius;
	final float	     mass;
	final float	     oneOverMass;
	float	         maxSpeed;
	float	         maxSpeedSq;
	float	         friction;

	/**
	 * Constuctor
	 * 
	 * @param x
	 * @param y
	 * @param radius
	 * @param _mass
	 */
	public MovingBody(final float x, final float y, final float radius, final float _mass)
	{
		if (_mass <= 0.0)
		{
			throw new RuntimeException("Mass negative or zero");
		}

		this.prev_position.x = x;
		this.position.x = x;
		this.prev_position.y = y;
		this.position.y = y;
		this.radius = radius;
		this.mass = _mass;
		this.oneOverMass = 1.0f / this.mass;
		this.bounds = new Circle(x, y, radius);

		this.maxSpeed = 0;
		this.maxSpeedSq = 0;
		this.friction = 0;
	}

	// TODO - move the class into iu.android.engine.ai package for optimisation
	public final Vector2d getPosition()
	{
		return this.position;
	}

	// TODO - move the class into iu.android.engine.ai package for optimisation
	public void setPosition(final float x, final float y)
	{
		this.position.x = x;
		this.position.y = y;
	}

	// TODO - move the class into iu.android.engine.ai package for optimisation
	public final Vector2d getVelocity()
	{
		return this.velocity;
	}

	public final float getSpeed()
	{
		return (float) Math.sqrt(this.velocity.x * this.velocity.x + this.velocity.y * this.velocity.y);
	}

	/**
	 * Will be set to <STRONG>50 or <CODE>max_speed</CODE></STRONG>, whichever is lower.
	 */
	// Only used in constructors
	public void setMaxSpeed(final float max_speed)
	{
		this.maxSpeed = Math.min(max_speed, MovingBody.MAX_SPEED);
		this.maxSpeedSq = this.maxSpeed * this.maxSpeed;
	}

	public float getMaxSpeed()
	{
		return this.maxSpeed;
	}

	// public void setVelocity (final float vx, final float vy)
	// {
	// this.velocity.x = vx;
	// this.velocity.y = vy;
	// }

	public final void applyForce(final float fx, final float fy)
	{
		this.force.x += fx;
		this.force.y += fy;
	}

	// public final void setFriction (final float friction)
	// {
	// this.friction = friction;
	// }

	/**
	 * Has this body moved a <EM>noticeable</EM> amount recently? Checks whether the integer part of <CODE>prev_position</CODE> is different to the integral part of
	 * <CODE>position</CODE>
	 */
	public boolean hasMoved()
	{
		return (int) (this.prev_position.x) != (int) (this.position.x) || (int) (this.prev_position.y) != (int) (this.position.y);
	}


	/**
	 * Get the bounding circle.
	 */
	public final Circle getBounds()
	{
		return this.bounds;
	}

	public void doCollision(final MovingBody body)
	{
		final float dx = body.position.x - this.position.x;
		final float dy = body.position.y - this.position.y;
		final float dist = (float) Math.sqrt(dx * dx + dy * dy);
		final float distInv = 1.0f / dist;
		final float sin = dx * distInv, cos = dy * distInv;
		float seperatingForce = 10f * (dist - (this.bounds.radius + body.bounds.radius));
		// dampening for speed
		final float dvx = body.velocity.x - this.velocity.x;
		final float dvy = body.velocity.y - this.velocity.y;

		final float damping = dvx * dx + dvy * dy;

		seperatingForce += 0.1 * damping;

		final float fx = sin * seperatingForce, fy = cos * seperatingForce;

		this.force.x += fx;
		this.force.y += fy;
		body.force.x -= fx;
		body.force.y -= fy;
	}

	/**
	 * Set the position back to what it was at the previous time step.
	 */
	// Not used
	// public void reset ( )
	// {
	// this.position.setEqualTo (this.prev_position);
	// this.bounds.x = this.position.x;
	// this.bounds.y = this.position.y;
	// this.bounds.radius = this.radius;
	// }
	public void integrate(final float dt)
	{
		// this.prev_position.setEqualTo (this.position);
		this.prev_position.x = this.position.x;
		this.prev_position.y = this.position.y;

		final float dtTimesOneOverMass = dt * this.oneOverMass;

		final float frx = this.friction * this.velocity.x * dtTimesOneOverMass;
		final float fry = this.friction * this.velocity.y * dtTimesOneOverMass;

		this.position.x += dt * this.velocity.x;
		this.velocity.x += dtTimesOneOverMass * this.force.x;
		this.position.y += dt * this.velocity.y;
		this.velocity.y += dtTimesOneOverMass * this.force.y;

		final float vx = this.velocity.x;
		final float vy = this.velocity.y;

		this.velocity.x -= frx;
		this.velocity.y -= fry;

		// friction can never change the direction of velocity,
		// only decrease its size
		if ((vx > 0 && this.velocity.x < 0) || (vx < 0 && this.velocity.x > 0))
		{
			this.velocity.x = 0;
		}

		if ((vy > 0 && this.velocity.y < 0) || (vy < 0 && this.velocity.y > 0))
		{
			this.velocity.y = 0;
		}

		this.bounds.x = this.position.x;
		this.bounds.y = this.position.y;
		this.bounds.radius = this.radius;
		// this.force.zero ( );
		this.force.x = 0;
		this.force.y = 0;

		// final float speedSq = this.velocity.getDotProduct (this.velocity);
		final float speedSq = this.velocity.x * this.velocity.x + this.velocity.y * this.velocity.y;

		if (speedSq > this.maxSpeedSq)
		{
			// this.velocity.multiply (1.0 / Math.sqrt (speedSq));
			// this.velocity.multiply (this.maxSpeed);
			final float f = this.maxSpeed / (float) Math.sqrt(speedSq);
			this.velocity.x *= f;
			this.velocity.y *= f;
		}
	}

	/**
	 * Checking for presence inside rectangle
	 */
	public boolean isVisibleInRect(float left, float right, float top, float bottom)
	{
		final float r = this.bounds.radius;
		Vector2d pos = this.position;

		return (pos.x + r > left && pos.x - r < right && pos.y + r > top && pos.y - r < bottom);
	}

	/**
	 * Checking for presence inside rectangle
	 */
	public boolean isVisibleInRect(Rect rect)
	{
		final float r = this.bounds.radius;
		Vector2d pos = this.position;

		return (pos.x + r > rect.left && pos.x - r < rect.right && pos.y + r > rect.top && pos.y - r < rect.bottom);
	}

	/**
	 * Checking for presence inside x interval
	 */
	public boolean isVisibleInIntervalX(float left, float right)
	{
		final float r = this.bounds.radius;
		Vector2d pos = this.position;

		return (pos.x + r > left && pos.x - r < right);
	}

	/**
	 * Checking for presence inside y interval
	 */
	public boolean isVisibleInIntervalY(float top, float bottom)
	{
		final float r = this.bounds.radius;
		Vector2d pos = this.position;

		return (pos.y + r > top && pos.y - r < bottom);
	}
}
