package iu.android.unit;

import iu.android.Debug;
import iu.android.engine.BattlePlayer;
import iu.android.engine.Explosion;
import iu.android.engine.ai.Brain;
import iu.android.engine.ai.BrainGraphics;
import iu.android.graph.sprite.UnitSprite;
import iu.android.map.Map;
import iu.android.map.Tile;
import iu.android.order.Order;

import java.util.ArrayList;

public abstract class Unit extends MovingBody
{

	/** Type representing a troop unit. */
	public final static UnitType	TROOP_UNIT	       = new UnitType(0);
	/** Type representing a light vehicle unit. e.g. jeeps etc. * */
	public final static UnitType	LIGHT_UNIT	       = new UnitType(1);
	/** Type representing a medium weight vehicle unit. e.g. an apc or a troop tank. */
	public final static UnitType	MEDIUM_UNIT	       = new UnitType(2);
	/** Type representing a heavy unit. e.g. a tank. * */
	public final static UnitType	HEAVY_UNIT	       = new UnitType(3);
	/** Type representing a passive unit */
	public final static UnitType	PASSIVE_UNIT	   = new UnitType(4);

	protected Weapon[]	         weapons	           = null;
	double	                     shortestRange	       = 0;
	double	                     shortestRangeSqr	   = 0;

	private int	                 id;
	public float	             dir;

	public int sortedIndexX;
	public int sortedIndexY;
	// Search for nearest enemy every SEARCH_DELAY frames or if he dies
	private static final int	 SEARCH_DELAY	       = 10;
	private int	                 delayedSearchForEnemy	= 0;

	// matts
	float	                     prev_dir;
	float	                     acceleration_force;
	float	                     turning_speed;
	float	                     angular_velocity;
	boolean	                     move;
	float	                     target_speed;
	boolean	                     reverse;
	Vector2d	                 direction	           = new Vector2d();

	private final float	         air_resistance	       = 0.1f;
	private boolean	             hovering	           = false;
	private boolean	             braking	           = false;

	final BattlePlayer	         player;
	protected Brain	             brain	               = null;
	private BrainGraphics	     brainGraphics;

	private final UnitType	     type;
	private final Map	         map;

	private ArrayList<Order>	 orders	               = new ArrayList<Order>();

	private boolean	             moved	               = false;

	int	                         health	               = 100;
	boolean	                     isDead	               = false;

	Vector2d	                 newPos	               = new Vector2d();

	Squad	                     squad	               = null;

	Unit	                     nearestEnemy	       = null;

	UnitSprite	                 unitSprite	           = null;

	private static float massForType(final UnitType type)
	{
		if ((type == Unit.TROOP_UNIT) || (type == Unit.PASSIVE_UNIT))
		{
			return 0.1f;
		}
		if (type == Unit.LIGHT_UNIT)
		{
			return 0.3f;
		}
		if (type == Unit.MEDIUM_UNIT)
		{
			return 0.7f;
		}

		return 1.0f; // HEAVY_UNIT
	}

	public Unit(final UnitType type, final BattlePlayer player, final Map map, final float dir, final float acceleration_force, final float turning_speed, final float x,
	        final float y, final float radius)
	{
		super(x, y, radius, Unit.massForType(type));

		if (Debug.JOSHUA)
		{
			Debug.Joshua.println("Entering Unit Constructor");
		}

		this.acceleration_force = acceleration_force;
		this.dir = dir;
		this.turning_speed = turning_speed;
		this.move = false;
		this.calcDir();
		this.player = player;
		// this.brain = brain;
		this.type = type;
		this.map = map;

		this.angular_velocity = 0;
		this.target_speed = 0;
		this.reverse = false;
		this.brain = this.createBrain();
		this.brainGraphics = this.brain.getBrainGraphics();
		this.health = 100;

		if (Debug.JOSHUA)
		{
			Debug.Joshua.println("Exiting Unit Constructor");
		}
	}

	/**
	 * This must be overridden so the unit has a brain.
	 */
	protected abstract Brain createBrain();

	public void setID(final int id)
	{
		this.id = id;
	}

	public int getID()
	{
		return this.id;
	}

	/** Gets the unit type, either troop, light, medium or heavy. * */

	public UnitType getUnitType()
	{
		return this.type;
	}

	public Squad getSquad()
	{
		return this.squad;
	}

	// public void setSquad (final Squad squad)
	// {
	// this.squad = squad;
	// }

	/** How healthy is this unit currently?. * */
	public int getHealth()
	{
		return this.health < 0 ? 0 : this.health;
	}

	/** Is the unit damaged beyond repair?. * */
	public boolean isDead()
	{
		return this.isDead;
	}

	/** Kill the unit * */
	public void kill()
	{
		this.isDead = true;
	}

	/** Set health (called by network code only) * */
	public void setHealth(final int nHealth)
	{
		this.health = nHealth;
	}

	/** Returns the player that has control over this unit. * */

	public BattlePlayer getPlayer()
	{
		return this.player;
	}

	public String getName()
	{
		return this.getClass().getName();
	}

	private void calcDir()
	{
		this.direction.x = (float) Math.sin(this.dir);
		this.direction.y = (float) Math.cos(this.dir);
	}

	/** Queries whether this unit is on the move. * */
	public final boolean isMoving()
	{
		return this.move || (this.target_speed != 0.0);
	}

	/** Tell the unit to move, or to stop moving. * */
	public final void setMoving(final boolean moving)
	{
		this.move = moving;
	}

	public final Unit UnitCollidingWith()
	{
		return this.brain.CollidingWith(); // if not in the collision State then it will return null
	}

	public final boolean isTurning()
	{
		return (this.dir != this.prev_dir);
	}

	// public final float getMaxSpeed(){
	// return (super.getMaxSpeed() / (id+1));
	// }

	/**
	 * <STRONG>Just for JM at the moment, needs to be in constructor, otherwise cheating would be too easy.</STRONG>
	 */
	protected final void setHovering(final boolean hovering)
	{
		this.hovering = hovering;
	}

	// protected final void accelerate (final float force)
	// {
	// //this.applyForce (force * this.direction.x, force * this.direction.y);
	// this.force.x += force * this.direction.x;
	// this.force.y += force * this.direction.y;
	//
	// }

	protected final void maintainSpeed(final float speed)
	{
		Vector2d pos = this.position;

		float frict = this.air_resistance;

		if (!this.hovering)
		{
			final Tile tile = this.map.getTileAt((int) pos.x, (int) pos.y);
			frict += tile.getFrictionCoefficient();
		}

		final float fx = frict * speed * this.direction.x;
		final float fy = frict * speed * this.direction.y;

		final float fSq = fx * fx + fy * fy;

		if (fSq > this.acceleration_force * this.acceleration_force)
		{
			// this.accelerate (this.acceleration_force);
			this.force.x += this.acceleration_force * this.direction.x;
			this.force.y += this.acceleration_force * this.direction.y;

			return;
		}

		// this.applyForce (fx, fy);
		this.force.x += fx;
		this.force.y += fy;

	}

	protected final void accelerate()
	{
		this.reverse = false;
		this.braking = false;
		// this.accelerate (this.acceleration_force);
		this.force.x += this.acceleration_force * this.direction.x;
		this.force.y += this.acceleration_force * this.direction.y;
	}

	// protected final void decelerate ( )
	// {
	// this.braking = true;
	// }

	protected final void reverse()
	{
		// needs changing so we have a reversing force
		this.reverse = true;
		// this.accelerate (-this.acceleration_force);
		this.force.x -= this.acceleration_force * this.direction.x;
		this.force.y -= this.acceleration_force * this.direction.y;
	}

	public final void setTargetSpeed(final float speed)
	{
		this.target_speed = speed;
	}

	public final float getTargetSpeed()
	{
		return this.target_speed;
	}

	public final float getCurrentSpeed()
	{
		return (float) Math.sqrt(this.velocity.x * this.velocity.x + this.velocity.y * this.velocity.y);
	}

	public boolean isReversing()
	{
		return this.reverse;
	}

	protected final void moveToSpeed(final float speed)
	{
		if (speed == 0.0f)
		{
			// this.decelerate ( );
			this.braking = true;

			return;
		}

		Vector2d v = this.velocity;
		// final float current_speedSq = velocity.getDotProduct (velocity);
		final float current_speedSq = v.x * v.x + v.y * v.y;

		/*
		 * float vx = velocity.x*Math.abs( direction.x ); float vy = velocity.y*Math.abs( direction.y );
		 * 
		 * float current_speedSq = vx*vx + vy*vy;
		 */

		// maintainSpeed( speed );
		// if ( player.getID() == 0 )
		// System.out.println( Math.sqrt( current_speedSq ) );
		if (speed * speed <= current_speedSq)
		{
			this.maintainSpeed(speed);
		}
		else
		{
			if (speed < 0)
			{
				this.reverse();
			}
			else
			{
				this.accelerate();
			}
		}
	}

	/** Turn to an absolute heading. Returns true if the unit has turned to the heading. * */
	public final boolean turnToHeading(float h)
	{
		float heading = h;

		// not done properly yet
		if (this.dir != heading)
		{
			this.dir %= 2 * Math.PI;
			if (this.dir > Math.PI)
			{
				this.dir -= 2 * Math.PI;
			}
			if (this.dir < -Math.PI)
			{
				this.dir += 2 * Math.PI;
			}
			heading %= 2 * Math.PI;
			if (heading > Math.PI)
			{
				heading -= 2 * Math.PI;
			}
			if (heading < -Math.PI)
			{
				heading += 2 * Math.PI;
			}
			float diff = (heading - this.dir) % (2 * (float) Math.PI);
			if (diff > Math.PI)
			{
				diff -= 2 * Math.PI;
			}
			if (diff < -Math.PI)
			{
				diff += 2 * Math.PI;
			}

			if (Math.abs(diff) < 0.1f * this.turning_speed)
			{
				this.angular_velocity = 0.0f;
				this.dir = heading;

				this.calcDir();

				return true;
			}

			if (diff > 0.0f)
			{
				this.angular_velocity = this.turning_speed;
			}
			else
			{
				this.angular_velocity = -this.turning_speed;
			}

			return false;
		}

		return true;
	}

	/** Turn to a <EM>relative</EM> heading. * */

	public final boolean turn(final float angle)
	{
		return this.turnToHeading(this.dir + angle);
	}

	/** Get the direction that this unit is moving in. * */

	public final float getDirection()
	{
		return this.dir;
	}

	// public final Vector2d getDirectionVector ( )
	// {
	// return this.direction;
	// }

	// public final float getMaxTurningSpeed ( )
	// {
	// return this.turning_speed;
	// }

	/** Tell this unit to do something. * */

	public void giveOrder(final Order order)
	{
		this.orders.add(order);
	}

	/** Returns what this unit has been ordered to do. * */

	public Order getCurrentOrder()
	{
		if (this.orders.size() == 0)
		{
			return null;
		}

		// return this.getOrder (0);
		return this.orders.get(0);
	}

	public int getNumOrders()
	{
		return this.orders.size();
	}

	public Order getOrder(final int i)
	{
		return this.orders.get(i);
	}

	public void orderDone(final Order order)
	{
		final int numOrders = this.orders.size();
		if (!this.orders.remove(order))
		{
			throw new RuntimeException("unit did not do this order");
		}
		order.dispose();
		if (numOrders == this.orders.size())
		{
			throw new RuntimeException("unit did not do this order");
		}
	}

	// //////////////////////////////////////
	public void collide(final Unit unit)
	{
		if (unit.player != this.player)
		{

			final UnitType colidingUnitType = unit.type;
			// there should only be
			// 4 instances of UnitType
			// if (colidingUnitType == Unit.TROOP_UNIT)
			// {
			// this.collideWithTroopUnit(unit);
			// }
			// else if (colidingUnitType == Unit.LIGHT_UNIT)
			// {
			// this.collideWithLightUnit(unit);
			// }
			// else if (colidingUnitType == Unit.MEDIUM_UNIT)
			// {
			// this.collideWithMediumUnit(unit);
			// }
			// else if (colidingUnitType == Unit.HEAVY_UNIT)
			// {
			// this.collideWithHeavyUnit(unit);
			// }

			final int thisSize = colidingUnitType.getID() + 1;
			final int otherSize = colidingUnitType.getID() + 1;

			Vector2d v1 = this.velocity;
			Vector2d v2 = unit.velocity;

			final float dx = v1.x - v2.x;
			final float dy = v1.y - v2.y;
			final float diffSq = dx * dx + dy * dy;
			final float damage = 0.1f * (float) Math.sqrt(diffSq);
			// for now
			this.health -= (damage * otherSize) / (thisSize + otherSize);
			unit.health -= (damage * thisSize) / (thisSize + otherSize);
		}
	}

	// public void collideWithTroopUnit(Unit troopUnit)
	// {
	// }
	//
	// public void collideWithLightUnit(Unit troopUnit)
	// {
	// }
	//
	// public void collideWithMediumUnit(Unit troopUnit)
	// {
	// }
	//
	// public void collideWithHeavyUnit(Unit troopUnit)
	// {
	// }

	public void takeDamageFrom(final Explosion explosion)
	{
		int strength = explosion.getStrength();

		final UnitType t = this.type;
		if ((t == Unit.TROOP_UNIT) || (t == Unit.LIGHT_UNIT))
		{
			strength *= 2;
		}
		else if (t == Unit.HEAVY_UNIT)
		{
			strength /= 2;
			strength = strength == 0 ? 1 : strength;
		}

		this.health -= strength;

		if (this.player.isUser())
		{
			// BattleEngine.messageDisplay.unitAttackMsg ("Enemy attack in progress.");
			// Log.w("IU", "Enemy attack in progress.");
		}
	}

	// //////////////////////////////////////

	@Override
	public boolean hasMoved()
	{
		return this.moved || (this.prev_dir != this.dir) || super.hasMoved();
	}

	/*******************************************************************************************************************************************************************************
	 * Explicitly set that this unit has moved. This will force it to be redrawn on screen. Useful for when a unit is selected as otherwise the selection circle would not show up.
	 ******************************************************************************************************************************************************************************/

	public void setMoved(final boolean moved)
	{
		this.moved = moved;
	}

	/** Update the brain. * */

	public void integrateBrain(final float dt)
	{
		if (this.brain != null)
		{
			this.brain.think(dt);
		}
	}

	/** Integrate through time to get new position .* */
	@Override
	public void integrate(final float dt)
	{
		// moved = false;
		Vector2d pos = this.position;
		// if ( brain != null )
		// brain.think( dt );
		float frict = this.air_resistance;

		if (!this.hovering)
		{
			final Tile tile = this.map.getTileAt((int) pos.x, (int) pos.y);
			frict += tile.getFrictionCoefficient();
		}
		if (this.braking)
		{
			frict = (frict + 1.0f) * 2.0f; // increase friction when braking
		}
		this.prev_dir = this.dir;

		if (Debug.JOSHUA)
		{
			this.brainGraphics.mattsAngularVelocity = dt * this.angular_velocity;
		}

		if (this.angular_velocity != 0.0f)
		{
			this.dir += dt * this.angular_velocity;
			this.angular_velocity = 0.0f;
			this.calcDir();
		}

		final Weapon[] thisWeapons = this.getWeapons();
		for (int i = thisWeapons.length - 1; i >= 0; --i)
		{
			thisWeapons[i].integrate(dt);
		}

		this.moveToSpeed(this.target_speed); // JM hack

		// this.setFriction (friction);
		this.friction = frict;

		super.integrate(dt);
	}

	// // abstract methods
	public abstract Weapon[] getWeapons();

	public final void calcPredictedPosition(Vector2d predictedPosition, final float dt)
	{
		Vector2d pos = this.position;
		Vector2d vel = this.position;
		predictedPosition.x = pos.x + dt * vel.x;
		predictedPosition.y = pos.y + dt * vel.y;
	}

	public final void calcPredictedPosition(Vector2d predictedPosition, final float dt, final float speed, final float d)
	{
		Vector2d pos = this.position;
		predictedPosition.x = pos.x + ((dt * speed) * (float) Math.sin(d));
		predictedPosition.y = pos.y + ((dt * speed) * (float) Math.cos(d));
	}

	public Unit findNearestEnemy()
	{

		// If previously nearest enemy is still in the game
		boolean enemyFound = (this.nearestEnemy != null && !this.nearestEnemy.isDead && this.nearestEnemy.sortedIndexX != -1 && this.nearestEnemy.sortedIndexY != -1);

		// Search for nearest enemy if the last one has been killed or every SEARCH_DELAY frames
		if (!enemyFound || this.delayedSearchForEnemy > Unit.SEARCH_DELAY)
		{
			BattlePlayer friend = this.player;

			float lastMinSqrDistance = 10000 * 10000;

			float distSqr, distSqrX, d;
			Unit unit;

			int idxOffset = 1;
			int leftIdx, rightIdx;

			boolean leftDone = false;
			boolean rightDone = false;

			Unit[] allUnits = this.map.getAllUnitsOnX();
			int unitCount = this.map.getAllUnitsCount();

			int startIndexX = this.sortedIndexX;

			// Searches left and right of this unit for the nearest enemy
			while (!(leftDone && rightDone))
			{
				leftIdx = startIndexX - idxOffset;
				leftDone = leftDone || (leftIdx < 0);

				// Look on the left
				if (!leftDone)
				{
					unit = allUnits[leftIdx];

					// If enemy unit and not dead then measure distance
					if (!unit.isDead && unit.player != friend)
					{
						d = this.position.x - unit.position.x;
						distSqrX = d * d;

						// If distance by X is greater than the last measured distance then look no further
						if (distSqrX >= lastMinSqrDistance)
						{
							leftDone = true;
						}
						else
						{
							d = this.position.y - unit.position.y;
							distSqr = distSqrX + d * d;

							if (distSqr < lastMinSqrDistance)
							{
								this.nearestEnemy = unit;
								lastMinSqrDistance = distSqr;
							}
						}
					}
				}

				rightIdx = startIndexX + idxOffset;
				rightDone = rightDone || (rightIdx >= unitCount);

				// Look on the right
				if (!rightDone)
				{
					unit = allUnits[rightIdx];

					// If enemy unit and not dead then measure distance
					if (!unit.isDead && unit.player != friend)
					{
						d = this.position.x - unit.position.x;
						distSqrX = d * d;

						// If distance by X is greater than the last measured distance then look no further
						if (distSqrX >= lastMinSqrDistance)
						{
							rightDone = true;
						}
						else
						{
							d = this.position.y - unit.position.y;

							distSqr = distSqrX + d * d;
							if (distSqr < lastMinSqrDistance)
							{
								lastMinSqrDistance = distSqr;
								this.nearestEnemy = unit;
							}
						}
					}
				}

				idxOffset++;
			}

			this.delayedSearchForEnemy = 0;

		}
		else
		{
			// We have waited for one frame more
			this.delayedSearchForEnemy++;
		}

		return this.nearestEnemy;
	}

	//
	// TODO [] Replace with int field
	// 
	static class UnitType
	{
		private final int	id;

		UnitType(final int id)
		{
			this.id = id;
		}

		public boolean equals(final UnitType type)
		{
			return type.id == this.id;
		}

		public int getID()
		{
			return this.id;
		}
	}

	/**
	 * Write the bytes representing this unit's state information into <CODE>bytes</CODE> starting from <CODE>pos</CODE>
	 */
	public void writeStateInfoBytes(final byte[] bytes, final int pos)
	{
		final short x = (short) super.position.x;
		final short y = (short) super.position.y;

		bytes[pos] = (byte) ((this.id & 0xFF000000) >> 24);
		bytes[pos + 1] = (byte) ((this.id & 0x00FF0000) >> 16);
		bytes[pos + 2] = (byte) ((this.id & 0x0000FF00) >> 8);
		bytes[pos + 3] = (byte) ((this.id & 0x000000FF));

		bytes[pos + 4] = (byte) (x >> 8);
		bytes[pos + 5] = (byte) (x & 0x00FF);

		bytes[pos + 6] = (byte) (y >> 8);
		bytes[pos + 7] = (byte) (y & 0x00FF);

		final int myDir = (int) (this.dir * 100);
		bytes[pos + 8] = (byte) ((myDir & 0xFF000000) >> 24);
		bytes[pos + 9] = (byte) ((myDir & 0x00FF0000) >> 16);
		bytes[pos + 10] = (byte) ((myDir & 0x0000FF00) >> 8);
		bytes[pos + 11] = (byte) ((myDir & 0x000000FF));

		final int vx = (int) (this.velocity.x * 100.0);
		final int vy = (int) (this.velocity.y * 100.0);

		bytes[pos + 12] = (byte) ((vx & 0xFF000000) >> 24);
		bytes[pos + 13] = (byte) ((vx & 0x00FF0000) >> 16);
		bytes[pos + 14] = (byte) ((vx & 0x0000FF00) >> 8);
		bytes[pos + 15] = (byte) ((vx & 0x000000FF));

		bytes[pos + 16] = (byte) ((vy & 0xFF000000) >> 24);
		bytes[pos + 17] = (byte) ((vy & 0x00FF0000) >> 16);
		bytes[pos + 18] = (byte) ((vy & 0x0000FF00) >> 8);
		bytes[pos + 19] = (byte) ((vy & 0x000000FF));

	}

	public void readStateBytes(final byte[] bytes, final int pos)
	{

		final short x = (short) (((bytes[pos] & 0x00FF) << 8) | (bytes[pos + 1] & 0x00FF));
		final short y = (short) (((bytes[pos + 2] & 0x00FF) << 8) | (bytes[pos + 3] & 0x00FF));

		// System.out.println("Repos:"+this.id+" from ("+this.getPosition().x+","+this.getPosition().y+") to
		// ("+x+","+y+")");

		int byte1 = (bytes[pos + 4] & 0x000000FF) << 24, byte2 = (bytes[pos + 5] & 0x000000FF) << 16, byte3 = (bytes[pos + 6] & 0x000000FF) << 8, byte4 = (bytes[pos + 7] & 0x000000FF);

		// final float dir = ((byte1 | byte2 | byte3 | byte4)) / 100.0f;
		final float dir = ((byte1 | byte2 | byte3 | byte4)) * 0.01f;

		byte1 = (bytes[pos + 8] & 0x000000FF) << 24;
		byte2 = (bytes[pos + 9] & 0x000000FF) << 16;
		byte3 = (bytes[pos + 10] & 0x000000FF) << 8;
		byte4 = (bytes[pos + 11] & 0x000000FF);

		// final float vx = ((byte1 | byte2 | byte3 | byte4)) / 100.0f;
		final float vx = ((byte1 | byte2 | byte3 | byte4)) * 0.01f;

		byte1 = (bytes[pos + 12] & 0x000000FF) << 24;
		byte2 = (bytes[pos + 13] & 0x000000FF) << 16;
		byte3 = (bytes[pos + 14] & 0x000000FF) << 8;
		byte4 = (bytes[pos + 15] & 0x000000FF);

		// final float vy = ((byte1 | byte2 | byte3 | byte4)) / 100.0f;
		final float vy = ((byte1 | byte2 | byte3 | byte4)) * 0.01f;

		// this.setPosition (x, y);
		this.position.x = x;
		this.position.y = y;

		this.dir = dir;
		this.calcDir();

		// this.setVelocity (vx, vy);
		this.velocity.x = vx;
		this.velocity.y = vy;

	}

	public UnitSprite getUnitSprite()
	{
		return this.unitSprite;
	}

	public void setUnitSprite(UnitSprite unitSprite)
	{
		this.unitSprite = unitSprite;
	}
}
