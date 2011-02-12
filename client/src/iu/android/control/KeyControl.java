package iu.android.control;

import iu.android.graph.GameView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;


/**
 * This class holds the keys status for required keys Using getters and setters for individual keys
 * 
 * NOTE: Removed key shortcuts for: Move, Attack, Turn, Recording and Shift
 *
 * In order for a view class to use keys it must implement these two methods with the following functionality:
 * 
 * The view class must also be set as focusable with the xml tag android:focusable="true" The onResume()
 * function of the View class must also call this.requestFocus(); every time in order for the View to get key
 * events.
 * 
 * public boolean onKeyDown(int keyCode, KeyEvent event) { return this.controls.onKeyDown(keyCode, event); }
 * 
 * public boolean onKeyUp(int keyCode, KeyEvent event) { return this.controls.onKeyUp(keyCode, event); }
 */

public class KeyControl
{
	private static GameView			battleView;
	private static SurfaceHolder	surfaceHolder;

	/*
	 * Order modifier keys
	 */
	private static boolean	turn_order_key_down		= false;
	private static boolean	move_in_formation			= false;
	private static boolean	attack_order_key_down	= false;

	
	/**
	 * Links the surface displayed (i.e. the canvas on which we draw) to the thread in which we are running.
	 * This is done for synchronization purposes, so that events are read consistently from the user interface.- 
	 */
	public static void setBattleView (GameView view)
	{
		KeyControl.battleView   = view;
		KeyControl.surfaceHolder = view.getHolder ( );
	}

	
	public static boolean turnOrderKeyDown ( )
	{
		return KeyControl.turn_order_key_down;
	}

	
	public static void setTurnOrderKeyDown (boolean turn_order_key_down)
	{
		KeyControl.turn_order_key_down = turn_order_key_down;
		if (turn_order_key_down)
		{
			KeyControl.attack_order_key_down = !turn_order_key_down;
		}
	}

	
	public static boolean moveInFormation ( )
	{
		return KeyControl.move_in_formation;
	}


	public static void setMoveInFormation (final boolean move_in_formation)
	{
		KeyControl.move_in_formation = move_in_formation;
	}


	public static void toggleMoveInFormation ( )
	{
		KeyControl.move_in_formation = !KeyControl.move_in_formation;
	}


	public static boolean attackOrderKeyDown ( )
	{
		return KeyControl.attack_order_key_down;
	}


	public static void setAttackOrderKeyDown (boolean attack_order_key_down)
	{
		KeyControl.attack_order_key_down = attack_order_key_down;
		if (attack_order_key_down)
		{
			KeyControl.turn_order_key_down = !attack_order_key_down;
		}
	}


	/*
	 * formation recording
	 */
	private static boolean	record_formation_key_down	= false;


	public static boolean recordingFormation ( )
	{
		return KeyControl.record_formation_key_down;
	}


	public static void setRecordingFormation (final boolean record_formation_key_down)
	{
		KeyControl.record_formation_key_down = record_formation_key_down;
	}

	/*
	 * DPAD center
	 */
	private static boolean	pad_center	= false;


	public static void setPadCenter (final boolean pad_center)
	{
		KeyControl.pad_center = pad_center;
	}


	public static boolean getPadCenter ( )
	{
		return KeyControl.pad_center;
	}

	/*
	 * shift, for selecting
	 */
	private static boolean	shift_down	= false;


	public static boolean isShiftDown ( )
	{
		return KeyControl.shift_down;
	}


	public static void setShiftDown (final boolean shift_down)
	{
		KeyControl.shift_down = shift_down;
	}


	/*
	 * Key down handling
	 */
	public static boolean onKeyDown (final int keyCode, final KeyEvent event)
	{
		boolean handled = false;
		
		synchronized (KeyControl.surfaceHolder)
		{
			Log.v ("IU - KeyControl", Integer.toString (keyCode));
			
			switch (keyCode)
			{
				//
				// Zooming should happen when the phone button is pushed
				//
				case KeyEvent.KEYCODE_DPAD_CENTER:
					KeyControl.battleView.startZooming ( );
					handled = true;
					break;

				//
				// Cursor keys
				//
				case KeyEvent.KEYCODE_DPAD_LEFT:
					KeyControl.battleView.moveScreenHorizontally (true);
					handled = true;
					break;
					
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					KeyControl.battleView.moveScreenHorizontally (false);
					handled = true;
					break;
					
				case KeyEvent.KEYCODE_DPAD_UP:
					KeyControl.battleView.moveScreenVertically (true);
					handled = true;
					break;
					
				case KeyEvent.KEYCODE_DPAD_DOWN:
					KeyControl.battleView.moveScreenVertically (false);
					handled = true;
					break;

				//
				// Keys common to ordinary telephone keyboard
				//
				case KeyEvent.KEYCODE_1:
					KeyControl.battleView.selectSquad (1, false);
					handled = true;
					break;
					
				case KeyEvent.KEYCODE_2:
					KeyControl.battleView.selectSquad (2, false);
					handled = true;
					break;
					
				case KeyEvent.KEYCODE_3:
					KeyControl.battleView.selectSquad (3, false);
					handled = true;
					break;
			}

			return handled;
		}
	}


	/*
	 * Key up handling
	 */
	public static boolean onKeyUp (final int keyCode, final KeyEvent event)
	{
		boolean handled = false;
		
		synchronized (KeyControl.surfaceHolder)
		{
			switch (keyCode)
			{
				case KeyEvent.KEYCODE_G:
					KeyControl.battleView.dispatchGuardOrder ( );
					handled = true;
					break;

				//
				// Zooming should happen either with the Z key
				// or the phones button.
				//
				case KeyEvent.KEYCODE_Z:
					KeyControl.battleView.startZooming ( );
					handled = true;
					break;
			}

			return handled;
		}
	}
}
