package iu.android.comm;

import android.content.Context;
import android.content.Intent;

public class IntentFactory
{
	// public static final String Type = "Type";
	// public static final String Type_Location = "l";
	// public static final String Type_Notification = "n";

	public static Intent createSendLocationIntent (final Context context/* , final GameWorld world */)
	{
		Intent intent = new Intent ( );
		intent.setClass (context, CommunicationService.class);

		return intent;
	}

	// public static Intent createNotificationIntent (final Context context, final Notification notification,
	// final long ttl)
	// {
	// Intent intent = new Intent ( );
	// intent.setClass (context, CommunicationService.class);
	// Bundle extras = new Bundle ( );
	// extras.putString (IntentFactory.Type, IntentFactory.Type_Notification);
	// BundleUtils.putNotification (notification, extras);
	// BundleUtils.putNotificationTTL (ttl, extras);
	// intent.putExtras (extras);
	// return intent;
	// }
}
