package iu.android.control;


/**
 * Allows non-game classes to hook in to the main thread, for synchronous input. Needed for say mouse input,
 * when you need to check where the units in a game are, in relation to the mouse.
 */
public interface InputDevice
{
	/**
	 * Processes input (touch, mouse, keys, ...) read from the current view
	 */
	public void doInput ( );	
}
