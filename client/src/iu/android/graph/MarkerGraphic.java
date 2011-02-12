package iu.android.graph;

import iu.android.graph.sprite.Sprite;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * A marking that is painted to reflect a users action (e.g. click). Includes several factory methods.
 * 
 * @author luka
 * 
 */
public final class MarkerGraphic extends Sprite
{
	private static ArrayList<MarkerGraphic>	Pool	= new ArrayList<MarkerGraphic>();

	private static final Paint	            CirclePaint;
	private static final Paint	            SquarePaint;

	private static final int	            Radius	= 10;

	static
	{
		CirclePaint = new Paint();
		MarkerGraphic.CirclePaint.setStyle(Style.STROKE);
		MarkerGraphic.CirclePaint.setAntiAlias(true);

		SquarePaint = new Paint();
		MarkerGraphic.SquarePaint.setStyle(Style.STROKE);
		MarkerGraphic.SquarePaint.setAntiAlias(false);
	}

	// private Rect dirtyRectangle = new Rect ( );

	int x;
	// private Rect dirtyRectangle = new Rect ( );

	int y;
	int	                                    life;
	int	                                    color;
	private boolean	                        out	   = true;
	private boolean	                        circle	= true;

	private MarkerGraphic()
	{
		this.out = true;
		this.circle = true;
	}

	/** This method is <STRONG>not thread-safe</STRONG>. * */
	public static MarkerGraphic getNewMarkerGraphic(MarkerGraphic marker)
	{
		MarkerGraphic copy = MarkerGraphic.getNewMarkerGraphic(marker.x, marker.y);
		copy.color = marker.color;
		copy.out = marker.out;
		copy.circle = marker.circle;
		return copy;
	}

	/** This method is <STRONG>not thread-safe</STRONG>. * */
	public static MarkerGraphic getNewMarkerGraphic(int x, int y)
	{
		MarkerGraphic marker = null;
		if (MarkerGraphic.Pool.isEmpty())
		{
			marker = new MarkerGraphic();
		}
		else
		{
			marker = MarkerGraphic.Pool.get(MarkerGraphic.Pool.size() - 1);
			MarkerGraphic.Pool.remove(MarkerGraphic.Pool.size() - 1);
		}
		marker.setPosition(x, y);
		marker.color = Color.YELLOW;
		marker.out = true;
		return marker;
	}

	/** This method is <STRONG>not thread-safe</STRONG>. * */
	public static MarkerGraphic getNewCircleMarkerGraphic(int color, int x, int y, boolean out)
	{
		MarkerGraphic graphic = MarkerGraphic.getNewMarkerGraphic(x, y);
		graphic.color = color;
		graphic.out = out;
		graphic.circle = true;
		return graphic;
	}

	/** This method is <STRONG>not thread-safe</STRONG>. * */
	public static MarkerGraphic getNewSquareMarkerGraphic(int color, int x, int y, boolean out)
	{
		MarkerGraphic graphic = MarkerGraphic.getNewMarkerGraphic(x, y);
		graphic.color = color;
		graphic.out = out;
		graphic.circle = false;
		return graphic;
	}

	/**
	 * Treat dispose like delete in C++, i.e. <STRONG>do not use this object after calling dispose on it</STRONG>.
	 */

	@Override
	public void dispose()
	{
		MarkerGraphic.Pool.add(this);
	}

	/**
	 * Set position and reset life
	 * 
	 * @param x
	 * @param y
	 */
	private void setPosition(int x, int y)
	{
		this.life = MarkerGraphic.Radius;
		this.x = x;
		this.y = y;
		// dirtyRectangle.x = x - radius - 1;
		// dirtyRectangle.y = y - radius - 1;
		// dirtyRectangle.width = 2 * radius + 2;
		// dirtyRectangle.height = 2 * radius + 2;
	}

	// public Rectangle getDirtyRectangle ( )
	// {
	// return dirtyRectangle;
	// }

	// public boolean hasMoved ( )
	// {
	// return true;// life <= 1 || life >= 9; // erase it when it is dead
	// }

	// public boolean isAlive ( )
	// {
	// return this.life >= 0;
	// }

	private void draw(Canvas c, int x, int y, int r)
	{
		if (this.circle)
		{
			MarkerGraphic.CirclePaint.setColor(this.color);
			c.drawCircle(x, y, r, MarkerGraphic.CirclePaint);
		}
		else
		{
			MarkerGraphic.SquarePaint.setColor(this.color);
			c.drawRect(x - r, y - r, x + r, y + r, MarkerGraphic.SquarePaint);
		}
	}

	@Override
	public void paint(Canvas c)
	{
		int lLife = this.life;
		int lx = this.x;
		int ly = this.y;
		int lr = MarkerGraphic.Radius;

		if (lLife > 1)
		{
			if (this.out)
			{
				int r = lr - lLife;
				this.draw(c, lx, ly, r);
			}
			else
			{
				if (lLife > 3)
				{
					int r = (lr + (lLife - 10));
					this.draw(c, lx, ly, r);
				}
				else
				{
					this.draw(c, lx, ly, lr);
				}
			}
		}

		this.life--;
	}

	/**
	 * Set the color and paint. The color setting will remain with the marker!
	 * 
	 * @param c
	 * @param color
	 */
	public void paint(Canvas c, int color)
	{
		this.color = color;
		this.paint(c);
	}
}
