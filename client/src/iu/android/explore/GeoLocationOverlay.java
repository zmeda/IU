package iu.android.explore;

import iu.android.R;
import iu.android.engine.PlayerRegistry;
import iu.android.network.explore.Protocol;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Message;
import android.util.Log;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.GeoPoint;


/*
 * This overlay is used to display stuff on the map like posts, info...
 */

class GeoLocationOverlay extends Overlay
{
	private static final int		FLAG_DRAW_SIZE	= 5;

	private static final int		cmdBlueColor	= 0x805070ff;
	private static final int		cmdRedColor		= 0x80ff5050;

	private final Paint farFlagPaint;
	private final Paint nearFlagPaint;
	private final Paint farFlagTextPaint;
	private final Paint nearFlagTextPaint;
	private final Paint selectedFlagPaint;
	private final Paint playerPaint;
	private final Paint playerTextPaint;
	private final Paint directionPaint;
	private final Paint gridPaint;
	private final Paint fogPaint;
	private final Paint boundingBoxPaint;
	private final Paint redBoxPaint;

	private final BitmapDrawable	redFlag;
	private final BitmapDrawable	blueFlag;
	private final BitmapDrawable	greenFlag;
	private final BitmapDrawable	greyFlag;
	private final BitmapDrawable	redFlagDark;
	private final BitmapDrawable	blueFlagDark;
	private final BitmapDrawable	greenFlagDark;
	private final BitmapDrawable	greyFlagDark;
	private final BitmapDrawable	flagUnderAttack;
	//  private final BitmapDrawable cmdRanks;

	private final ExploreMode		exploreMode;

	boolean								debug				= false;

	public static Location			MAX_LOCATION	= new Location (LocationManager.GPS_PROVIDER);
	public static Location			MIN_LOCATION	= new Location (LocationManager.GPS_PROVIDER);

	GeoPoint								lastMapCenter	= new GeoPoint (0, 0);
	Rect									boundsIndex		= new Rect ( );

	// Used for some distance calculation
	Location								tmpLoc			= new Location (LocationManager.GPS_PROVIDER);


	public GeoLocationOverlay (final ExploreMode exploreMode)
	{
		this.exploreMode = exploreMode;

		//
		// The brushes we paint with
		//

		// Flags
		this.nearFlagPaint = new Paint ( );
		this.nearFlagPaint.setStyle (Style.FILL);
		this.nearFlagPaint.setStrokeWidth (3);
		this.nearFlagPaint.setAntiAlias (true);

		this.farFlagPaint = new Paint ( );
		this.farFlagPaint.setStyle (Style.STROKE);
		this.farFlagPaint.setStrokeWidth (1);
		this.farFlagPaint.setAntiAlias (true);

		this.nearFlagTextPaint = new Paint ( );
		this.nearFlagTextPaint.setTextSize (15f);
		this.nearFlagTextPaint.setAntiAlias (true);

		this.farFlagTextPaint = new Paint ( );
		this.farFlagTextPaint.setTextSize (10f);
		this.farFlagTextPaint.setAntiAlias (true);

		this.selectedFlagPaint = new Paint ( );
		this.selectedFlagPaint.setColor (0xffffff00);
		this.selectedFlagPaint.setStyle (Style.STROKE);
		this.selectedFlagPaint.setStrokeWidth (2);

		// Player
		this.playerPaint = new Paint ( );
		this.playerPaint.setAntiAlias (true);
		this.playerPaint.setAntiAlias (true);

		this.playerTextPaint = new Paint ( );
		this.playerTextPaint.setStyle (Style.STROKE);
		this.playerTextPaint.setAntiAlias (true);

		this.directionPaint = new Paint ( );
		this.directionPaint.setColor (0xffffff00);
		this.directionPaint.setStrokeWidth (3);
		this.directionPaint.setAntiAlias (true);

		// Fog
		this.fogPaint = new Paint ( );
		// this.fogPaint.setColor (0x5f000000);
		this.fogPaint.setColor (0x8f606060);
		this.fogPaint.setStyle (Style.FILL);

		//
		// Debug paints
		//

		// Blue fog of war field boxes
		this.boundingBoxPaint = new Paint ( );
		this.boundingBoxPaint.setColor (0xff0000ff);
		this.boundingBoxPaint.setStyle (Style.STROKE);

		// Map grid and rectangle
		this.gridPaint = new Paint ( );
		this.gridPaint.setColor (0xff00ff00);
		this.gridPaint.setStyle (Style.STROKE);

		// Empty fog of war tile
		this.redBoxPaint = new Paint ( );
		this.redBoxPaint.setColor (0xffff0000);
		this.redBoxPaint.setStyle (Style.STROKE);

		// 
		// Commanders
		//
		//  this.cmdRanks = (BitmapDrawable) this.exploreMode.getResources ( ).getDrawable
		// (R.drawable.commander_20x30);

		// flags
		this.redFlag = (BitmapDrawable) this.exploreMode.getResources ( ).getDrawable (R.drawable.flag_red);
		this.redFlagDark = (BitmapDrawable) this.exploreMode.getResources ( ).getDrawable (R.drawable.flag_red_dark);

		this.blueFlag = (BitmapDrawable) this.exploreMode.getResources ( ).getDrawable (R.drawable.flag_blue);
		this.blueFlagDark = (BitmapDrawable) this.exploreMode.getResources ( ).getDrawable (R.drawable.flag_blue_dark);

		this.greenFlag = (BitmapDrawable) this.exploreMode.getResources ( ).getDrawable (R.drawable.flag_green);
		this.greenFlagDark = (BitmapDrawable) this.exploreMode.getResources ( ).getDrawable (R.drawable.flag_green_dark);

		this.greyFlag = (BitmapDrawable) this.exploreMode.getResources ( ).getDrawable (R.drawable.flag_grey);
		this.greyFlagDark = (BitmapDrawable) this.exploreMode.getResources ( ).getDrawable (R.drawable.flag_grey_dark);

		this.flagUnderAttack = (BitmapDrawable) this.exploreMode.getResources ( ).getDrawable (R.drawable.battle);
	}


	private void drawFlag (final Canvas canvas, final int x, final int y, final int own, final boolean near, final byte state)
	{
		BitmapDrawable flag;

		if (state == Protocol.StateUnderAttack)
		{
			flag = this.flagUnderAttack;
			flag.setBounds (x - 8, y - 8, x + 8, y + 8);
		}
		else
		{
			switch (own)
			{
				case Color.BLUE:
					flag = near ? this.blueFlag : this.blueFlagDark;
				break;

				case Color.RED:
					flag = near ? this.redFlag : this.redFlagDark;
				break;

				case Color.GREEN:
					flag = near ? this.greenFlag : this.greenFlagDark;
				break;

				default:
					flag = near ? this.greyFlag : this.greyFlagDark;
				break;
			}
			flag.setBounds (x - 1, y - 24 + 1, x + 16 - 1, y + 1);
		}
		// Its a 16x24 pixel image (+1 offset so that center of the flags pole is at the correct coordinates)
		flag.draw (canvas);
	}


	@Override
	public void draw (final Canvas canvas, final MapView mapView, final boolean shadow)
	{
		//
		// If shadows are on, we will not be visible. It is therefore needed to draw only when shadows are off.
		//
		if (shadow == false)
		{
			Log.d ("IU", "Drawing map overlay ...");
			
			int screenWidth = canvas.getWidth ( );
			int screenHeight = canvas.getHeight ( );
	
			FogOfWar fow = this.exploreMode.fogOfWar;
	
			Commander myCommander = this.exploreMode.locationUpdater.getCommander ( );
			myCommander.nearestFlag = null;
			double minDistance = 100000.0;
	
			//
			// Get the two extreme locations on screen (bottom/left and top/right)
			//
			int lon = mapView.getLongitudeSpan ( );
			int lat = mapView.getLatitudeSpan ( );
	
			GeoPoint mapCenter = mapView.getMapCenter ( );
	
			Location mapCenterLocation = new Location (LocationManager.GPS_PROVIDER);
			mapCenterLocation.setLongitude (mapCenter.getLongitudeE6 ( ) * 0.000001d);
			mapCenterLocation.setLatitude (mapCenter.getLatitudeE6 ( ) * 0.000001d);
	
			GeoPoint bottomLeftPoint = new GeoPoint (mapCenter.getLatitudeE6 ( ) - (lat >> 1), mapCenter.getLongitudeE6 ( ) - (lon >> 1));
			GeoPoint topRightPoint   = new GeoPoint (mapCenter.getLatitudeE6 ( ) + (lat >> 1), mapCenter.getLongitudeE6 ( ) + (lon >> 1));
	
			// Get the tile index ranges of the tiles that we have to try to draw
			fow.getScreenBoundsTiles (bottomLeftPoint, topRightPoint, this.boundsIndex);
	
			// If no fog cleared yet just draw a dark fog everywhere
			if (this.boundsIndex.left > this.boundsIndex.right || this.boundsIndex.top < this.boundsIndex.bottom)
			{
				canvas.drawRect (0, 0, screenWidth, screenHeight, this.fogPaint);
			}
	
			//
			// Get the grid bounds of the rectangle of the map
			//
			Point scrXY = new Point ( );
	
			mapView.getProjection ( ).toPixels (this.exploreMode.gameWorld.minPoint ( ), scrXY);
			int x1 = scrXY.x;
			int y1 = scrXY.y;
	
			mapView.getProjection ( ).toPixels (this.exploreMode.gameWorld.maxPoint ( ), scrXY);
			int x2 = scrXY.x;
			int y2 = scrXY.y;
	
			int tdx = x2 - x1;
			int tdy = y2 - y1;
	
			Location playerLocation = myCommander.player.mapLocation;
	
			// If location already acquired
			if (playerLocation != null)
			{
				//
				// Display flag locations for the tiles that we can see
				//
	
				ArrayList<Flag> flags;
				Flag flag;
				int flagCount;
				int xa, xb, fdx, ya, yb, fdy, fx1, fx2, fy1, fy2;
	
				final int flagDrawSize = GeoLocationOverlay.FLAG_DRAW_SIZE;
	
				//
				// First draw the fog outside of the bounding box of explored tiles of the fog of war
				//
	
				// Draw the rest of the fog around the bounding box
				int left = x1 + ((tdx * (this.boundsIndex.left)) >> fow.MAP_TILE_COUNT_POW);
				int right = x1 + ((tdx * (this.boundsIndex.right + 1)) >> fow.MAP_TILE_COUNT_POW);
				int bottom = y1 + ((tdy * (this.boundsIndex.bottom)) >> fow.MAP_TILE_COUNT_POW);
				int top = y1 + ((tdy * (this.boundsIndex.top + 1)) >> fow.MAP_TILE_COUNT_POW);
	
				// Draw fog outside of the map
				if (!this.debug)
				{
					canvas.drawRect (0, 0, screenWidth, top, this.fogPaint); // Top
					canvas.drawRect (0, bottom, screenWidth, screenHeight, this.fogPaint); // Bottom
					canvas.drawRect (0, top, left, bottom, this.fogPaint); // Left
					canvas.drawRect (right, top, screenWidth, bottom, this.fogPaint); // Right
				}
				else
				{
					// Blue bounding box rectangle of the tiles already explored
					canvas.drawRect (left - 2, top - 2, right + 2, bottom + 2, this.boundingBoxPaint);
	
					// Green tile grid
					for (int i = 0; i <= fow.MAP_TILE_COUNT; i++)
					{
						int xo = (tdx * i) >> fow.MAP_TILE_COUNT_POW;
						int yo = (tdy * i) >> fow.MAP_TILE_COUNT_POW;
	
						// Hor
						canvas.drawLine (x1, y1 + yo, x2, y1 + yo, this.gridPaint);
	
						// Ver
						canvas.drawLine (x1 + xo, y1, x1 + xo, y2, this.gridPaint);
					}
	
					canvas.drawRect (x1, y2, x2, y1, this.gridPaint);
				}
	
				//
				// Check each tile and then each field of a partially explored tile
				//
	
				// for (int tj = this.boundsIndex.bottom; tj <= this.boundsIndex.top; tj++)
				for (int tj = this.boundsIndex.top; tj >= this.boundsIndex.bottom; tj--)
				{
					// Top and bottom tile coordinates on screen
					ya = y1 + ((tdy * tj) >> fow.MAP_TILE_COUNT_POW);
					yb = y1 + ((tdy * (tj + 1)) >> fow.MAP_TILE_COUNT_POW);
					fdy = yb - ya;
	
					for (int ti = this.boundsIndex.right; ti >= this.boundsIndex.left; ti--)
					{
						// Left and right tile coordinates on screen
						xa = x1 + ((tdx * ti) >> fow.MAP_TILE_COUNT_POW);
						xb = x1 + ((tdx * (ti + 1)) >> fow.MAP_TILE_COUNT_POW);
						fdx = xb - xa;
	
						//
						// Draw fogy tiles and fields
						//
						if (fow.isTileFoggy (ti, tj))
						{
							// If tile completely foggy then draw a black rectangle
							if (!this.debug)
							{
								canvas.drawRect (xa, yb, xb, ya, this.fogPaint);
							}
							else
							{
								// If debug mode then draw a blue box
								canvas.drawRect (xa + 3, ya - 3, xb - 2, yb + 3, this.redBoxPaint);
							}
						}
						else if (fow.isTilePartial (ti, tj))
						{
							// If tile is partially foggy then draw each fogged tile separately
	
							for (int fi = 0; fi < FogOfWar.TILE_SIZE; fi++)
							{
								fx1 = xa + ((fdx * fi) >> FogOfWar.TILE_SIZE_POW);
								fx2 = xa + ((fdx * (fi + 1)) >> FogOfWar.TILE_SIZE_POW);
	
								for (int fj = 0; fj < FogOfWar.TILE_SIZE; fj++)
								{
									if (!fow.isCleared (ti, tj, fi, fj))
									{
										fy1 = ya + ((fdy * fj) >> FogOfWar.TILE_SIZE_POW);
										fy2 = ya + ((fdy * (fj + 1)) >> FogOfWar.TILE_SIZE_POW);
	
										// Draw a dark box in the place of this unexplored field
										if (!this.debug)
										{
											canvas.drawRect (fx1, fy2, fx2, fy1, this.fogPaint);
										}
										else
										{
											// If debug mode then draw blue box (if well seen / if big enough)
											if (fx2 - fx1 > 3)
											{
												canvas.drawRect (fx1 + 1, fy2 + 1, fx2 - 1, fy1 - 1, this.boundingBoxPaint);
											}
										}
									}
								}
							}
						}
	
						//
						// If this tile is not completely foggy then try to draw the flags on it
						//
						if (!fow.isTileFoggy (ti, tj))
						{
							flags = fow.getTileFlags (ti, tj);
	
							if (flags != null)
							{
								flagCount = flags.size ( );
	
								// For every flag on this tile
								for (int n = 0; n < flagCount; n++)
								{
									flag = flags.get (n);
									ExplorePlayer flagOwner = flag.getOwner ( );
	
									// Is field where this flag is standing cleared then draw the flag
									if (fow.isCleared (ti, tj, flag.fovI, flag.fovJ))
									{
	
										//calculator.getPointXY (flags.get (n).point, scrXY);
										mapView.getProjection ( ).toPixels (flags.get (n).point, scrXY);
	
										// If flag seen on screen
										if (this.isOnScreen (scrXY, flagDrawSize, screenWidth, screenHeight))
										{
											String flagText = flagOwner.getName ( );
											int color;
											boolean near;
	
											if (flagOwner == myCommander.player)
											{
												flagText += " (" + flag.getNumHovercraft ( ) + "," + flag.getNumTanks ( ) + "," + flag.getNumArtillery ( ) + ")";
												color = Color.BLUE; // Me
											}
											else if (flagOwner.getId ( ) == 0)
											{
												color = Color.GREEN; // AI
											}
											else
											{
												color = Color.RED; // Them
											}
	
											// double dist = CoordinateTranslation.distance(myCommander.mapLocation, flag);
											double distCenter = CoordinateTranslation.distance (mapCenterLocation, flag);
											double distCommander = CoordinateTranslation.distance (myCommander.player.mapLocation, flag);
	
											// If close enough to commander or if my flag
											if (distCommander < Commander.getViewDistance ( ) || flagOwner == myCommander.getPlayer ( ))
											{
												near = true;
												// If new closest flag
												if (distCenter < minDistance)
												{
													myCommander.nearestFlag = flag;
													minDistance = distCenter;
												}
	
												// Draw text next to flag
												this.nearFlagTextPaint.setColor (color);
												canvas.drawText (flagText, scrXY.x + flagDrawSize, scrXY.y + flagDrawSize, this.nearFlagTextPaint);
											}
											else
											{
												near = false;
												// Draw text next to flag
												this.farFlagTextPaint.setColor (color);
												canvas.drawText (flagText, scrXY.x + flagDrawSize, scrXY.y + flagDrawSize, this.farFlagTextPaint);
											}
	
											byte state = flag.getState ( );
											this.drawFlag (canvas, scrXY.x, scrXY.y, color, near, state);
										}
									}
								}
							}
						}
	
					}
				}
	
				//
				// Display players locations
				//
	
				int playerCount = PlayerRegistry.getPlayerCount ( );
	
				for (int i = 0; i < playerCount; i++)
				{
					ExplorePlayer player = PlayerRegistry.getPlayer (i);
	
					// If player is logged in and is in out explored area
					if (player.isActive ( ) && fow.isExplored (player.mapPoint))
					{
						//calculator.getPointXY (player.mapPoint, scrXY);
						mapView.getProjection ( ).toPixels (player.mapPoint, scrXY);
	
						player.paint (canvas, 
											scrXY.x, 
											scrXY.y, 
											(player == myCommander.player ? GeoLocationOverlay.cmdBlueColor : GeoLocationOverlay.cmdRedColor));
					}
				}
	
				// Paint the nearest flag yellow
				if (myCommander.nearestFlag != null)
				{
					//calculator.getPointXY (myCommander.nearestFlag.point, scrXY);
					mapView.getProjection ( ).toPixels (myCommander.nearestFlag.point, scrXY);
	
					canvas.drawCircle (scrXY.x, scrXY.y, 6, this.selectedFlagPaint);
	
					int color;
					if (myCommander.nearestFlag.getOwner ( ) == myCommander.player)
					{
						color = Color.BLUE; // Me
					}
					else if (myCommander.nearestFlag.getOwner ( ).getId ( ) == 0)
					{
						color = Color.GREEN; // AI
					}
					else
					{
						color = Color.RED; // Them
					}
	
					this.drawFlag (canvas, scrXY.x, scrXY.y, color, true, myCommander.nearestFlag.getState ( ));
				}
			}
		}
		
		super.draw (canvas, mapView, shadow);
	}


	/**
	 * 
	 * @param coords
	 * @param size
	 * @param width
	 * @param height
	 * @return
	 */
	public boolean isOnScreen (final Point coords, final int size, final int width, final int height)
	{
		return (coords.x + size >= 0 && coords.x - size <= width && coords.y + size >= 0 && coords.y - size < height);

	}


	public void toggleDebugMode ( )
	{
		this.debug = !this.debug;
		
		//
		// Redraw the map (i.e. refresh it!)
		//
		Message msg = this.exploreMode.handler.obtainMessage (ExploreMode.REFRESH_MAP_VIEW);
		msg.sendToTarget ( );
	}
}
