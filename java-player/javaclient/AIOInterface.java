/*
 *  Player Java Client - AIOInterface.java
 *  Copyright (C) 2003-2005 Maxim A. Batalin & Radu Bogdan Rusu
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id: AIOInterface.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient;

/**
 * The aio interface provides access to an analog I/O device.
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class AIOInterface extends PlayerDevice {
	
    private final short PLAYER_AIO_CODE = PlayerClient.PLAYER_AIO_CODE;   /* analog I/O */
    
    /** maximum number of analog I/O samples */
    public static final short PLAYER_AIO_MAX_SAMPLES = 8;
    
    private int  count = 0;                                 /* number of valid samples */
    private int[] anin = new int[PLAYER_AIO_MAX_SAMPLES];   /* the samples */
    /**
     * Constructor for AIOInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public AIOInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_AIO_CODE;
        index     = indexOfDevice;
    }
    
    /**
     * Read the samples values.
     */
    public synchronized void readData () {
        readHeader ();
        try {
            count = is.readUnsignedByte ();           /* number of valid samples  */
            for (int i = 0; i < count; i++)
                anin[i] = is.readInt ();              /* the samples */
        } catch (Exception e) {
            System.err.println ("[AIO] : Error when reading payload: " + e.toString ());
        }
    }
    
    /**
     * Returns the number of valid samples
     * @return the number of valid samples as a byte
     */
    public synchronized byte getCount () { return (byte)count; }
    
    /**
     * Returns the samples values up to PLAYER_AIO_MAX_SAMPLES.
     * @return an array filled with the samples values
     */
    public synchronized int[] getAnin () { return anin; }
}
