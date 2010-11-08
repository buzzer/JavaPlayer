/*
 *  Player Java Client - PlayerDevice.java
 *  Copyright (C) 2002-2005 Maxim A. Batalin, Esben H. Ostergaard & Radu Bogdan Rusu
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
 * $Id: PlayerDevice.java 18 2005-05-25 21:55:48Z veedee $
 *
 */
package javaclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract class for all Player interfaces.
 * @author Maxim A. Batalin, Esben H. Ostergaard & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *	<li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *	<li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public abstract class PlayerDevice {

    protected final short PLAYER_STXX             = PlayerClient.PLAYER_STXX;
    protected final int   PLAYER_MAX_REQREP_SIZE  = 4096;
    protected final int   DIFFERENCE_SYNCH_FACTOR = 10;
	
    protected PlayerClient     pc;
    protected DataInputStream  is;
    protected DataOutputStream os;
	
    protected int   t_sec    = 0;
    protected int   t_usec   = 0;
    protected int   ts_sec   = 0;
    protected int   ts_usec  = 0;
    protected int   reserved = 0;
    protected int   size     = 0;
    protected short device   = 0x0000;
    protected short index    = -1;
	
    // synchronization stuff...
    private   int   indSynch = 0;
    private   int   tmpI     = 0;
	
    public synchronized int getTimeForDataSampled_sec  () { return t_sec;   };
    public synchronized int getTimeForDataSampled_usec () { return t_usec;  };
    public synchronized int getTimeForDataSent_sec     () { return ts_sec;  };
    public synchronized int getTimeForDataSent_usec    () { return ts_usec; };
    
    public synchronized long getTimeForDataSampled () {
        return ((long)t_sec) * 1000000 + t_usec;
    }
	
    public synchronized long getTimeForDataSent ()    {
        return ((long)ts_sec) * 1000000 + ts_usec;
    }
	
    /**
     * Abstract constructor for each PlayerDevice.
     * @param plc a reference to the PlayerClient object
     */
    public PlayerDevice (PlayerClient plc) {
        pc = plc;
        is = pc.is;
        os = pc.os;
    }
	
    /**
     * Read the t_sec, t_usec, ts_sec, ts_usec, reserved and size values from the Player 
     * message header.
     */
    protected synchronized void readHeader() {
        try {
            t_sec    = is.readInt ();
            t_usec   = is.readInt ();
            ts_sec   = is.readInt ();
            ts_usec  = is.readInt ();
            reserved = is.readInt ();
            size     = is.readInt ();
        } catch (Exception e) {
            System.err.println ("[PlayerDevice] : Error when reading header: " + e.toString ());
        }
    }
	
	
    /**
     * Sends a Player message header.
     * @param type type of message (DATA, CMD, REQ, RESP_ACK, SYNCH, RESP_NACK, RESP_ERR)
     * @param size size in bytes of the payload to follow
     */
    protected void sendHeader (short type, int size) {
        try {
            /* see player.h / player_msghdr for additional explanations */
            os.writeShort (PLAYER_STXX);	/* 0x5878 - the message start signifier */
            os.writeShort (type);   /* DATA, CMD, REQ, RESP_ACK, SYNCH, RESP_NACK, RESP_ERR */
            os.writeShort (device); /* what kind of device */
            os.writeShort (index);  /* which device of what kind */
            os.writeInt (0);        /* server's current time (seconds) */
            os.writeInt (0);        /* server's current time (microseconds) */
            os.writeInt (0);        /* time when the current data was generated (seconds) */
            os.writeInt (0);        /* time when the current data was generated (microseconds) */ 
            os.writeInt (0);        /* reserved */
            os.writeInt (size);     /* size in bytes of the payload to follow */
        } catch (Exception e) {
            System.err.println ("[PlayerDevice] : Error when reading header: " + e.toString ());
        }
    }

    /**
     * Read up to <i>size</i> bytes of data.
     */
    public synchronized void readData() {
        readHeader ();
        try {
            for (int i = 0; i < size; i++)
                is.readByte ();
        } catch (Exception e) {
            System.err.println ("[PlayerDevice] : Error when reading payload: " + e.toString ());
        }
    }
	
    /**
     * Abstract handleNARMessage method.
     */
    public void handleNARMessage () {
        System.err.println ("[PlayerDevice] : Need to handle a NAR message.");
    }
    
    /**
     * Abstract handleEARMessage method.
     */
    public void handleEARMessage () {
        System.err.println ("[PlayerDevice] : Need to handle a EAR message.");
    }
    
    /**
     * Abstract handleResponse method (threaded mode). 
     * @param size size of the payload
     */
    public void handleResponse (int size) {
        if (size == 0) {
            System.err.println ("[PlayerDevice] : Unexpected response of size 0!");
            return;
        }
        System.err.println ("[PlayerDevice] : General handle responce was triggered.");
    }
}
