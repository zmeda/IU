package iu.android.map;

public class MapGenerator
{

	private java.util.Random	random	= null;

		
	public float[][] createHeightMap(long seed, int size)
	{
		this.random = new java.util.Random(seed);
		float[][] height_map = new float[size][size];
		
		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				height_map[i][j] = this.random.nextFloat()*2-1;
			}
		}
		
		return height_map;
	}

	
	/** 
	 * Takes a height map and turns it into tile types.
	 */
	public int[][] createTileTypes(float[][] height_map, float waterLevel, float slope)
	{
		int[][] tileTypes = new int[height_map.length][height_map.length];

		for (int i = 0; i < height_map.length; i++)
		{
			for (int j = 0; j < height_map.length; j++)
			{
				int type = Tile.GRASS;
				if (height_map[i][j] < waterLevel)
				{
					type = Tile.WATER;
				}
				else
				{
					if ( height_map[ i ][ j ] < slope ) 
					{
						type = Tile.MARSH;
					}
					// type = Tile.MARSH;
//
//					float gradient = this.slope(i, j, height_map);
//
//					if (gradient > slope)
//					{
//						type = Tile.MARSH;
//					}

				}

				// if (Debug.)
				// {
				// if (i < 16 && j < 16)
				// {
				// type = Tile.GRASS; // matts graphics
				// }
				// }

				tileTypes[i][j] = type;
			}
		}

		return tileTypes;
	}

}