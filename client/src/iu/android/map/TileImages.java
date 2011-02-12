/*************************************************************************************************************
 * IU 1.0b, a java realtime strategy game
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
 **************************************************************************************************************/

package iu.android.map;

import iu.android.graph.ImageManager;
import iu.android.graph.LitImage;

import android.graphics.Canvas;
import android.graphics.Paint;


/**
 * This class represents all the tile images needed to paint the map.-
 * 
 * @author luka
 *
 */
public class TileImages
{
	private LitImage		image;
	private Tile			tile	= null;
	
	public static Paint	paint	= new Paint ( );

	

	/**
	 * Constructor
	 * 
	 * @param tile
	 */
	public TileImages (Tile tile)
	{
		this.tile = tile;
		this.image = ImageManager.getLitImage (tile.getTypeAsString ( ));
	}


	public void paint (Canvas canvas, float left, float top)
	{
		// int x = this.tile.left, y = this.tile.top;

		// canvas.clipRect( left, top, left+Tile.Size, top+Tile.Size );
		canvas.drawBitmap (this.image.getImage ( ), left, top, TileImages.paint);

	}


	public void paint (Canvas canvas)
	{
		int left = this.tile.left, top = this.tile.top;

		// g.setClip( x, y, Tile.Size, Tile.Size );
		canvas.drawBitmap (this.image.getImage ( ), left, top, TileImages.paint);
		// g.drawImage( image/*.getImage()*/, x, y, component );
		/*
		 * for ( int i = 0; i < overlaps.size(); i++ ) { Overlap overlap = (Overlap)overlaps.elementAt( i );
		 * overlap.paint( left, top, canvas ); }
		 */
	}
}
