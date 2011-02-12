/**
 * 
 */
package iu.android.graph.sprite;

import android.graphics.Canvas;

import iu.android.Debug;

/**
 * @author luka
 * 
 */
public abstract class Sprite
{
// We probably do not need this method - since entire View redrawn on each View.onDraw() call back
//	public abstract Rect getDirtyRectangle ( );


	// GUI - old signature was paint (Graphics, Component)
	public abstract void paint (Canvas canvas);


	public abstract void dispose ( );


	@Override
	protected void finalize ( )
	{
		if (Debug.JOSHUA)
		{
			System.out.println ("Sprite GC-ed");
		}
	}
}
