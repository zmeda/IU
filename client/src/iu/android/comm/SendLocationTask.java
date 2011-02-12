package iu.android.comm;

import android.location.Location;

public class SendLocationTask implements ICommunicationTask
{

	private ILocationSender	sender;
	// private ILocationTransform transform;
	private Location			inGame;


	public SendLocationTask (final ILocationSender sender, /* final ILocationTransform transform, */
	final Location inGame)
	{
		super ( );
		this.sender = sender;
		// this.transform = transform;
		this.inGame = inGame;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see iu.android.comm.ICommunicationTask#execute()
	 */
	public void execute ( )
	{
		// if (this.transform != null)
		// {
		// Location inGame = this.transform.toGame (this.inGame);
		this.sender.sendLocation (this.inGame);
		// }
	}
}
