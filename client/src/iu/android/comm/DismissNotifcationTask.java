package iu.android.comm;

import android.app.NotificationManager;

public class DismissNotifcationTask implements ICommunicationTask
{
	NotificationManager	manager;
	int						id;
	long						delay;
	


	public DismissNotifcationTask (NotificationManager manager, int id, long delay)
	{
		this.manager = manager;
		this.id = id;
		this.delay = delay;
	}


	public void execute ( )
	{
		Thread t = new Thread ( ) {
			@Override
			public void run ( )
			{
				try
				{
					Thread.sleep (DismissNotifcationTask.this.delay);
				}
				catch (InterruptedException e)
				{
					// Nothing
				}

				DismissNotifcationTask.this.manager.cancel (DismissNotifcationTask.this.id);
			}
		};
		t.start ( );
	}
}
