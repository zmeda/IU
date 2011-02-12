package iu.database.util;

import iu.server.explore.game.World;

public class FillDB
{
	public static void main (final String[] args)
	{
		float centerLat, centerLon, size;
		int fowTileCountPow;
		boolean truncateBatabaseBeforeInsert;

		DBUtility iuDbUtil = new DBUtility ( );

		iuDbUtil.openDbConnection ( );

		// Makes a 2^6 x 2^6 = 64x64 tiles size world with the center of 4x4 tiles opened
		// Size = 1 is about 11.1km on the equator

		//
		// Create Ljubljana
		//

		centerLat = 46.0f + 3.0f / 60.0f;
		centerLon = 14.0f + 31.0f / 60.0f;
		size = 0.2f;
		fowTileCountPow = 6;
		truncateBatabaseBeforeInsert = true;
		
		World ljubljana = new World (new Integer (1), "Ljubljana", 
									 centerLat + size / 2, 
									 centerLon - size / 2,
									 size, size, fowTileCountPow);
		
		iuDbUtil.createRandomDatabase (ljubljana, 2, truncateBatabaseBeforeInsert);

		//
		// Create Mountain View
		//

		/*
		centerLat = 37.591488f;
		centerLon = -122.120120f;
		size = 0.1f;
		fowTileCountPow = 6;

		World mountView = new World (new Integer (1), "Mountain View", centerLat + size / 2, centerLon - size / 2, size, size, fowTileCountPow);

		truncateBatabaseBeforeInsert = true;
		iuDbUtil.createRandomDatabase (mountView, 2, truncateBatabaseBeforeInsert);*/

		//
		// Northern California forest
		//
//		centerLat = 47.055673f;
//		centerLon = -115.667390f;
//		size = 0.2f;
//		fowTileCountPow = 6;
//		
//		World mountView = new World (new Integer (1), 
//									 "California forest", 
//									 centerLat + size / 2, 
//									 centerLon - size / 2,
//									 size, size, fowTileCountPow);
//		
//		truncateBatabaseBeforeInsert = true;
//		iuDbUtil.createRandomDatabase (mountView, 2, truncateBatabaseBeforeInsert);

		//
		// Close connection
		//

		iuDbUtil.closeDbConnection ( );
	}
}