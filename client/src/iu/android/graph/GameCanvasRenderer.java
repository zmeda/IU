package iu.android.graph;

import iu.android.battle.BattleEngine;
import iu.android.control.MotionControl;
import iu.android.engine.BattlePlayer;
import iu.android.graph.sprite.SpriteEngine;
import iu.android.map.Map;
import iu.android.map.MapRenderer;
import iu.android.map.Tile;
import iu.android.unit.Unit;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.Log;




/**
 * 
 * @author luka
 *
 */
public class GameCanvasRenderer
{

	private static final Paint			SelectionRectanglePaint;

	static
	{
		SelectionRectanglePaint = new Paint ( );
		GameCanvasRenderer.SelectionRectanglePaint.setColor (Color.WHITE);
		GameCanvasRenderer.SelectionRectanglePaint.setStyle (Style.STROKE);
	}

	//
	// Saves the previous location of the clipping area, so that 
	// it is calculated upon changes only (performance!)
	//
	private Point							clipAreaLocation		= new Point ( );

	//
	// Rectangle of indexes of which tiles should be drawn in the current frame
	//
	private Rect							drawableIndexRect		= new Rect ( );

	private ArrayList<Unit>				visibleUnits			= new ArrayList<Unit> ( );

	private MapRenderer					mapRenderer				= null;
		

	/**
	 * Draws the units on the screen
	 * 
	 * @param canvas
	 * @param map
	 * @param clipArea
	 */
	private void paintUnits (final Canvas canvas, final Map map, final Rect clipArea)
	{
		Unit[] allUnits = map.getAllUnitsOnX ( );
		int unitCount = map.getAllUnitsCount ( );
		Unit unit;

		//
		// This holds all visible units on screen (Faster selection with mouse)
		//
		ArrayList<Unit> thisVisibleUnits = this.visibleUnits;

		thisVisibleUnits.clear ( );

		for (int i = 0; i < unitCount; i++)
		{
			unit = allUnits[i];

			if (unit.isVisibleInRect (clipArea))
			{
				unit.getUnitSprite ( ).paint (canvas);

				thisVisibleUnits.add (unit);
			}
		}
	}
	
 
	/**
	 * Constructor
	 */
	public GameCanvasRenderer (final BattleEngine game)
	{
		int playerCount = game.getNumPlayers ( );
		
		for (int i = 0; i < playerCount; i++)
		{
			BattlePlayer player = game.getPlayer (i);
			SpriteEngine.addNewPlayerSprites (player);
		}

		this.mapRenderer = new MapRenderer (game.getMap ( ));
	}



	/**
	 * 
	 * Just draw stuff onto the back buffer and flip it to the screen.
	 * 
	 * @param map
	 * @param selectedUnits
	 * @param markerGraphics
	 * @param unitMarkerGraphics
	 * @param mouse
	 * @param canvas
	 * @param clipArea
	 * @param scaleFactor	The zoom level being used.
	 */
	public void renderFullScreen (final Map map, final SelectedUnits selectedUnits, final ArrayList<MarkerGraphic> markerGraphics,
											final ArrayList<UnitMarkerGraphic> unitMarkerGraphics, final Canvas canvas, final Rect clipArea,
											final float scaleFactor)
	{
		MapRenderer thisMapRenderer = this.mapRenderer;
		Rect 			selectionArea 	 = MotionControl.getSelectionArea ( );
		Tile[][] 	mapTiles 		 = map.getTiles ( );
		
		//
		// Calculate the areas of the map we'll be drawing, but only if the clipping area has changed
		//
		if (this.clipAreaLocation.x != clipArea.centerX ( ) || this.clipAreaLocation.y != clipArea.centerY ( ))
		{
			this.clipAreaLocation.x = clipArea.centerX ( );
			this.clipAreaLocation.y = clipArea.centerY ( );
			
			map.getClippedTiles (this.drawableIndexRect, clipArea);
		}
		
		//
		// Paint those areas of the map
		//
		for (int i = this.drawableIndexRect.left; i <= this.drawableIndexRect.right; i++)
		{
			for (int j = this.drawableIndexRect.top; j <= this.drawableIndexRect.bottom; j++)
			{
				thisMapRenderer.paint (canvas, mapTiles[i][j]);
			}
		}

		//
		// Paint the units
		//
		this.paintUnits (canvas, map, clipArea);

		//
		// Paints the explosions
		//
		thisMapRenderer.paintExplosions (canvas);

		//
		// Draw "markers" for different actions 
		//
		
		//
		// Markers for unit movements (reminders where move orders are)
		//
		int graphicsSize = markerGraphics.size ( );
		for (int i = 0; i < graphicsSize; i++)
		{
			MarkerGraphic marker = markerGraphics.get (i);
			marker.paint (canvas, Color.BLACK);
		}

		//
		// Markers for where attack unit orders were placed
		//
		for (int i = unitMarkerGraphics.size ( ) - 1; i >= 0; i--)
		{
			UnitMarkerGraphic marker = unitMarkerGraphics.get (i);
			marker.paint (canvas, Color.RED);
		}

		thisMapRenderer.paintProjectiles (canvas);

		//
		// Paint the selection box, if it is not empty
		//
		if (!selectionArea.isEmpty ( ))
		{
			if (MotionControl.hasBeenDragged ( ))
			{
				//
				// We must render the selection rectangle on the visible area of the map, thus the translation.
				// This is because events are read with coordinates relative to the visible area.
				//
				int offsetX = (int) (MotionControl.getClickX ( ) * scaleFactor);
				int offsetY = (int) (MotionControl.getClickY ( ) * scaleFactor);
				
				offsetX += clipArea.left;
				offsetY += clipArea.top;
				
				Log.d ("IU - GameCanvasRenderer", "Drawing rectangle " + selectionArea.toString ( ));
				
				//
				// Translate the selection rectangle to the clipped area we are rendering
				//
				selectionArea.offsetTo (offsetX, offsetY);

				//
				// Draw selection rectangle
				//
				canvas.drawRect (selectionArea, GameCanvasRenderer.SelectionRectanglePaint);
			}
		}
		
		//
		// Highlight units that are currently selected
		//
		selectedUnits.paint (canvas);
	}


	/**
	 * This method tries to first draw the units that were on screen the last frame then others
	 */
	public ArrayList<Unit> getVisibleUnits ( )
	{
		return this.visibleUnits;
	}


	/**
	 * Used for the first draw of the battle field background.-
	 * 
	 * @return the drawableIndexRect
	 */
	public Rect getDrawableIndexRect ( )
	{
		return this.drawableIndexRect;
	}
}
