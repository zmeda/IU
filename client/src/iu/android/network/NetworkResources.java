package iu.android.network;

import iu.android.network.explore.ExploreClient;

public class NetworkResources
{
	public static final int			UDP_SEND_PORT	= 3000;

	private static ExploreClient	client			= null;


	public static void setClient (final ExploreClient client)
	{
		NetworkResources.client = client;
	}


	public static ExploreClient getClient ( )
	{
		return NetworkResources.client;
	}

}
