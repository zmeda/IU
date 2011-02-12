package iu.android.control;

import iu.android.graph.GameView;
import android.hardware.SensorListener;
import android.util.Log;
import android.view.SurfaceHolder;


/**
 * This class defines the listener that reacts to the accelerometer in the phone.
 * With the accelerometer we move the battle field, instead of using the phone's track ball.
 * 
 * @author luka
 *
 */
public class AccelerometerListener implements SensorListener
{
	private static GameView			battleView;
	private static SurfaceHolder	surfaceHolder; 

	
	/**
	 * Links the surface displayed (i.e. the canvas on which we draw) to the thread in which we are running.
	 * This is done for synchronization purposes, so that events are read consistently from the user interface.- 
	 */
	public static void setBattleView (GameView view)
	{
		AccelerometerListener.battleView    = view;
		AccelerometerListener.surfaceHolder = view.getHolder ( );
	}


	/**
	 * Read the position of the phone from the accelerometer.-
	 */
	@Override
	public void onSensorChanged (int sensor, float[] values)
	{
		if (AccelerometerListener.surfaceHolder != null)
		{
			synchronized (AccelerometerListener.surfaceHolder)
			{
				Log.v ("IU", "sensorChanged (" + values[0] + ", " + values[1] + ", " + values[2] + ")");
				
				if (AccelerometerListener.battleView != null)
				{
					//
					// Add a little more stability to the background, so that it will not be shaking
					//
					if (Math.abs (values[0]) > 1.0)
					{
						//
						// Prevent diagonal movements when any of the axis prevail
						//
						if (Math.abs (values[0]) > Math.abs (values[1] * 1.5))
						{
							//
							// Horizontal move only
							//
							AccelerometerListener.battleView.moveScreenHorizontally (values[0] / 4);
						}
					}
					
					//
					// Add a little more stability to the background, so that it will not be shaking
					//
					if (Math.abs (values[1]) > 1.0)
					{
						if (Math.abs (values[1]) > Math.abs (values[0] * 2))
						{
							//
							// Vertical move only
							//
							AccelerometerListener.battleView.moveScreenVertically   (values[1] / -4);
						}
					}
				}
			}
		}
	}

	
	/**
	 * For the time being we will ignore any accuracy changes.- 
	 */
	@Override
	public void onAccuracyChanged (int sensor, int accuracy)
	{
		//
		// Nothing to do here!
		//
	}
}
