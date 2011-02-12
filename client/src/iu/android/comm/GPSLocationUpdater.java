package iu.android.comm;

public class GPSLocationUpdater extends Thread
{
	long	updateTime;
	boolean running;
	boolean pause;


	public GPSLocationUpdater (long updateTime)
	{
		this.updateTime = updateTime;
		this.running = true;
		this.pause = false;
	}


	@Override
	public void run ( )
	{
		while (this.running)
		{
			if (!this.pause)
			{
				// CommunicationService.
			}

			try
			{
				Thread.sleep (this.updateTime);
			}
			catch (InterruptedException ex)
			{
				// TODO Auto-generated catch block
				ex.printStackTrace ( );
			}
		}
	}
}
