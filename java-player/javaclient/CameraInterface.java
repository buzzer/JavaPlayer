/*
 *  Player Java Client - CameraInterface.java
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
 * $Id: CameraInterface.java 32 2006-02-07 15:50:56Z veedee $
 *
 */
package javaclient;

/**
 * The camera interface is used to see what the camera sees. It is intended 
 * primarily for server-side (i.e., driver-to-driver) data transfers, rather 
 * than server-to-client transfers. Image data can be in may formats (see below), 
 * but is always packed (i.e., pixel rows are byte-aligned).<br /><br />
 * This interface has no commands or configuration requests.
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.5 - bug fixes (image data was corrupted previously)
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * </ul>
 */
public class CameraInterface extends PlayerDevice {
    
    private static final boolean isDebugging = PlayerClient.isDebugging;
    
    /* camera device (gazebo) */
    private final short PLAYER_CAMERA_CODE = PlayerClient.PLAYER_CAMERA_CODE;

    /** Image dimensions */ 
    public static final short PLAYER_CAMERA_IMAGE_WIDTH  = 640;
    public static final short PLAYER_CAMERA_IMAGE_HEIGHT = 480;
    public static final int   PLAYER_CAMERA_IMAGE_SIZE   = (640 * 480 * 4);
    
    /** Image format : 8-bit monochrome */
    public static final short PLAYER_CAMERA_FORMAT_MONO8  = 1;
    /** Image format : 16-bit monochrome (network byte order) */
    public static final short PLAYER_CAMERA_FORMAT_MONO16 = 2;
    /** Image format : 16-bit color (5 bits R, 6 bits G, 5 bits B) */
    public static final short PLAYER_CAMERA_FORMAT_RGB565 = 4;
    /** Image format : 24-bit color (8 bits R, 8 bits G, 8 bits B) */
    public static final short PLAYER_CAMERA_FORMAT_RGB888 = 5;
    
    public static final short PLAYER_CAMERA_COMPRESS_RAW  = 0;
    public static final short PLAYER_CAMERA_COMPRESS_JPEG = 1;

    private short width, height;        /* Image dimensions (pixels) */
    private byte bpp;                   /* Image bits-per-pixel (8, 16, 24, 32) */
    private byte format;                /* Image format (must be compatible with depth) */
    private short fdiv;                 /* Some images (such as disparity maps) use scaled 
                                           pixel values; for these images, fdiv specifies 
                                           the scale divisor (i.e., divide the integer pixel 
                                           value by fdiv to recover the real pixel value). */
    private byte compression;           /* Image compression; PLAYER_CAMERA_COMPRESS_RAW 
                                           indicates no compression. */
    private int image_size;             /* Size of image data as stored in image buffer (bytes) */
    /* Compressed image data (byte-aligned, row major order). Multi-byte image formats 
     *(such as MONO16) must be converted to network byte ordering.
     */
    private byte[] image = new byte[PLAYER_CAMERA_IMAGE_SIZE];
    
    /**
     * Constructor for CameraInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public CameraInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_CAMERA_CODE;
        index     = indexOfDevice;
    }

    /**
     * Read the camera data.<br /><br />
     * See the player_camera_data structure from player.h
     */
    public synchronized void readData () {
        readHeader ();
        try {
            width       = (short)is.readUnsignedShort ();   /* image dimensions (pixels) */
            height      = (short)is.readUnsignedShort ();
            bpp         = (byte)is.readUnsignedByte   ();   /* bits-per-pixel */
            format      = (byte)is.readUnsignedByte   ();   /* image format */
            fdiv        = (short)is.readUnsignedShort ();   /* scale divisor */
            compression = (byte)is.readUnsignedByte   ();   /* image compression status */
            image_size  = is.readInt ();                    /* size of image data */
        	
        	int totalBytes = 13;
        	int bytes;
        	while (totalBytes < this.size)
        	{
        		// read the compressed image data
        		bytes = is.read (image, totalBytes - 13, this.size - totalBytes);
        		totalBytes += bytes;
        	}
        } catch (Exception e) {
            System.err.println ("[Camera] : Error when reading payload: " + e.toString ());
        }
    }

    /**
     * Returns the image width dimension.
     * @return the image width dimension as a short
     */
    public synchronized short getWidth () { return width; }

    /**
     * Returns the image height dimension.
     * @return the image height dimension as a short
     */
    public synchronized short getHeight () { return height; }

    /**
     * Returns the image bits-per-pixel (8, 16, 24, 32) value.
     * @return the image bpp as a byte
     */
    public synchronized byte getBPP () { return bpp; }

    /**
     * Returns the image format (must be compatible with depth).
     * @return the image format as a byte
     */
    public synchronized byte getFormat () { return format; }

    /**
     * Some images (such as disparity maps) use scaled pixel values; for these 
     * images, fdiv specifies the scale divisor (i.e., divide the integer pixel
     * value by fdiv to recover the real pixel value).
     * @return the scale divisor as a short
     */
    public synchronized short getFDiv () { return fdiv; }

    /**
     * Returns the image compression status (PLAYER_CAMERA_COMPRESS_RAW indicates 
     * no compression.).
     * @return the image compression status as a byte
     */
    public synchronized byte getCompression () { return compression; }

    /**
     * Returns the size of image data as stored in image buffer (bytes). 
     * @return the image size as an integer
     */
    public synchronized int getImageSize () { return image_size; }

    /**
     * Returns the compressed image data (byte-aligned, row major order). 
     * Multi-byte image formats (such as MONO16) must be converted to 
     * network byte ordering. 
     * @return the image data as an array of bytes
     */
    public synchronized byte[] getImage () { return image; }
    
    /**
     * Handle acknowledgement response messages (threaded mode).
     * @param size size of the payload
     */
    public void handleResponse (int size) {
        if (size == 0) {
            if (isDebugging)
                System.err.println ("[Camera][Debug] : Unexpected response of size 0!");
            return;
        }
        try {
            /* each reply begins with a uint8_t subtype field */
            byte subtype = is.readByte ();
            switch (subtype) {
                default:{
                    System.err.println ("[Camera] : Unexpected response " + subtype + 
                            " of size = " + size);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println ("[Camera] : Error when reading payload " + e.toString ());
        }
    }
}
