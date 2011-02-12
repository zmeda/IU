package iu.android.util;

import android.os.SystemClock;

/**
 * 
 * This class is used to measure time between calls to the function markFrame(int). In case of consecutive
 * calls to functions startFrame(int) and markFrame(int) only time between calls to these two functions is
 * measured. An average of last FRAME_NUMBER frames is measured and returned as FPS or time per frame in
 * milliseconds. Five different timers are supported which are accessible through the int variable for each
 * function call.
 * 
 * A call to resetFPS() resets all of the timers. A call to resetFPS(int) resets of the timer on the current
 * index.
 */

public class FPSUtil
{
	private static final String	LogCatFilterTag	= "FPSTest";

	public static final int			FRAME_COUNT			= 10;
	public static final int			COUNTER_COUNT		= 5;

	private static String[]			names					= new String[FPSUtil.COUNTER_COUNT];
	{
		for (int i = 0; i < FPSUtil.COUNTER_COUNT; i++)
		{
			FPSUtil.names[i] = "Counter " + i;
		}
	}

	// For FPS display
	private static int[]				frameNum				= new int[FPSUtil.COUNTER_COUNT];
	{
		for (int i = 0; i < FPSUtil.COUNTER_COUNT; i++)
		{
			FPSUtil.frameNum[i] = 0;
		}
	}
	private static long[]			lastTime				= new long[FPSUtil.COUNTER_COUNT];
	{
		for (int i = 0; i < FPSUtil.COUNTER_COUNT; i++)
		{
			FPSUtil.lastTime[i] = SystemClock.elapsedRealtime ( );
			;
		}
	}

	private static long[]			fullTime				= new long[FPSUtil.COUNTER_COUNT];
	{
		for (int i = 0; i < FPSUtil.COUNTER_COUNT; i++)
		{
			FPSUtil.fullTime[i] = 0;
		}
	}

	private static long				lastTimes[][]		= new long[FPSUtil.COUNTER_COUNT][FPSUtil.FRAME_COUNT];
	{
		for (int i = 0; i < FPSUtil.FRAME_COUNT; i++)
		{
			for (int n = 0; n < FPSUtil.COUNTER_COUNT; n++)
			{
				FPSUtil.lastTimes[FPSUtil.COUNTER_COUNT][i] = 0;
			}
		}
	}


	/**
	 * 
	 */
	public static void setName (int counter, String name)
	{
		FPSUtil.names[counter] = name;
	}


	/**
	 * Resets all of the statistics gathered
	 */
	public static void resetFPS ( )
	{
		for (int i = 0; i < FPSUtil.COUNTER_COUNT; i++)
		{
			FPSUtil.frameNum[i] = 0;
			FPSUtil.lastTime[i] = SystemClock.elapsedRealtime ( );
			FPSUtil.fullTime[i] = 0;

			for (int f = 0; f < FPSUtil.FRAME_COUNT; f++)
			{
				FPSUtil.lastTimes[i][f] = 0;
			}
		}
	}


	/**
	 * Resets the statistics gathered for the specified counter
	 */
	public static void resetFPS (int counter)
	{
		FPSUtil.frameNum[counter] = 0;
		FPSUtil.lastTime[counter] = SystemClock.elapsedRealtime ( );
		FPSUtil.fullTime[counter] = 0;

		for (int f = 0; f < FPSUtil.FRAME_COUNT; f++)
		{
			FPSUtil.lastTimes[counter][f] = 0;
		}
	}


	/**
	 * Start frame timing
	 */
	public static void startFrame (int counter)
	{
		FPSUtil.lastTime[counter] = System.currentTimeMillis ( );// SystemClock.elapsedRealtime();
	}


	/**
	 * This unit should be called once in the entire code - for example at the start of rendering of canvas
	 */
	public static void markFrame (int counter)
	{
		/*
		 * FPS
		 */

		// Get time and time for last frame
		long currentTime = System.currentTimeMillis ( );// SystemClock.elapsedRealtime();
		long diff = currentTime - FPSUtil.lastTime[counter];

		// Calculate average of last FRAME_NUMBER frames
		int idx = FPSUtil.frameNum[counter] % FPSUtil.FRAME_COUNT;
		FPSUtil.fullTime[counter] = FPSUtil.fullTime[counter] - FPSUtil.lastTimes[counter][idx] + diff;
		FPSUtil.lastTimes[counter][idx] = diff;

		// Remember the time of this mark
		FPSUtil.frameNum[counter]++;

		if (FPSUtil.frameNum[counter] % 100 == 0)
		{
			// Log.d ("FPSTest", "Opt. * DRAW FPS('" + FPSUtil.names[counter] + "', " + FPSUtil.frameNum[counter] + "): "
			// + FPSUtil.getAverageFPS (counter)
			// + " FPS / " + FPSUtil.getAverageTimeMillis (counter) + "ms/Frame");
		}

		// Update time mark
		FPSUtil.lastTime[counter] = currentTime;
	}


	/**
	 * @return the average FPS for the last FRAME_NUMBER frames
	 */
	public static long getAverageFPS (int counter)
	{
		long div = FPSUtil.fullTime[counter];

		if (div > 0)
		{
			return ((1000 * (FPSUtil.frameNum[counter] < FPSUtil.FRAME_COUNT ? FPSUtil.frameNum[counter] : FPSUtil.FRAME_COUNT)) / div);
		}
		else
		{
			return 0;
		}
	}


	/**
	 * @return the milliseconds used for the last FRAME_NUMBER frames
	 */
	public static long getAverageTimeMillis (int counter)
	{
		int div = (FPSUtil.frameNum[counter] < FPSUtil.FRAME_COUNT ? FPSUtil.frameNum[counter] : FPSUtil.FRAME_COUNT);

		if (div > 0)
		{
			return FPSUtil.fullTime[counter] / div;
		}
		else
		{
			return 0;
		}
	}
}
