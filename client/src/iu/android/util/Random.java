package iu.android.util;

public class Random
{
	private static java.util.Random	rand	= new java.util.Random();

	public static void setSeed(long seed)
	{
		Random.rand.setSeed(seed);
	}

	public static float randomFloat()
	{
		return Random.rand.nextFloat();
	}

}