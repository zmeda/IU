package iu.android.control;

import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * This class is used to track the mouse handling for different types of actions
 * 
 * On the emulator
 * ---------------
 * The size of the touch is always 0.0 for event.getSize ( )
 * The pressure of the touch is always 1.0 for event.getPressure ( )
 * 
 * On the actual phone the values above vary between 0.0 and 1.0, but the never reach the limits.
 */

public class MotionControl
{
	private final static long 		CLICK_TIME = 180;
	
	private static SurfaceHolder	surfaceHolder;
	
	private static boolean	actionDown		= false;
	private static boolean	actionMove		= false;
	private static boolean  clicked			= false;

	//
	// Rectangle representing a square selection on the screen
	//
	private static Rect		selectionArea	= new Rect (0, 0, 0, 0);

	// Position of start of the move
	private static int		startX;
	private static int		startY;

	// Last position of the move or actionDown action
	private static int		posX;
	private static int		posY;
	
	// The time of the last read event.
	// This value gives us clicks, when compared to CLICK_TIME.
	private static long		lastEventTime;
	

	
	/**
	 * 
	 * @param isDown
	 */
	private static void setActionDown (boolean isDown)
	{
		MotionControl.actionDown = isDown;
	}
	
	
	/**
	 * 
	 * @param isMove
	 */
	private static void setActionMove (boolean isMove)
	{
		MotionControl.actionMove = isMove;
	}
	

	/**
	 * 
	 * @return
	 */
	private static boolean isActionDown ( )
	{
		return MotionControl.actionDown;
	}


	/**
	 * 
	 * @return
	 */
	private static boolean isActionMove ( )
	{
		return MotionControl.actionMove;
	}

	
	/**
	 *  Links the surface displayed (i.e. the canvas on which we draw) to the thread in which we are running.
	 *  This is done for synchronization purposes, so that events are read consistently from the user interface.-
	 * 
	 *  @param holder The surface holder that is drawn in the main application thread.-
	 */
	public static void setSurfaceHolder (SurfaceHolder holder)
	{
		MotionControl.surfaceHolder = holder;
	}

	
	/**
	 * @return Returns true if the screen or the mouse have been clicked.-
	 */
	public static boolean hasBeenClicked ( )
	{
		if (MotionControl.clicked)
		{
			MotionControl.clicked = false;
			return (true);	
		}
		else
		{
			return (false);
		}
	}

	
	/**
	 * @return Returns true if the screen or the mouse have detected a drag gesture.- 
	 */
	public static boolean hasBeenDragged ( )
	{
		return (MotionControl.actionMove);
	}
	
	
	/**
	 * Returns the position of the start of a  move gesture or the last touch.-
	 */
	public static int getClickX ( )
	{
		return MotionControl.startX;
	}


	/**
	 * Returns the position of the start of a move gesture or the last touch.-
	 */
	public static int getClickY ( )
	{
		return MotionControl.startY;
	}


	/**
	 * Returns a reference to the last area selected. The coordinates are relative to the displayed screen!
	 */
	public static Rect getSelectionArea ( )
	{
		return MotionControl.selectionArea;
	}
	

	/**
	 * Processes the events read through the active surface (i.e. canvas).-
	 *  
	 * @param event
	 * @return True if the event was processed, false otherwise.-
	 */
	public static boolean onTouchEvent (MotionEvent event)
	{
		boolean handled = false;
		
		synchronized (MotionControl.surfaceHolder)
		{
			switch (event.getAction ( ))
			{
				case MotionEvent.ACTION_DOWN:
					//
					// Do not change the state of the flag if we already did
					//
					if (!MotionControl.isActionDown ( ))
					{
						Log.v ("IU", "Action down -> " + String.valueOf (event.getPressure ( )) + " [] " + String.valueOf (event.getSize ( )));
						
						// Save the time when this event happened
						MotionControl.lastEventTime = event.getEventTime ( );
						
						MotionControl.setActionDown (true);
						MotionControl.setActionMove (false);

						// Remembers the position of the last touch / start of move
						MotionControl.startX = (int) event.getX ( );
						MotionControl.startY = (int) event.getY ( );

						// Move not done yet (zero difference)
						MotionControl.posX = MotionControl.startX;
						MotionControl.posY = MotionControl.startY;
					}
					handled = true;
					break;

				case MotionEvent.ACTION_UP:
					//
					// Are we processing a click?
					//
					if (MotionControl.isActionDown ( ))
					{
						Log.v ("IU", "Click!");
						
						MotionControl.clicked = true;
						
						MotionControl.setActionDown (false);
						MotionControl.setActionMove (false);
					}
					//
					// Are we processing a drag?
					//
					else if (MotionControl.isActionMove ( ))
					{
						Log.v ("IU", "Mouse dragged!");
						
						MotionControl.setActionDown (false);
						MotionControl.setActionMove (false);

						//
						// Convert selection points into a proper rectangle
						//
						MotionControl.selectionArea.left   = Math.min (MotionControl.startX, MotionControl.posX);
						MotionControl.selectionArea.top    = Math.min (MotionControl.startY, MotionControl.posY);
						MotionControl.selectionArea.right  = Math.max (MotionControl.startX, MotionControl.posX);
						MotionControl.selectionArea.bottom = Math.max (MotionControl.startY, MotionControl.posY);
					}
					handled = true;
					break;

				case MotionEvent.ACTION_MOVE:
					//
					// Here we decide whether the event comes from the emulator or the actual phone
					// Look at the top of this class to see the differences.
					//
					if (event.getEventTime ( ) - MotionControl.lastEventTime < MotionControl.CLICK_TIME)
					{
						Log.v ("IU", "Action move -> ignored");
					}
					else
					{
						MotionControl.setActionMove (true);
						MotionControl.setActionDown (false);
	
						//
						// Remembers the position of this last move event
						//
						MotionControl.posX = (int) event.getX ( );
						MotionControl.posY = (int) event.getY ( );
	
						//
						// Convert selection points into a proper rectangle
						//
						MotionControl.selectionArea.left   = Math.min (MotionControl.startX, MotionControl.posX);
						MotionControl.selectionArea.top    = Math.min (MotionControl.startY, MotionControl.posY);
						MotionControl.selectionArea.right  = Math.max (MotionControl.startX, MotionControl.posX);
						MotionControl.selectionArea.bottom = Math.max (MotionControl.startY, MotionControl.posY);
	
						Log.v ("IU", "Action move -> " + MotionControl.selectionArea.toString ( ));
					}
					handled = true;
					break;

				case MotionEvent.ACTION_CANCEL:
					Log.v ("IU", "Action cancel -> " + event.toString ( ));

					MotionControl.setActionMove (false);
					MotionControl.setActionDown (false);
					handled = true;
					break;
			}

			return handled;
		}
	}
}
