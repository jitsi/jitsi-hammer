/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jitsi.hammer.neomedia.jmfext.media.protocol.ivffile;

import java.awt.*;
import java.io.*;

import com.google.common.io.LittleEndianDataInputStream;

/**
 * This class represent the 32 bytes header of an IVF file.
 * http://wiki.multimedia.cx/index.php?title=IVF
 * 
 * 
 * @author Thomas Kuntz
 */
public class IVFHeader
{
    private String signature;
    private short version;
    private short headerLengh;
    private String codec;
    private short width;
    private short height;
    private int framerate;
    private int timeScale;
    private int numberOfFramesInFile;
    
    public IVFHeader(String filePath)
    {
        try
        {
            InputStream input = new FileInputStream(filePath);
            LittleEndianDataInputStream stream =
                    new LittleEndianDataInputStream(input);
            
            signature = "" +
                    (char)stream.readByte() +
                    (char)stream.readByte() +
                    (char)stream.readByte() +
                    (char)stream.readByte();
            version = stream.readShort();
            headerLengh = stream.readShort();
            codec = "" +
                    (char)stream.readByte() +
                    (char)stream.readByte() +
                    (char)stream.readByte() +
                    (char)stream.readByte();
            width = stream.readShort();
            height = stream.readShort();
            framerate = stream.readInt();
            timeScale = stream.readInt();
            numberOfFramesInFile = stream.readInt();
            
            stream.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    
    
    public String getSignature()
    {
        return signature;
    }
    
    
    public short getVersion()
    {
        return version;
    }
    
    
    public short getHeaderLengh()
    {
        return headerLengh;
    }
    
    
    public String getCodec()
    {
        return codec;
    }
    
    
    public short getWidth()
    {
        return width;
    }
    
    
    public short getHeight()
    {
        return height;
    }
    
    public Dimension getDimension()
    {
        return new Dimension(width,height);
    }
    
    
    public int getFramerate()
    {
        return framerate;
    }
    
    
    public int getTimeScale()
    {
        return timeScale;
    }
    
    
    public int getNumberOfFramesInFile()
    {
        return numberOfFramesInFile;
    }
}