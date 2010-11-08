/*
 *  Player Java Client - MapInterface.java
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
 * $Id: MapInterface.java 19 2005-05-26 09:14:47Z veedee $
 *
 */
package javaclient;

/**
 * The map interface provides acces to an occupancy grid map. This interface returns no data 
 * and accepts no commands. The map is delivered in tiles, via a sequence of configuration 
 * requests.
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * </ul>
 */
public class MapInterface extends PlayerDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;

    /* get a map */
    private final short PLAYER_MAP_CODE    = PlayerClient.PLAYER_MAP_CODE;

    /* the player message types (see player.h) */
    private static final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;

    /** the max number of cells we can send in one tile */
    public static final short PLAYER_MAP_MAX_CELLS_PER_TILE = 
        PlayerClient.PLAYER_MAX_REQREP_SIZE - 17;
    
    /* configuration subtypes */
    protected static final short PLAYER_MAP_GET_INFO_REQ = 1;
    protected static final short PLAYER_MAP_GET_DATA_REQ = 2;

    private int scale;      /* the scale of the map (pixels per kilometer) */
    private int width;      /* the width of the map (pixels) */
    private int height;     /* the height of the map (pixels) */
   
    private int tileCol;    /* the tile origin (X - pixels) */
    private int tileRow;    /* the tile origin (Y - pixels) */
    private int tileWidth;  /* the width of a tile in the map (pixels) */
    private int tileHeight; /* the height of a tile the map (pixels) */
    
    /* cell occupancy value (empty = -1, unknown = 0, occupied = +1) */
    private int tileData[] = new int[PLAYER_MAP_MAX_CELLS_PER_TILE];
    
    /**
     * Constructor for MapInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public MapInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_MAP_CODE;
        index     = indexOfDevice;
    }

    /**
     * Configuration request: Get map information.
     * <br><br>
     * Retrieve the size and scale information of a current map. This request is used to 
     * get the size information before you request the actual map data. Set the subtype to 
     * PLAYER_MAP_GET_INFO_REQ; the server will reply with the size information filled in.
     * <br><br>
     * See the player_map_info structure from player.h
     */
    public void getMapInformation () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);     /* 1 byte payload */
            os.writeByte (PLAYER_MAP_GET_INFO_REQ);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Map] : Couldn't send PLAYER_MAP_GET_INFO_REQ " +
                    "command: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Get map data.
     * <br><br>
     * Retrieve the map data. Beacause of the limited size of a request-reply messages, the 
     * map data is tranfered in tiles. In the request packet, set the column and row index 
     * of a specific tile; the server will reply with the requested map data filled in.
     * <br><br>
     * See the player_map_data structure from player.h
     * @param col the tile origin (X - pixels)
     * @param row the tile origin (Y - pixels)
     * @param width the width of the tile in the map (pixels)
     * @param height the height of the tile in the map (pixels)
     */
    public void getMapData (int col, int row, int width, int height) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 17);     /* 17 byte payload */
            os.writeByte (PLAYER_MAP_GET_DATA_REQ);
            os.writeInt (col);        /* the tile origin (X - pixels) */
            os.writeInt (row);        /* the tile origin (Y - pixels) */
            os.writeInt (width);      /* the width of the tile in the map (pixels) */
            os.writeInt (height);     /* the height of the tile in the map (pixels) */
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Map] : Couldn't send PLAYER_MAP_GET_DATA_REQ " +
                    "command: " + e.toString ());
        }
    }
    
    /**
     * Handle acknowledgement response messages (threaded mode).
     * @param size size of the payload
     */
    public void handleResponse (int size) {
        if (size == 0) {
            if (isDebugging)
            	System.err.println ("[Map][Debug] : Unexpected response of size 0!");
            return;
        }
        try {
            /* each reply begins with a uint8_t subtype field */
            short subtype = (short)is.readUnsignedByte ();
            // should be byte subtype = is.readByte ();  
            switch (subtype) {
                case PLAYER_MAP_GET_INFO_REQ: {
                    scale = is.readInt ();      /* the scale of the map (pixels per kilometer) */
                    width = is.readInt ();      /* the width of the map (pixels) */
                    height = is.readInt ();     /* the height of the map (pixels) */
                    break;
                }
                case PLAYER_MAP_GET_DATA_REQ: {
                    tileCol    = is.readInt (); /* the tile origin (X - pixels) */
                    tileRow    = is.readInt (); /* the tile origin (Y - pixels) */
                    tileWidth  = is.readInt (); /* the width of a tile in the map (pixels) */
                    tileHeight = is.readInt (); /* the height of a tile in the map (pixels) */
                    for (int i = 0; i < PLAYER_MAP_MAX_CELLS_PER_TILE; i++) {
                        /* cell occupancy value (empty = -1, unknown = 0, occupied = +1) */
                        tileData[i] = is.readByte ();
                    }
                    break;
                }
                default:{
                    System.err.println ("[Map] : Unexpected response " + subtype + 
                            " of size = " + size);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println ("[Map] : Error when reading payload " + e.toString ());
        }
    }

    /**
     * Get the scale of the map (pixels per kilometer)
     * @return the scale of the map as an int
     */
    public synchronized int getScale () { return this.scale; }
    
    /**
     * Get the width of the map (pixels)
     * @return the width of the map as an int
     */
    public synchronized int getWidth () { return this.width; }
    
    /**
     * Get the height of the map (pixels)
     * @return the height of the map as an int
     */
    public synchronized int getHeight () { return this.height; }

    /**
     * Get the tile origin (X - pixels)
     * @return the tile origin (X - pixels) as an int
     */
    public synchronized int getTileCol () { return this.tileCol; }
    
    /**
     * Get the tile origin (Y - pixels)
     * @return the tile origin (Y - pixels) as an int
     */
    public synchronized int getTileRow () { return this.tileRow; }
    
    /**
     * Get the width of a tile in the map (pixels)
     * @return the width of a tile the map as an int
     */
    public synchronized int getTileWidth () { return this.tileWidth; }
    
    /**
     * Get the height of a tile in the map (pixels)
     * @return the height of a tile in the map as an int
     */
    public synchronized int getTileHeight () { return this.tileHeight; }
    
    /**
     * Get a tile data
     * @return the data of a specified tile as an array of integers
     * @see #getMapData(int, int, int, int)
     */
    public synchronized int[] getTileData () { return this.tileData; }
    
    /**
     * Handle negative acknowledgement response messages.
     */
    public void handleNARMessage () {
        try {
            int size = is.readInt ();    /* read the packet size */
            System.err.println ("[Map] : Handling NAR of size = " + size);
        } catch (Exception e) {
            System.err.println ("[Map] : handleResponsePosition ERROR " + e.toString ());
        }
    }

}
