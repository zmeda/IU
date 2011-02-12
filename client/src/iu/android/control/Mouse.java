package iu.android.control;

import android.graphics.Rect;
import android.view.MotionEvent;

//
// XXX maybe we don't need this whole mouse thing at all and do everything directly through MotionEvents
// 
public class Mouse /* extends MouseAdapter implements MouseMotionListener */
{

	public static class State
	{
		public int		x					= 0;
		public int		y					= 0;
		public boolean	mouseDown		= false;
		public boolean	mouseClicked	= false; // for when a click occurs quickly, i.e. between polls


		public void setEqualTo (final State state)
		{
			this.x = state.x;
			this.y = state.y;
			this.mouseDown = state.mouseDown;
			this.mouseClicked = state.mouseClicked;
		}

	}

	private Rect	viewRectangle;
	private State	state;
	private State	currentState;
	private State	previousState;
	private float	scaleFactor;


	public Mouse (final Rect viewRectangle)
	{
		this.viewRectangle = viewRectangle;
		this.state = new State ( );
		this.currentState = new State ( );
		this.previousState = new State ( );
	}


	public Rect getViewRectangle ( )
	{
		return this.viewRectangle;
	}


	public void setViewRectangle (final Rect viewRectangle)
	{
		this.viewRectangle = viewRectangle;
	}


	public void setScaleFactor (final float scale)
	{
		this.scaleFactor = scale;
	}


	public void mousePressed (final MotionEvent me)
	{
		// [] Requesting focus isn't really an issue since we are always in full screen mode
		// Object source = me.getSource();
		// Component component = (Component)source;
		// component.requestFocus();
		//

		this.state.x = this.viewRectangle.left + (int) (me.getX ( ) * this.scaleFactor);
		this.state.y = this.viewRectangle.top + (int) (me.getY ( ) * this.scaleFactor);
		this.state.mouseDown = true;
		this.state.mouseClicked = false;
	}


	public void mouseReleased (final MotionEvent me)
	{
		this.state.x = this.viewRectangle.left + (int) (me.getX ( ) * this.scaleFactor);
		this.state.y = this.viewRectangle.top + (int) (me.getY ( ) * this.scaleFactor);

		if (this.state.mouseDown)
		{
			this.state.mouseClicked = true;
		}
		else
		{
			this.state.mouseClicked = false;
		}
		this.state.mouseDown = false;
	}


	public void mouseDragged (final MotionEvent me)
	{
		this.state.x = this.viewRectangle.left + (int) (me.getX ( ) * this.scaleFactor);
		this.state.y = this.viewRectangle.top + (int) (me.getY ( ) * this.scaleFactor);
		this.state.mouseDown = true;
		this.state.mouseClicked = false;
	}


	// public void mouseMoved(MotionEvent me)
	// {
	// this.state.x = (int) (this.viewRectangle.left + me.getX());
	// this.state.y = (int) (this.viewRectangle.top + me.getY());
	// this.state.mouseDown = false;
	// this.state.mouseClicked = false;
	// }

	// public void mouseExited(MotionEvent me)
	// {
	// this.state.mouseDown = false;
	// this.state.mouseClicked = false;
	// }

	/** Query the mouse for it's current position. * */
	public void poll ( )
	{
		this.previousState.setEqualTo (this.currentState);
		this.currentState.setEqualTo (this.state);
		this.state.mouseClicked = false;
	}


	public void onMotionEvent (final MotionEvent me)
	{
		switch (me.getAction ( ))
		{
			case MotionEvent.ACTION_DOWN:
				this.mousePressed (me);
			break;

			case MotionEvent.ACTION_UP:
				this.mouseReleased (me);
			break;

			case MotionEvent.ACTION_MOVE:
				this.mouseDragged (me);
			break;

			default:
			break;
		}
	}


	public State getCurrentState ( )
	{
		return this.currentState;
	}


	public State getPreviousState ( )
	{
		return this.previousState;
	}

}