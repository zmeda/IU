package iu.android.network;

/*************************************************************************************************************
 * IU 1.0b, a java realtime strategy game
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 ************************************************************************************************************/


/**
 * TODO: [Lucas] Not sure how this one works
 */
public class StateUpdate
{
	//
	// The size of one state update in bytes.
	// It comes out from 
	//			playerID = 1, unitID = 4, x = 2, y = 2, dir = 4, vx = 4, vy = 4
	//
	public final static int STATE_UPDATE_SIZE = 21;
	
	
	private short x;
	private short y;
	private byte	playerID;
	private int		unitID;
	private int		dir;
	private int		vx;
	private int		vy;


	/**
	 * Constructor
	 * 
	 * @param pid
	 * @param unitID
	 * @param x
	 * @param y
	 * @param dir
	 * @param vx
	 * @param vy
	 */
	public StateUpdate (byte pid, int unitID, short x, short y, int dir, int vx, int vy)
	{
		this.playerID = pid;
		this.unitID = unitID;
		this.x = x;
		this.y = y;
		this.dir = dir;
		this.vx = vx;
		this.vy = vy;
	}


	/**
	 * Constructor
	 */
	public StateUpdate ( )
	{
		// Nothing to do!
	}

	public int getUnitID ( )
	{
		return this.unitID;
	}


	public byte getPlayerID ( )
	{
		return this.playerID;
	}


	public short getX ( )
	{
		return this.x;
	}


	public short getY ( )
	{
		return this.y;
	}


	public int getDirection ( )
	{
		return this.dir;
	}


	public int getVX ( )
	{
		return this.vx;
	}


	public int getVY ( )
	{
		return this.vy;
	}


	/**
	 * Write the bytes representing this order into <CODE>bytes</CODE> starting from <CODE>pos</CODE>
	 */
	public void writeBytes (byte[] bytes, int pos)
	{
		bytes[pos] = this.playerID;

		bytes[pos + 1] = (byte) ((this.unitID & 0xFF000000) >> 24);
		bytes[pos + 2] = (byte) ((this.unitID & 0x00FF0000) >> 16);
		bytes[pos + 3] = (byte) ((this.unitID & 0x0000FF00) >> 8);
		bytes[pos + 4] = (byte) ((this.unitID & 0x000000FF));

		bytes[pos + 5] = (byte) ((this.x >> 8) & 0x00FF);
		bytes[pos + 6] = (byte) (this.x & 0x00FF);

		bytes[pos + 7] = (byte) ((this.y >> 8) & 0x00FF);
		bytes[pos + 8] = (byte) (this.y & 0x00FF);

		bytes[pos + 9] = (byte) ((this.dir & 0xFF000000) >> 24);
		bytes[pos + 10] = (byte) ((this.dir & 0x00FF0000) >> 16);
		bytes[pos + 11] = (byte) ((this.dir & 0x0000FF00) >> 8);
		bytes[pos + 12] = (byte) ((this.dir & 0x000000FF));

		bytes[pos + 13] = (byte) ((this.vx & 0xFF000000) >> 24);
		bytes[pos + 14] = (byte) ((this.vx & 0x00FF0000) >> 16);
		bytes[pos + 15] = (byte) ((this.vx & 0x0000FF00) >> 8);
		bytes[pos + 16] = (byte) ((this.vx & 0x000000FF));

		bytes[pos + 17] = (byte) ((this.vy & 0xFF000000) >> 24);
		bytes[pos + 18] = (byte) ((this.vy & 0x00FF0000) >> 16);
		bytes[pos + 19] = (byte) ((this.vy & 0x0000FF00) >> 8);
		bytes[pos + 20] = (byte) ((this.vy & 0x000000FF));

	}


	public void readBytes (byte[] bytes, int pos)
	{
		this.playerID = bytes[pos];

		int byte1 = (bytes[pos + 1] & 0x000000FF) << 24;
		int byte2 = (bytes[pos + 2] & 0x000000FF) << 16;
		int byte3 = (bytes[pos + 3] & 0x000000FF) << 8;
		int byte4 = (bytes[pos + 4] & 0x000000FF);

		this.unitID = byte1 | byte2 | byte3 | byte4;

		this.x = (short) (((bytes[pos + 5] & 0x00FF) << 8) | (bytes[pos + 6] & 0x00FF));
		this.y = (short) (((bytes[pos + 7] & 0x00FF) << 8) | (bytes[pos + 8] & 0x00FF));

		byte1 = (bytes[pos + 9] & 0x000000FF) << 24;
		byte2 = (bytes[pos + 10] & 0x000000FF) << 16;
		byte3 = (bytes[pos + 11] & 0x000000FF) << 8;
		byte4 = (bytes[pos + 12] & 0x000000FF);

		this.dir = byte1 | byte2 | byte3 | byte4;

		byte1 = (bytes[pos + 13] & 0x000000FF) << 24;
		byte2 = (bytes[pos + 14] & 0x000000FF) << 16;
		byte3 = (bytes[pos + 15] & 0x000000FF) << 8;
		byte4 = (bytes[pos + 16] & 0x000000FF);

		this.vx = byte1 | byte2 | byte3 | byte4;

		byte1 = (bytes[pos + 17] & 0x000000FF) << 24;
		byte2 = (bytes[pos + 18] & 0x000000FF) << 16;
		byte3 = (bytes[pos + 19] & 0x000000FF) << 8;
		byte4 = (bytes[pos + 20] & 0x000000FF);

		this.vy = byte1 | byte2 | byte3 | byte4;

	}
}