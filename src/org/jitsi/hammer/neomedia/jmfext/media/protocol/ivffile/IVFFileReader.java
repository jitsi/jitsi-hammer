/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jitsi.hammer.neomedia.jmfext.media.protocol.ivffile;

import java.awt.*;
import java.io.*;

import org.jitsi.impl.neomedia.codec.AbstractCodec2;

import com.google.common.io.LittleEndianDataInputStream;

/**
 * This class represent an IVF file and provide an API to get the vp8 it contains
 * 
 * 
 * @author Thomas Kuntz
 */
public class IVFFileReader
{
    IVFHeader header;
    
    private int frameNo = 0;
    
    private RandomAccessFile stream;
    
    
    public IVFFileReader(String filePath)
    {
        header = new IVFHeader(filePath);
        
        try
        {
            stream = new RandomAccessFile(filePath,"r");
            stream.seek(32);
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
    
    public IVFHeader getHeader()
    {
        return header;
    }

    
    public byte[] getNextFrame(boolean loopFile) throws IOException
    {
        if((loopFile == true) && (frameNo >= header.getNumberOfFramesInFile()))
        {
            stream.seek(header.getHeaderLengh());
            frameNo = 0;
        }
        
        byte[] data;
        int frameSizeInBytes;
        
        
        frameSizeInBytes = changeEndianness(stream.readInt());
        stream.skipBytes(8);//skip the timespamp (or should I? XXX)
        data = new byte[frameSizeInBytes];
        stream.read(data);
        frameNo++;
        
        return data;
    }
    
    
    private static int changeEndianness(int value)
    {
        return 
        (((value << 24) & 0xFF000000) |
        ((value << 8) & 0x00FF0000) |
        ((value >> 8) & 0x0000FF00) |
        ((value >> 24) & 0x000000FF));
    }
}