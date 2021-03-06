/*
 *  Player Java Client - WiFiInterface.java
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
 * $Id: WiFiInterface.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient;

/**
 * The wifi interface provides access to the state of a wireless network interface.
 * This interface accepts no commands.
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class WiFiInterface extends PlayerDevice {

    private final short PLAYER_WIFI_CODE = PlayerClient.PLAYER_WIFI_CODE; /* wifi card status */

    private short  linkCount = 0;       /* length of said list */
    private int    throughput;          /* mysterious throughput calculated by driver */
    private int    bitrate;             /* current bitrate of device */
    private byte   mode;                /* operating mode of device */
    private byte   qualType;            /* indicates type of link quality info we have */
    private short  maxQual;             /* maximum values for quality, level and noise */
    private short  maxLevel;
    private short  maxNoise;
    private char[] ap = new char[32];   /* MAC address of current access point/cell */
    /**
     * Constructor for WiFiInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public WiFiInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_WIFI_CODE;
        index     = indexOfDevice;
    }
    
    /**
     * Read the WiFi information.
     */
    public synchronized void readData () {
        readHeader ();
        try {
            linkCount  = (short)is.readUnsignedShort ();    /* length of said list */
            throughput = is.readInt ();                     /* throughput */
            bitrate    = is.readInt ();                     /* current bitrate of device */
            mode       = (byte)is.readUnsignedByte ();      /* operating mode of device */
            qualType   = (byte)is.readUnsignedByte ();      /* type of link quality info */
            maxQual    = (short)is.readUnsignedShort ();    /* maximum values for quality */
            maxLevel   = (short)is.readUnsignedShort ();    /* maximum values for level */
            maxNoise   = (short)is.readUnsignedShort ();    /* maximum values for noise */
            for (int i = 0; i < 32; i++)
            	ap[i] = is.readChar ();     /* MAC address of current access point/cell */
        } catch (Exception e) {
            System.err.println ("[WiFi] : Error when reading payload: " + e.toString ());
        }
    }
}
