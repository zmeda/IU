package iu.android.graph;

import iu.android.Debug;
import iu.android.engine.BattlePlayer;
import iu.android.order.AttackUnitOrder;
import iu.android.order.GuardOrder;
import iu.android.unit.Circle;
import iu.android.unit.Unit;
import iu.android.unit.Vector2d;
import iu.android.graph.GameView;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;


/**
 * Paints some extra stuff around the units that are selected.-
 * 
 * @author luka
 *
 */
public class SelectedUnits
{

	private static final Paint	SelectionPaint;

	static
	{
		SelectionPaint = new Paint();
		SelectedUnits.SelectionPaint.setStyle(Style.STROKE);
		SelectedUnits.SelectionPaint.setAntiAlias(true);
	}

	private ArrayList<Unit>	   units;
	private BattlePlayer	      player	    = null;
	private int	               player_color = Color.TRANSPARENT;

	//
	// Never used!
	//
	//private Vector2d	       p1;
	//private Vector2d	       p2;
	

	/**
	 * Constructor
	 * 
	 * @param player
	 */
	public SelectedUnits (BattlePlayer player)
	{
		this.player = player;
		this.units = new ArrayList<Unit>();
		this.player_color = this.player.getColor();
	}

	public void addUnit(Unit unit)
	{
		if (!this.units.contains(unit))
		{
			this.units.add(unit);
			Debug.Joshua.println("Unit added : " + unit);
		}
	}

	
	public void removeDeadUnits()
	{
		ArrayList<Unit> unitsVec = this.units;

		for (int i = unitsVec.size() - 1; i >= 0; i--)
		{
			Unit selectedUnit = unitsVec.get(i);
			if (selectedUnit.isDead())
			{
				unitsVec.remove(i);
			}
		}
	}

	
	public void deselect()
	{
		ArrayList<Unit> unitsVec = this.units;

		int unitNum = unitsVec.size();
		for (int i = 0; i < unitNum; i++)
		{
			Unit selectedUnit = unitsVec.get(i);
			selectedUnit.setMoved(true); // so they'll redraw themselves to get rid of the selection circle
		}
		unitsVec.clear();
	}

	public void removeUnit(Unit unit)
	{
		this.units.remove(unit);
	}

	/*public void dispatchMoveOrders(GameCanvas game_canvas, int x, int y)
	{
		ArrayList<Unit> unitsVec = this.units;

		int unitSize = unitsVec.size();

		for (int i = 0; i < unitSize; i++)
		{
			Unit selectedUnit = unitsVec.get(i);

			Circle bounds = selectedUnit.getBounds();

			if (!bounds.contains(x, y))
			{ // if we are clicking out side of the unit
				game_canvas.dispatchMoveOrder(selectedUnit, (short) x, (short) y);
			}
		}
	}*/


	public void dispatchMoveOrders(GameView gameView, int x, int y)
	{
		ArrayList<Unit> unitsVec = this.units;

		int unitSize = unitsVec.size();

		for (int i = 0; i < unitSize; i++)
		{
			Unit selectedUnit = unitsVec.get(i);

			Circle bounds = selectedUnit.getBounds();

			if (!bounds.contains(x, y))
			{ // if we are clicking out side of the unit
				gameView.dispatchMoveOrder(selectedUnit, (short) x, (short) y);
			}
		}
	}

	
	
	/*public void dispatchFormationOrders(GameCanvas game_canvas, FormationGesture formationGesture)
	{
		formationGesture.dispatchMoveOrder(this.units, game_canvas);
	}

	public void dispatchFormationOrders(GameCanvas game_canvas, FormationGesture formationGesture, int x, int y)
	{
		formationGesture.moveTo(x, y);
		formationGesture.dispatchMoveOrder(this.units, game_canvas);
	}

	public void dispatchPointTurnOrders(GameCanvas game_canvas, int x, int y)
	{
		ArrayList<Unit> unitsVec = this.units;

		int unitNum = unitsVec.size();

		for (int i = 0; i < unitNum; i++)
		{
			Unit selectedUnit = unitsVec.get(i);
			TurnOrder torder = TurnOrder.getNewTurnOrder(this.player.getID(), selectedUnit.getID(), (short) (x), (short) (y), false); // immediate
			game_canvas.dispatchOrder(selectedUnit, torder);
		}
	}

	public void dispatchParallelTurnOrders(GameCanvas game_canvas, int x, int y)
	{
		float centre_x = 0.0f;
		float centre_y = 0.0f;

		ArrayList<Unit> unitsVec = this.units;

		int unitNum = unitsVec.size();
		for (int i = 0; i < unitNum; i++)
		{
			Unit selectedUnit = unitsVec.get(i);
			Vector2d pos = selectedUnit.getPosition();
			centre_x += pos.x;
			centre_y += pos.y;
		}
		centre_x /= unitsVec.size();
		centre_y /= unitsVec.size();

		int turn_x = (int) (x - centre_x), turn_y = (int) (y - centre_y);

		unitNum = unitsVec.size();
		for (int i = 0; i < unitNum; i++)
		{
			Unit selectedUnit = unitsVec.get(i);
			Vector2d pos = selectedUnit.getPosition();
			TurnOrder torder = TurnOrder.getNewTurnOrder(this.player.getID(), selectedUnit.getID(), (short) (pos.x + turn_x), (short) (pos.y + turn_y), false); // immediate
			game_canvas.dispatchOrder(selectedUnit, torder);
		}
	}

	public void dispatchPointAttackOrders(GameCanvas game_canvas, int x, int y)
	{
		ArrayList<Unit> unitsVec = this.units;

		int unitNum = unitsVec.size();

		for (int i = 0; i < unitNum; i++)
		{
			Unit selectedUnit = unitsVec.get(i);
			AttackPositionOrder order = AttackPositionOrder.getNewAttackPositionOrder(this.player.getID(), selectedUnit.getID(), (short) (x), (short) (y));
			game_canvas.dispatchOrder(selectedUnit, order);
		}
	}

	public void dispatchParallelAttackOrders(GameCanvas game_canvas, int x, int y)
	{
		ArrayList<Unit> unitsVec = this.units;

		int unitNum = unitsVec.size();

		if (unitNum == 0)
		{
			return;
		}

		float centre_x = 0.0f, centre_y = 0.0f;

		for (int i = 0; i < unitNum; i++)
		{
			Unit selectedUnit = unitsVec.get(i);
			Vector2d pos = selectedUnit.getPosition();
			centre_x += pos.x;
			centre_y += pos.y;
		}
		centre_x /= unitsVec.size();
		centre_y /= unitsVec.size();

		float dx = x - centre_x;
		float dy = y - centre_y;

		float distSq = dx * dx + dy * dy;

		if (distSq == 0.0)
		{
			return;
		}

		float dist = (float) Math.sqrt(distSq);

		if (dx == 0.0)
		{
			dx += 0.000001; // avoid special cases
		}
		if (dy == 0.0)
		{
			dy += 0.000001; // avoid special cases
		}

		dx /= dist;
		dy /= dist;

		this.p1.x = x - 10 * dy;
		this.p1.y = y + 10 * dx;

		this.p2.x = x + 10 * dy;
		this.p2.y = y - 10 * dx;

		unitNum = unitsVec.size();
		for (int i = 0; i < unitNum; i++)
		{
			Unit selectedUnit = unitsVec.get(i);
			Vector2d pos = selectedUnit.getPosition();
			dist = GeneralPhysics.distancePointLine(pos, this.p1, this.p2);
			float ax = dist * dx, ay = dist * dy;
			AttackPositionOrder order = AttackPositionOrder.getNewAttackPositionOrder(this.player.getID(), selectedUnit.getID(), (short) (pos.x + ax), (short) (pos.y + ay));
			game_canvas.dispatchOrder(selectedUnit, order);
		}
	}

	public void dispatchAttackOrder(GameCanvas game_canvas, Unit target)
	{
		ArrayList<Unit> unitsVec = this.units;

		int unitNum = unitsVec.size();

		for (int i = 0; i < unitNum; i++)
		{
			Unit selectedUnit = unitsVec.get(i);
			AttackUnitOrder order = AttackUnitOrder.getNewAttackUnitOrder(this.player.getID(), selectedUnit.getID(), target);
			game_canvas.dispatchOrder(selectedUnit, order);
		}
	}*/


	public void dispatchAttackOrder(GameView gameView, Unit target)
	{
		ArrayList<Unit> unitsVec = this.units;

		int unitNum = unitsVec.size();

		for (int i = 0; i < unitNum; i++)
		{
			Unit selectedUnit = unitsVec.get(i);
			AttackUnitOrder order = AttackUnitOrder.getNewAttackUnitOrder(this.player.getID(), selectedUnit.getID(), target);
			gameView.dispatchOrder(selectedUnit, order);
		}
	}

	
	/*public void guard(GameCanvas game_canvas)
	{
		ArrayList<Unit> unitsVec = this.units;

		int unitNum = unitsVec.size();

		for (int i = 0; i < unitNum; i++)
		{
			Unit selectedUnit = unitsVec.get(i);
			Vector2d pos = selectedUnit.getPosition();
			GuardOrder order = GuardOrder.getNewGuardOrder(this.player.getID(), selectedUnit.getID(), (short) (pos.x), (short) (pos.y));
			game_canvas.dispatchOrder(selectedUnit, order);
		}
	}*/

	
	public void guard(GameView gameView)
	{
		ArrayList<Unit> unitsVec = this.units;

		int unitNum = unitsVec.size();

		for (int i = 0; i < unitNum; i++)
		{
			Unit selectedUnit = unitsVec.get(i);
			Vector2d pos = selectedUnit.getPosition();
			GuardOrder order = GuardOrder.getNewGuardOrder(this.player.getID(), selectedUnit.getID(), (short) (pos.x), (short) (pos.y));
			gameView.dispatchOrder(selectedUnit, order);
		}
	}

	
	/**
	 * Paints the health bars of selected units and circles around them
	 */
	public void paint(Canvas c)
	{
		ArrayList<Unit> unitsVec = this.units;

		SelectedUnits.SelectionPaint.setColor(this.player_color);

		Unit unit;
		Circle bounds;

		int size = unitsVec.size();
		for (int i = 0; i < size; i++)
		{
			unit = unitsVec.get(i);
			bounds = unit.getBounds();

			c.drawCircle(bounds.x, bounds.y, bounds.radius, SelectedUnits.SelectionPaint);

			unit.getUnitSprite().paintHealth(c);
		}
	}

	/**
	 * @return A list of currently selected units.-
	 */
	public ArrayList<Unit> getSelectedUnits ( )
	{
		return this.units;
	}
}
