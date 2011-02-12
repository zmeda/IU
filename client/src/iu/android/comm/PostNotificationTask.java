package iu.android.comm;

import android.app.Notification;
import android.app.NotificationManager;

public class PostNotificationTask implements ICommunicationTask
{
	private NotificationManager manager;
	private Notification notification;
	private int id;
	private long ttl;
	
	
	/**
	 * @param manager
	 * @param notification
	 * @param id
	 * @param ttl how long until the notification is dismissed (millis). Less than 0 = never
	 */
	public PostNotificationTask (NotificationManager manager, Notification notification, int id, long ttl)
	{
		super ( );
		this.manager = manager;
		this.notification = notification;
		this.id = id;
		this.ttl = ttl;
	}


	public void execute ( )
	{
		this.manager.notify (this.id, this.notification);
		if (this.ttl >= 0) {
			new DismissNotifcationTask (this.manager, this.id, this.ttl).execute ( );
		}
	}
}
