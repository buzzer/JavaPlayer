/*
 *  Player Java Client 2 - HealthInterface.java
 *  Copyright (C) 2006 Radu Bogdan Rusu
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
 * $Id: HealthInterface.java 69 2006-06-30 09:10:43Z veedee $
 *
 */
package javaclient2;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javaclient2.xdr.OncRpcException;
import javaclient2.xdr.XdrBufferDecodingStream;

import javaclient2.structures.PlayerMsgHdr;

/**
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v2.0 - Player 2.0 supported
 * </ul>
 */
public class HealthInterface extends PlayerDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;
    
    // Logging support
	private Logger logger = Logger.getLogger (HealthInterface.class.getName ());

    /**
     * Constructor for HealthInterface.
     * @param pc a reference to the PlayerClient object
     */
    public HealthInterface (PlayerClient pc) { super (pc); }
    
    /**
     * Read the bumper values.
     */
    public synchronized void readData (PlayerMsgHdr header) {
    	// TO IMPLEMENT!!!
    }
}

