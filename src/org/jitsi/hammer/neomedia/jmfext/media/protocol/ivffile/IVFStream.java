/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer.neomedia.jmfext.media.protocol.ivffile;

import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;

import org.jitsi.impl.neomedia.codec.*;
import org.jitsi.impl.neomedia.codec.video.ByteBuffer;
import org.jitsi.impl.neomedia.jmfext.media.protocol.*;

import com.google.common.io.LittleEndianDataInputStream;

import java.awt.Dimension;
import java.io.*;
import java.util.*;

/**
 * Implements a <tt>PullBufferStream</tt> which read an IVF file for the video
 * stream
 */
public class IVFStream
    extends AbstractVideoPullBufferStream<DataSource>
{
    private long seqNo = 0;
    
    private long timeLastRead = 0;
    
    private IVFFileReader ivfFileReader;
    
    private float FRAMERATE = 1;
    /**
     * Initializes a new <tt>ImageStream</tt> instance which is to have a
     * specific <tt>FormatControl</tt>
     *
     * @param dataSource the <tt>DataSource</tt> which is creating the new
     * instance so that it becomes one of its <tt>streams</tt>
     * @param formatControl the <tt>FormatControl</tt> of the new instance which
     * is to specify the format in which it is to provide its media data
     */
    IVFStream(DataSource dataSource, FormatControl formatControl)
    {
        super(dataSource, formatControl);
        this.ivfFileReader = new IVFFileReader(
                dataSource.getCaptureDeviceInfo().getLocator().getRemainder());
        
        this.FRAMERATE = ((VideoFormat)getFormat()).getFrameRate();
    }

    
    /**
     * Reads available media data from this instance into a specific
     * <tt>Buffer</tt>.
     *
     * @param buffer the <tt>Buffer</tt> to write the available media data
     * into
     * @throws IOException if an I/O error has prevented the reading of
     * available media data from this instance into the specified
     * <tt>buffer</tt>
     */
    @Override
    protected void doRead(Buffer buffer)
        throws IOException
    {
        long millis = 0;
        VideoFormat format;
        
        format = (VideoFormat)buffer.getFormat();
        if (format == null)
        {
            format = (VideoFormat)getFormat();
            if (format != null)
                buffer.setFormat(format);
        }
                
        byte[] data = ivfFileReader.getNextFrame(true);
        
        buffer.setData(data);
        buffer.setOffset(0);
        buffer.setLength(data.length);
        
        
        buffer.setTimeStamp(System.nanoTime());
        buffer.setFlags(Buffer.FLAG_SYSTEM_TIME | Buffer.FLAG_LIVE_DATA);
        //buffer.setHeader(null);
        //buffer.setSequenceNumber(seqNo);
        //seqNo++;
        
        millis = System.currentTimeMillis() - this.timeLastRead;
        millis = (long)(1000.0 / this.FRAMERATE) - millis;
        if(millis > 0)
        {
            try
            {
                Thread.sleep(millis);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        this.timeLastRead=System.currentTimeMillis();
    }
}