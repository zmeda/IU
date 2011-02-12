/**
 * 
 */
package iu.android.comm;

import java.util.ArrayList;

class Worker extends Thread
{

	/** How long this thread will sleep before re-checking its queue */
	private static final long					Lazyness	= 200;

	private boolean								running;
	private ArrayList<ICommunicationTask>	fifo;


	public Worker ( )
	{
		super ( );
		this.fifo = new ArrayList<ICommunicationTask> ( );
	}


	public boolean isRunning ( )
	{
		return this.running;
	}


	public void push (ICommunicationTask task)
	{
		synchronized (this.fifo)
		{
			this.fifo.add (task);
		}
	}


	@Override
	public void run ( )
	{
		while (this.running)
		{
			if (this.fifo.isEmpty ( ))
			{
				try
				{
					Thread.sleep (Worker.Lazyness);
				}
				catch (InterruptedException e)
				{
					// Nothing wrong with being interrupted here
				}
			}
			else
			{
				ICommunicationTask task = null;

				// Pop first from the queue and execute it
				synchronized (this.fifo)
				{
					task = this.fifo.remove (0);
				}

				if (task != null)
				{
					task.execute ( );
				}
			}
		}
	}


	@Override
	public synchronized void start ( )
	{
		this.running = true;
		super.start ( );
	}


	/** Use this to stop the worker thread */
	public void requestStop ( )
	{
		this.running = false;
	}
}