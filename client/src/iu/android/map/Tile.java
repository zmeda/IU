/*************************************************************************************************************
 * YARTS 1.0, a java realtime strategy Copyright (C) 2003 Duncan Jauncey, John S Montgomery
 * (john.montgomery@lineone.net) and Matthew Sarjent
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 ************************************************************************************************************/

package iu.android.map;

/** An individual graphical tile, that may have properties concerning unit movement etc. * */

public class Tile
{
	public static final int	Size_Power	      = 8;
	public static final int	Size	            = 1 << Tile.Size_Power;

	public static final int	WATER	            = 0;
	public static final int	MARSH	            = 1;
	public static final int	GRASS	            = 2;
	public static final int	NUMBER_OF_TYPES	= 3;

	int left;
	int top;
	int right;
	int bottom;
	int index1;
	int index2;

	// private Vector images = new Vector();
	// private Vector details = new Vector();
	// private Vector cliff_edges = new Vector();
	float	                frictionCoefficient	= 0.0f;
	int	                    type;
	int	                    height	            = 0;

	/*
	 * private class Detail { int x, y; LitImage image; }
	 */

	public Tile(int _left, int _top, int type, int height)
	{
		this.left = this.right = _left;
		this.top = this.bottom = _top;
		// index1 = left / Size;
		// index2 = top / Size;
		this.index1 = this.left >> Tile.Size_Power;
		this.index2 = this.top >> Tile.Size_Power;

		this.right += Tile.Size;
		this.bottom += Tile.Size;
		this.height = height;
		this.type = type;
		switch (type)
		{
		case WATER:
			this.frictionCoefficient = 1.5f;
			break;
		case MARSH:
			this.frictionCoefficient = 4.0f;
			break;
		case GRASS:
			this.frictionCoefficient = 0.9f;
			break;
		}

		// images.addElement( imageManager.getLitImage( getTypeString( type ) + ".gif" ) );
		// addDetails( imageManager );
	}


	public String getTypeAsString()
	{
		switch (this.type)
		{
			case GRASS:
				return "grass_very_big";
			case WATER:
				return "grass_water_very_big";
			case MARSH:
				return "grass_rocks_very_big";
			default:
				return null;
		}
	}


	public float getFrictionCoefficient()
	{
		return this.frictionCoefficient;
	}

	public int getType()
	{
		return this.type;
	}
}
