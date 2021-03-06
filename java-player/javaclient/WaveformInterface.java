/*
 *  Player Java Client - WaveformInterface.java
 *  Copyright (C) 2005 Radu Bogdan Rusu
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
 * $Id: WaveformInterface.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient;

/**
 * The waveform interface is used to receive arbitrary digital samples, say from 
 * a digital audio device. 
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * </ul>
 */
public class WaveformInterface extends PlayerDevice {
    
    /* fetch raw waveforms */
    private final short PLAYER_WAVEFORM_CODE = PlayerClient.PLAYER_WAVEFORM_CODE;

    /** 4K - half the packet max */
    public static final short PLAYER_WAVEFORM_DATA_MAX = 4096;

    private int rate;               /* bit rate - bits per second */
    private int depth;              /* depth - bits per sample */
    private int samples;            /* samples - the number of bytes of raw data */
    /* data - an array of raw data */
    private byte[] data = new byte[PLAYER_WAVEFORM_DATA_MAX];
    
    /**
     * Constructor for WaveformInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public WaveformInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_WAVEFORM_CODE;
        index     = indexOfDevice;
    }

    /**
     * The waveform interface reads a digitized waveform from the target device.
     */
    public synchronized void readData () {
        readHeader ();
        try {
            rate    = is.readInt ();            /* bit rate - bits per second */
            depth   = is.readUnsignedShort ();  /* depth - bits per sample */
            samples = is.readInt ();            /* samples - the number of bytes of raw data */
            if (samples > PLAYER_WAVEFORM_DATA_MAX)
                samples = PLAYER_WAVEFORM_DATA_MAX;
            for (int i = 0; i < samples; i++) {
            	data[i] = (byte)is.readUnsignedByte ();    /* the array of raw data */
            }
        } catch (Exception e) {
            System.err.println ("[Waveform] : Error when reading payload: " + e.toString ());
        }
    }
    
    /**
     * Returns the current bit rate in bits per second. 
     * @return the current bit rate as an integer
     */
    public synchronized int getRate () { return this.rate; }

    /**
     * Returns the current depth in bits per sample. 
     * @return the current depth as an integer
     */
    public synchronized int getDepth () { return this.depth; }
    
    /**
     * Returns the number of samples. 
     * @return the number of bytes of raw data to follow as an integer
     */
    public synchronized int getSamples () { return this.samples; }

    /**
     * Returns the data array 
     * @return the array of raw data as an array of bytes
     */
    public synchronized byte[] getData () { return this.data; }
}
