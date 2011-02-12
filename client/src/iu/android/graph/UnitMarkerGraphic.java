package iu.android.graph;

import iu.android.unit.Circle;
import iu.android.unit.Unit;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class UnitMarkerGraphic
{

	private static final ArrayList<UnitMarkerGraphic>	pool	  = new ArrayList<UnitMarkerGraphic>();
	private static final Paint	                      MarkerPaint	= new Paint();

	static
	{
		UnitMarkerGraphic.MarkerPaint.setAntiAlias(true);
		UnitMarkerGraphic.MarkerPaint.setStyle(Style.STROKE);
	}

	Unit	                                          unit	      = null;
	int	                                              life	      = -1;

	
	/**
	 * Constructor
	 */
	private UnitMarkerGraphic()
	{
	// so you can't make new ones and bypass the pool
	} 

	/** This method is <STRONG>not thread-safe</STRONG>. * */
	public static UnitMarkerGraphic getNewUnitMarkerGraphic(Unit unit)
	{
		ArrayList<UnitMarkerGraphic> unitPool = UnitMarkerGraphic.pool;
		int poolSize = unitPool.size();

		UnitMarkerGraphic marker = null;
		if (poolSize == 0)
		{
			marker = new UnitMarkerGraphic();
		}
		else
		{
			marker = unitPool.get(poolSize - 1);
			unitPool.remove(poolSize - 1);
		}
		marker.setUnit(unit);
		return marker;
	}

	/**
	 * Treat dispose like delete in C++, i.e. <STRONG>do not use this object after calling dispose on it</STRONG>.
	 */

	public void dispose()
	{
		UnitMarkerGraphic.pool.add(this);
	}

	/**
	 * Set the unit and reset life of the marker
	 * 
	 * @param unit
	 */
	private void setUnit(Unit unit)
	{
		this.life = 12;
		this.unit = unit;
	}

	// public boolean isAlive ( )
	// {
	// return life >= 0;
	// }

	
	/**
	 * Draws the marker. Different sizes are to make a little animation effect.-
	 */
	public void paint(Canvas c, int color)
	{
		if (this.life > 1)
		{
			UnitMarkerGraphic.MarkerPaint.setColor(color);
			Circle b = this.unit.getBounds();

			c.drawCircle(b.x, b.y, b.radius, UnitMarkerGraphic.MarkerPaint);
		}

		this.life--;
	}
}
