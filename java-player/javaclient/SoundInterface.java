/*
 *  Player Java Client - SoundInterface.java
 *  Copyright (C) 2003-2005 Josh Bers & Radu Bogdan Rusu
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
 * $Id: SoundInterface.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient;

/**
 * The sound interface allows playback of a pre-recorded sound (e.g., on an Amigobot).
 * This interface provides no data.
 * @author Josh Bers & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class SoundInterface extends PlayerDevice {

    /* sound file playback */
    private final short PLAYER_SOUND_CODE = PlayerClient.PLAYER_SOUND_CODE; 
    
    /* the player message types (see player.h) */
    private final short PLAYER_MSGTYPE_CMD = PlayerClient.PLAYER_MSGTYPE_CMD;
    
    /**
     * Constructor for SoundInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public SoundInterface (PlayerClient pc, short indexOfDevice) {
        super (pc);
        device = PLAYER_SOUND_CODE;
        index  = indexOfDevice;
    }
    
    /**
     * Send a play command to the Sound device. 
     * @param index index of sound to be played
     */
    public void play (short index) {
    	try {
            sendHeader (PLAYER_MSGTYPE_CMD, 2);       /* 2 bytes payload */
            os.writeShort (index);                    /* index of sound to be played */
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Sound] : Couldn't send play command request: " + 
                    e.toString ());
        }
    }
}
