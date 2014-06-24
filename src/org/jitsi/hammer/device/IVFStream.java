package org.jitsi.hammer.device;

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
    extends AbstractVideoPullBufferStream<IVFCaptureDevice>
{
    /**
     * The indicator which determines whether {@link #start()} has been
     * invoked on this instance without an intervening {@link #stop()}.
     */
    private boolean started;
    
    private long seqNo = 0;

    
    private long timeLastRead = 0;
    
    IVFFileReader ivfFileReader;
    IVFHeader header;
    
    private String filePath;

    /**
     * Initializes a new <tt>ImageStream</tt> instance which is to have a
     * specific <tt>FormatControl</tt>
     *
     * @param dataSource the <tt>DataSource</tt> which is creating the new
     * instance so that it becomes one of its <tt>streams</tt>
     * @param formatControl the <tt>FormatControl</tt> of the new instance which
     * is to specify the format in which it is to provide its media data
     */
    IVFStream(IVFCaptureDevice dataSource, FormatControl formatControl)
    {
        super(dataSource, formatControl);
        filePath = dataSource.getFilePath();
        ivfFileReader = new IVFFileReader(filePath);
        header=ivfFileReader.getHeader();
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
        
        byte[] bytes = (byte[]) buffer.getData();
        
        byte[] data = ivfFileReader.getNextFrame(true);
        
        buffer.setData(data);
        buffer.setOffset(0);
        buffer.setLength(data.length);
        
        
        buffer.setTimeStamp(System.nanoTime());
        buffer.setFlags(Buffer.FLAG_SYSTEM_TIME | Buffer.FLAG_LIVE_DATA);
        //buffer.setHeader(null);
        //buffer.setSequenceNumber(seqNo);
        //seqNo++;
        
        millis = System.currentTimeMillis() - timeLastRead;
        millis = (long)(1000.0 / (header.getFramerate()/header.getTimeScale())) - millis;
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
        timeLastRead=System.currentTimeMillis();
    }

    /**
     * Starts the transfer of media data from this instance.
     * @throws IOException 
     */
    @Override
    public synchronized void start() 
            throws IOException
    {
        started = true;
        super.start();
    }

    /**
     * Stops the transfer of media data from this instance.
     * @throws IOException 
     */
    @Override
    public synchronized void stop()
            throws IOException
    {
        started = false;
        super.stop();
    }
}