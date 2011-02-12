package iu.android.map;

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

import iu.android.engine.Explosion;
import iu.android.graph.sprite.ExplosionSprite;
import iu.android.graph.sprite.ProjectileSprite;
import iu.android.graph.sprite.Sprite;
import iu.android.unit.Projectile;

import java.util.ArrayList;

import android.graphics.Canvas;

public class MapRenderer
{
	public TileImages[][]	  tile_images	    = null;
	private ArrayList<Sprite>	renderedSprites	= new ArrayList<Sprite>();
	private Map	              map	            = null;

	public MapRenderer(Map map)
	{
		int size = map.mapSize;
		this.tile_images = new TileImages[size][size];

		TileImages[][] thisTileImages = this.tile_images;

		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				Tile tile = map.getTile(i, j);
				thisTileImages[i][j] = new TileImages(tile);
			}
		}
		//this.calcOverlappingTiles();
		this.map = map;
	}

	// public Vector getRenderedSprites ( )
	// {
	// return renderedSprites;
	// }

	// public void paint(Canvas canvas, Area area)
	// {
	//
	// /*
	// * LitImage litImage = Managers.getImageManager().getLitImage( "grassTransitions.gif", "grassTransitionsMap.gif" ); game.Debug.johnWin.setGraphicsSize( 1024, 130 );
	// * game.Debug.johnWin.g().drawImage( litImage.getImage(), 0, 0, game.Debug.johnWin ); game.Debug.johnWin.repaint();
	// */
	//
	// // Rectangle prevClip = g.getClipBounds ( );
	// Tile[][] tiles = area.tiles;
	//
	// TileImages[][] thisTileImages = this.tile_images;
	//
	// for (int i = 0; i < tiles.length; i++)
	// {
	// Tile[] tilesI = tiles[i];
	//
	// int len = tilesI.length;
	// for (int j = 0; j < len; j++)
	// {
	// Tile tile = tilesI[j];
	// // tile_images[tile.getIndex1 ( )][tile.getIndex2 ( )].paint (canvas);
	// thisTileImages[tile.index1][tile.index2].paint(canvas);
	// }
	// }
	// // if (prevClip != null)
	// // g.setClip (prevClip.x, prevClip.y, prevClip.width, prevClip.height);
	// //
	// }

	public void paint(Canvas canvas, Tile tile)
	{
		this.tile_images[tile.index1][tile.index2].paint(canvas);
	}

	public void paintExplosions(Canvas canvas)
	{
		ArrayList<Explosion> mapExplosions = this.map.getExplosions();
		ArrayList<Sprite> thisRendererSprites = this.renderedSprites;

		int explosionsCount = mapExplosions.size();

		for (int i = explosionsCount - 1; i >= 0; i--)
		{
			Explosion explosion = mapExplosions.get(i);

			ExplosionSprite explosionSprite = ExplosionSprite.newExplosionSprite();
			explosionSprite.init(explosion);
			explosionSprite.paint(canvas);
			thisRendererSprites.add(explosionSprite);
		}
	}

	// public void paintHigherLayers(Canvas canvas, Area area)
	// {
	//
	// Tile[][] tiles = area.tiles;
	//
	// TileImages[][] thisTileImages = this.tile_images;
	//
	// for (int i = 0; i < tiles.length; i++)
	// {
	// Tile[] tilesI = tiles[i];
	// int len = tilesI.length;
	//
	// for (int j = 0; j < len; j++)
	// {
	// Tile tile = tilesI[j];
	// // tile_images[tile.getIndex1 ( )][tile.getIndex2 ( )].paintHigherLayer (canvas);
	// thisTileImages[tile.index1][tile.index2].paintHigherLayer(canvas);
	// }
	// }
	// }

	public void paintProjectiles(Canvas canvas)
	{
		ArrayList<Projectile> mapProjectiles = this.map.getProjectiles();
		ArrayList<Sprite> rendererSprites = this.renderedSprites;

		for (int i = mapProjectiles.size() - 1; i >= 0; i--)
		{
			Projectile projectile = mapProjectiles.get(i);

			ProjectileSprite projectileSprite = ProjectileSprite.newProjectileSprite();
			projectileSprite.init(projectile);
			projectileSprite.paint(canvas);
			rendererSprites.add(projectileSprite);
		}
	}
}
