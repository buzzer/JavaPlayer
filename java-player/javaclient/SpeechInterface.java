/*
 *  Player Java Client - SpeechInterface.java
 *  Copyright (C) 2002-2005 Maxim Batalin & Radu Bogdan Rusu
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
 * $Id: SpeechInterface.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient;

/**
 * The speech interface provides access to a speech synthesis system.
 * The speech interface returns no data. 
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class SpeechInterface extends PlayerDevice {

    private final short PLAYER_SPEECH_CODE = PlayerClient.PLAYER_SPEECH_CODE; /* speech I/O */ 
    
    /** maximum string length */
    public static final short PLAYER_SPEECH_MAX_STRING_LEN = 256;
    
    /* the player message types (see player.h) */
    private final short PLAYER_MSGTYPE_CMD = PlayerClient.PLAYER_MSGTYPE_CMD;
    
    /**
     * Constructor for SpeechInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public SpeechInterface (PlayerClient pc, short indexOfDevice) {
        super (pc);
        device = PLAYER_SPEECH_CODE;
        index  = indexOfDevice;
    }
 
    /**
     * The speech interface accepts a command that is a string to be given to the speech 
     * synthesizer. The first PLAYER_SPEECH_MAX_STRING_LEN characters will be said. 
     * @param text the string to say 
     */
    public void speech (String text) {
    	String temp = text;
        if (text.length () > PLAYER_SPEECH_MAX_STRING_LEN)
            temp = text.substring (0, PLAYER_SPEECH_MAX_STRING_LEN);
        try {
            sendHeader (PLAYER_MSGTYPE_CMD, temp.length ());
            os.writeBytes (temp);                           /* the string to say */
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Speech] : Couldn't send speech command request: " + 
                    e.toString ());
        }
    }
}
