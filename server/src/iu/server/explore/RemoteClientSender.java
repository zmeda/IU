package iu.server.explore;


/**
 * 
 * 
 * @author luka
 *
 */
public class RemoteClientSender {

	RemoteClientSenderTCP tcpSender;
	RemoteClientSenderUDP udpSender;
	
	public RemoteClientSender (RemoteClientSenderTCP tcpSender, RemoteClientSenderUDP udpSender)
	{
		this.tcpSender = tcpSender;
		this.udpSender = udpSender;
	}
	
	/**
	 * Sends event trought upd or tcp connection
	 * @param event
	 */
	public void send (byte[] arr)
	{
		// check command that will be send 
		// and choose appropriate protocol
		
		if (arr[0] < 96)
		{
			// send trought tcp port
			
			//check if it is event
			if (arr[0] > 63)
			{
				this.tcpSender.sendEvent(arr);
			}
			else
			{
				this.tcpSender.sendData(arr);
			}
		}
		else
		{
			// commands from 96 are reserved for UPD 
			this.udpSender.send(arr);
		}
	}
	
	
}
