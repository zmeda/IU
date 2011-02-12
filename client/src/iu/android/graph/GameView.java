package iu.android.graph;

import iu.android.battle.BattleActivity;
import iu.android.battle.BattleEngine;
import iu.android.control.AccelerometerListener;
import iu.android.control.InputDevice;
import iu.android.control.KeyControl;
import iu.android.control.MotionControl;
import iu.android.map.Map;
import iu.android.network.battle.LocalPlayer;
import iu.android.order.AttackPositionOrder;
import iu.android.order.AttackUnitOrder;
import iu.android.order.GuardOrder;
import iu.android.order.MoveOrder;
import iu.android.order.Order;
import iu.android.unit.Squad;
import iu.android.unit.Unit;
import iu.android.unit.Vector2d;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;




/**
 * This class is a bit of a mess, the weird stuff with the mouse events is for thread safeness. The drawing
 * uses "dirty rectangles" where-by only the bare minimum (well nearly) of blt-ing needs to be done.
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Renderer, InputDevice
{
	//
	// The resolution needed for the game to work
	//
	private static final int RESOLUTION_WIDTH  = 320;
	private static final int RESOLUTION_HEIGHT = 455;
	
	//
	// The area of the battle field that is currently visible
	//
	private Rect									clipArea						= null;

	//
	// Screen movement in the last frame
	//
	private int										dx								= 0;
	private int										dy								= 0;
	private int										cx								= 0;
	private int										cy								= 0;

	//
	// Every movement step of the window over the map
	//
	private int										moveLen						= 13;
	
	private Map										map							= null;
	private LocalPlayer							player						= null;

	private ArrayList<MarkerGraphic>			markerGraphics				= new ArrayList<MarkerGraphic> ( );
	private ArrayList<UnitMarkerGraphic>	unitMarkerGraphics		= new ArrayList<UnitMarkerGraphic> ( );

	private SelectedUnits						selectedUnits				= null;

	private boolean								firstDraw					= true;
	
	private BattleEngine							game							= null;
	
	private Handler								battleActivityMessageHandler;

	private GameCanvasRenderer					renderer						= null;

	
	/*
	 * The zooming functionality uses some local members which are declared here.-
	 */
	//
	// If between zoom in and zoom out
	//
	private boolean								zooming					= false;

	//
	// Direction of the current/last zoom
	//
	private boolean								zoomedIn					= true;

	private static float							zoomInFactor			= 1.0f;
	private static float							zoomOutFactor			= 0.60f;
	private static long							zoomTime					= 2000;

	private float									zoomStartFactor;
	private float									zoomEndFactor;
	private long									startZoomTime;
	private long									endZoomTime;

	private float									zoomFactor				= GameView.zoomInFactor;
	private float									scaleFactor				= 1.0f / GameView.zoomInFactor;
	
	
	/**
	 * Starts processing the zooming effect for the battle field.-
	 */
	public void startZooming ( )
	{
		if (this.zooming)
		{
			return;
		}

		this.startZoomTime = System.currentTimeMillis ( );
		this.endZoomTime = this.startZoomTime + GameView.zoomTime;

		if (this.zoomedIn)
		{
			this.zoomStartFactor = GameView.zoomInFactor;
			this.zoomEndFactor = GameView.zoomOutFactor;
		}
		else
		{
			this.zoomStartFactor = GameView.zoomOutFactor;
			this.zoomEndFactor = GameView.zoomInFactor;
		}

		this.zooming = true;
	}


	/**
	 * Does the zooming of the battle field
	 */
	private void integrateZoom ( )
	{
		long time = System.currentTimeMillis ( );

		if (time > this.endZoomTime)
		{
			this.zoomFactor = this.zoomEndFactor;
			this.scaleFactor = 1.0f / this.zoomFactor;

			this.zoomedIn = !this.zoomedIn;
			this.zooming = false;
		}
		else
		{
			// between 0.0 and 1.0
			float d = ((float) (time - this.startZoomTime)) / GameView.zoomTime;

			this.zoomFactor = this.zoomStartFactor + (this.zoomEndFactor - this.zoomStartFactor) * d;
			this.scaleFactor = 1.0f / this.zoomFactor;
		}
	}	


	/**
	 * Updates the screen position, recalculating the clipped (i.e. viewable) area of the battle field.- 
	 */
	private void updateScreenPosition ( )
	{
		// Update position
		this.cx += (int) (this.dx * this.scaleFactor);
		this.cy += (int) (this.dy * this.scaleFactor);

		this.dx = 0;
		this.dy = 0;

		int w2 = (int) (this.getWidth ( ) * this.scaleFactor) >> 1;
		int h2 = (int) (this.getHeight ( ) * this.scaleFactor) >> 1;

		int size = this.map.pixelCount ( ) - 1;

		// Setup the center of screen still on the map
		this.cx = (this.cx - w2 < 0 ? w2 : (this.cx + w2 > size ? size - w2 : this.cx));
		this.cy = (this.cy - h2 < 0 ? h2 : (this.cy + h2 > size ? size - h2 : this.cy));

		// Setup clip area
		this.clipArea.left = this.cx - w2;
		this.clipArea.right = this.cx + w2;
		this.clipArea.top = this.cy - h2;
		this.clipArea.bottom = this.cy + h2;
		
		Log.d ("IU - GameView", "ClipArea updated " + this.clipArea.toString ( ));
	}
	
	
	/**
	 * Creates a new View
	 * 
	 * IMPORTANT: This constructor is used to initialize {@link Managers} Once managers have been initialized
	 * they cannot be initialized again. Trying to reinitialize the {@link Managers} will cause a
	 * {@link RuntimeException} to be thrown.
	 * 
	 * For that reason only one instance can be created.
	 * 
	 * @param context
	 * @param attrs
	 */
	public GameView (final Context context, final AttributeSet attrs)
	{
		super (context, attrs);

		//
		// Register our interest in hearing about changes to our surface
		//
		SurfaceHolder holder = this.getHolder ( );
		holder.addCallback (this);

		holder.setKeepScreenOn (true);
		
		//
		// Make sure we get key events
		//
		this.setFocusable (true);
	}


	/**
	 * Initializes the canvas and leaves it ready to start drawing on it.-
	 * 
	 * @param game
	 */
	public void init (final BattleEngine game)
	{
		ImageManager.setComponent (this);

		this.game = game;

		this.map = game.getMap ( );

		this.player = game.getUser ( );

		this.selectedUnits = new SelectedUnits (this.player);

		this.renderer = new GameCanvasRenderer (game);

		// Clip area
		this.clipArea = new Rect (0, 0, this.getWidth ( ), this.getHeight ( ));
		
		//
		// Save a reference to this view in the class reading input events from keys.
		// This will ensure synchronization between the drawing thread and the main one.
		//
		KeyControl.setBattleView (this);

		//
		// Save a reference to this view in the class reading touch events.
		// This will ensure synchronization between the drawing thread and the main one.
		//
		MotionControl.setSurfaceHolder (this.getHolder ( ));
		
		//
		// Same as above holds for the accelerometer listener
		//
		AccelerometerListener.setBattleView (this);
	}


	
	/**
	 * Sends the order 'order' to the unit 'unit'.-
	 * 
	 * @param unit
	 * @param order
	 */
	public void dispatchOrder (final Unit unit, final Order order)
	{
		if (order.getType ( ) == MoveOrder.TYPE)
		{
			MoveOrder move_order = (MoveOrder) order;
			this.markerGraphics.add (MarkerGraphic.getNewCircleMarkerGraphic (Color.BLACK, move_order.getX ( ), move_order.getY ( ), true));
		}
		else if (order.getType ( ) == AttackUnitOrder.TYPE)
		{
			AttackUnitOrder attackOrder = (AttackUnitOrder) order;

			Unit attacked = this.game.getPlayerByID (attackOrder.getAttackPlayerID ( )).getUnit (attackOrder.getAttackUnitID ( ));

			this.unitMarkerGraphics.add (UnitMarkerGraphic.getNewUnitMarkerGraphic (attacked));
		}
		else if (order.getType ( ) == AttackPositionOrder.TYPE)
		{
			AttackPositionOrder attackOrder = (AttackPositionOrder) order;
			this.markerGraphics.add (MarkerGraphic.getNewCircleMarkerGraphic (Color.RED, attackOrder.getX ( ), attackOrder.getY ( ), false));
		}
		else if (order.getType ( ) == GuardOrder.TYPE)
		{
			GuardOrder guardOrder = (GuardOrder) order;
			this.markerGraphics.add (MarkerGraphic.getNewSquareMarkerGraphic (Color.GREEN, guardOrder.getX ( ), guardOrder.getY ( ), true));
		}

		this.player.dispatchOrder (order);
	}


	public void dispatchMoveOrder (final Unit unit, final short x, final short y)
	{
		MoveOrder order = MoveOrder.getNewMoveOrder (this.player.getID ( ), unit.getID ( ), x, y);

		this.dispatchOrder (unit, order);
	}

	
	/**
	 * This method does the actual drawing.-
	 * 
	 * @see iu.android.graph.Renderer#requestRender()
	 */
	public void requestRender (Canvas canvas)
	{
		Map mMap = this.map;
		
		if (mMap == null)
		{
			return;
		}

		if (this.firstDraw)
		{
			int[] coord = new int[2];

			this.player.calculateAverageUnitPosition (coord);

			this.cx = coord[0];
			this.cy = coord[1];
			
			this.updateScreenPosition ( );
			
			mMap.getClippedTiles (this.renderer.getDrawableIndexRect ( ), this.clipArea);

			this.firstDraw = false;
		}

		//
		// Draw the battle field zooming effect
		//
		if (this.zooming)
		{
			this.integrateZoom ( );

			//
			// Update the screen position and clipping area
			//
			this.updateScreenPosition ( );
		}

		//
		// Do actual drawing
		//
		canvas.save ( );

		canvas.scale (this.zoomFactor, this.zoomFactor);

		canvas.translate (-this.clipArea.left, -this.clipArea.top);

		this.renderer.renderFullScreen (mMap, this.selectedUnits, this.markerGraphics, this.unitMarkerGraphics, canvas, this.clipArea, this.scaleFactor);

		canvas.restore ( );
	}


	
	///////////////////////////////////////////////////////////////////////////
	//
	// Input event setup and handling
	//
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Moves the screen horizontally over the map.-
	 * 
	 * @param left If true, moves the screen towards the left. To the right otherwise.-
	 */
	public void moveScreenHorizontally (boolean left)
	{
		if (left)
		{
			this.dx -= this.moveLen;
		}
		else
		{
			this.dx += this.moveLen;
		}
		
		//
		// Update the location of the screen
		//
		this.updateScreenPosition ( );
	}
	
	
	/**
	 * Moves the screen with the given speed.-
	 * 
	 * @param speed The speed also defines left or right (i.e. negative or positive values).
	 */
	public void moveScreenHorizontally (float speed)
	{
		this.dx += this.moveLen * speed;
		
		//
		// Update the location of the screen
		//
		this.updateScreenPosition ( );
	}

	
	/**
	 * Moves the screen vertically over the map.-
	 * 
	 * @param up If true, moves the screen upwards. Downwards otherwise.-
	 */
	public void moveScreenVertically (boolean up)
	{
		if (up)
		{
			this.dy -= this.moveLen;
		}
		else
		{
			this.dy += this.moveLen;
		}

		//
		// Update the location of the screen
		//
		this.updateScreenPosition ( );
	}

	
	/**
	 * Moves the screen with the given speed.-
	 * 
	 * @param speed The speed also defines upwards or downwards (i.e. negative or positive values).
	 */
	public void moveScreenVertically (float speed)
	{
		this.dy += this.moveLen * speed;
		
		//
		// Update the location of the screen
		//
		this.updateScreenPosition ( );
	}

	
	/**
	 * Selects a group of units (a squad), that is identified by numbers (from 1 to 3).-
	 * 
	 * @param i
	 * @param shiftDown
	 */
	public void selectSquad (int i, boolean shiftDown)
	{
		if (i == 0)
		{
			i = 9;
		}
		else
		{
			i--;
		}

		if (i < this.player.getNumSquads ( ))
		{
			Squad squad = this.player.getSquad (i);
	
			if (squad.allDead ( ))
			{
				return;
			}
	
			if (!shiftDown)
			{
				this.selectedUnits.deselect ( );
			}
	
			int squadUnitNum = squad.getNumUnits ( );
			for (int j = 0; j < squadUnitNum; j++)
			{
				Unit unit = squad.getUnit (j);
				if (!unit.isDead ( ))
				{
					this.selectedUnits.addUnit (unit);
				}
			}
		}
	}
	
	
	/**
	 * Sends the guard order to the currently selected units.
	 * Happens whenever the user hits the 'G' key.-
	 */
	public void dispatchGuardOrder ( )
	{
		ArrayList<Unit> unitsList = this.selectedUnits.getSelectedUnits ( );

		for (Unit selectedUnit : unitsList)
		{
			Vector2d pos = selectedUnit.getPosition();
			GuardOrder order = GuardOrder.getNewGuardOrder(this.player.getID(), selectedUnit.getID(), (short) (pos.x), (short) (pos.y));
			this.dispatchOrder(selectedUnit, order);
		}
	}

	
	/**
	 * Processes events coming from the user.-
	 */
	public void doInput ( )
	{
		//
		// FIXME - Performance: This should happen ONLY when a unit dies!
		//
		this.selectedUnits.removeDeadUnits ( );

		//
		// Process the mouse events only if anything new happened
		//
		//
		// Check if the drag gesture is happening
		//
		if (MotionControl.hasBeenDragged ( ) && (!MotionControl.getSelectionArea ( ).isEmpty ( )))
		{
			Rect selectionArea = MotionControl.getSelectionArea ( );
			
			//
			// Adapt the selection coordinates to the clipped area we are rendering
			//
			int offsetX = (int) (MotionControl.getClickX ( ) * scaleFactor);
			int offsetY = (int) (MotionControl.getClickY ( ) * scaleFactor);
			
			offsetX += clipArea.left;
			offsetY += clipArea.top;

			selectionArea.offsetTo (offsetX, offsetY);
			
			//
			// Multiple unit selection
			//
			boolean selection = false;

			ArrayList<Unit> allUnits = this.renderer.getVisibleUnits ( );
			int unitCount = allUnits.size ( );

			for (int j = 0; j < unitCount; j++)
			{
				Unit unitj = allUnits.get (j);
				Vector2d pos = unitj.getPosition ( );
				if (unitj.getPlayer ( ) == this.player && selectionArea.contains ((int) (pos.x), (int) (pos.y)))
				{
					selection = true;
					break;
				}
			}

			if (selection)
			{
				if (!KeyControl.isShiftDown ( ))
				{
					this.selectedUnits.deselect ( );
				}

				for (int j = 0; j < unitCount; j++)
				{
					Unit unitj = allUnits.get (j);
					Vector2d pos = unitj.getPosition ( );
					if (unitj.getPlayer ( ) == this.player && selectionArea.contains ((int) (pos.x), (int) (pos.y)))
					{
						this.selectedUnits.addUnit (unitj);
						unitj.setMoved (true);
					}
				}
			}
		}
		//
		// Check if the screen has been touched (or clicked)
		//
		else if (MotionControl.hasBeenClicked ( ))
		{
			int  			  	currentLocationX  = (int) (MotionControl.getClickX ( ) * this.scaleFactor);
			int  			  	currentLocationY  = (int) (MotionControl.getClickY ( ) * this.scaleFactor);
			Unit 				unit 					= null;		
			SelectedUnits 	thisSelectedUnits = this.selectedUnits;
	
			//
			// Adapt the current coordinate to the clipped area we are rendering
			//
			currentLocationX += this.clipArea.left;
			currentLocationY += this.clipArea.top;
		
			//
			// Get the unit under the mouse pointer
			//
			unit = this.map.getUnitAt (currentLocationX, currentLocationY);
			
			if (unit != null)
			{
				//
				// Is it a friendly unit?
				//
				if (unit.getPlayer ( ) == this.player)
				{
					if (!KeyControl.isShiftDown ( ))
					{
						thisSelectedUnits.deselect ( );
					}
					unit.setMoved (true);
					thisSelectedUnits.addUnit (unit);
				}
				else 
				{
					//
					// The user clicked on an enemy unit
					//
					thisSelectedUnits.dispatchAttackOrder (this, unit);
				}
			}
			else
			{
				//
				// There is no unit under the mouse cursor, just move the selected unit(s) there ...
				//
				thisSelectedUnits.dispatchMoveOrders (this, currentLocationX, currentLocationY);
			}
		}		
	}

	
	@Override
	public boolean onTouchEvent (MotionEvent event)
	{
		return MotionControl.onTouchEvent (event);
	}
	
	
	@Override
	public boolean onKeyDown (int keyCode, final KeyEvent event)
	{
		return KeyControl.onKeyDown (keyCode, event);
	}


	@Override
	public boolean onKeyUp (int keyCode, final KeyEvent event)
	{
		return KeyControl.onKeyUp (keyCode, event);
	}

	
	/**
	 * Callback invoked when the surface dimensions change.-
	 */
	public void surfaceChanged (SurfaceHolder holder, int format, int width, int height)
	{
		Log.d ("IU - GameView", "Surface changed its size to " + String.valueOf (width) + " x " + String.valueOf (height));
		
		//
		// Check we support the surface dimensions
		//
		if ((width != GameView.RESOLUTION_WIDTH) || (height != GameView.RESOLUTION_HEIGHT))
		{
			//
			// Inform the user that the resolution is not supported.-
			//
			Message msg = this.battleActivityMessageHandler.obtainMessage (BattleActivity.SHOW_MESSAGE,
																								new String ("Resolution not supported!"));
			msg.sendToTarget ( );
		}
	}

	
	/**
	 * Callback invoked right before the surface is available for drawing.-
	 */
	public void surfaceCreated (SurfaceHolder holder)
	{
		Log.d ("IU - GameView", "Surface created!");

		//
		// The surface is ready ... let's start the battle
		//
		Message msg = this.battleActivityMessageHandler.obtainMessage (BattleActivity.START_BATTLE);
		msg.sendToTarget ( );
	}


	/**
	 * Callback invoked right before the surface is deleted from memory.-
	 */
	public void surfaceDestroyed (SurfaceHolder holder)
	{
		//
		// FIXME: We should shut down the drawing thread to prevent NullPointerExceptions
		//
	}


	/**
	 * Returns a reference to the surface holder of this surface view.-
	 * 
	 * @see iu.android.graph.Renderer#requestRender()
	 */
	public SurfaceHolder getSurfaceHolder ( )
	{
		return (this.getHolder ( ));
	}


	/**
	 * @param messageHandler for the BattleActivity.-
	 */
	public void setMessageHandler (Handler messageHandler)
	{
		this.battleActivityMessageHandler = messageHandler;
	}
}
