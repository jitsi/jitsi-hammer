package org.jitsi.hammer.device;

import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;

import org.jitsi.impl.neomedia.codec.*;
import org.jitsi.impl.neomedia.codec.video.AVFrame;
import org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
import org.jitsi.impl.neomedia.codec.video.ByteBuffer;
import org.jitsi.impl.neomedia.jmfext.media.protocol.*;

import java.awt.Dimension;
import java.io.*;
import java.util.*;

/**
 * Implements a <tt>PullBufferStream</tt> which provides a fading animation from
 * white to black to white... in form of video.
 */
public class VideoGreyFadingStream
	extends AbstractVideoPullBufferStream<VideoGreyFadingCaptureDevice>
{
	/**
	 * The indicator which determines whether {@link #start()} has been
	 * invoked on this instance without an intervening {@link #stop()}.
	 */
	private boolean started;
	
	private long seqNo = 0;
	
	private int color = 0;
	private boolean increment = true;
	
	private long timeLastRead = 0;

	/**
	 * Initializes a new <tt>VideoGreyFadingStream</tt> which is to be exposed
	 * by a specific <tt>VideoGreyFadingCaptureDevice</tt> and which is to have
	 * its <tt>Format</tt>-related information abstracted by a specific
	 * <tt>FormatControl</tt>.
	 *
	 * @param dataSource the <tt>VideoGreyFadingCaptureDevice</tt> which is
	 * initializing the new instance and which is to expose it in its array
	 * of <tt>PushBufferStream</tt>s
	 * @param formatControl the <tt>FormatControl</tt> which is to abstract
	 * the <tt>Format</tt>-related information of the new instance
	 */
	public VideoGreyFadingStream(
			VideoGreyFadingCaptureDevice dataSource,
			FormatControl formatControl)
	{
		super(dataSource, formatControl);
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
        Dimension size = ((VideoFormat) format).getSize();
        
        int frameSizeInBytes
		= (int) (
		  size.getHeight()
		* size.getWidth()
		* 4);
        
        byte[] data
		= AbstractCodec2.validateByteArraySize(
				buffer,
				frameSizeInBytes,
				false);

        Arrays.fill(data, 0, frameSizeInBytes, (byte) color);
        
        if(increment) color+=3;
        else color-=3;
        if(color >= 255)
        {
        	increment = false;
        	color=255;
        }
        else if(color <= 0) 
        {
        	increment = true;
        	color=0;
        }

        buffer.setData(data);
        buffer.setOffset(0);
        buffer.setLength(bytes.length);
		
		
		buffer.setTimeStamp(System.nanoTime());
		buffer.setFlags(Buffer.FLAG_SYSTEM_TIME | Buffer.FLAG_LIVE_DATA);
		//buffer.setHeader(null);
        //buffer.setSequenceNumber(seqNo);
        //seqNo++;
        
        
        //To respect the framerate, we wait for the remaing milliseconds since
		//last doRead call
		
        millis = System.currentTimeMillis() - timeLastRead;
        millis = (long)(1000.0 / VideoGreyFadingMediaDevice.FRAMERATE) - millis;
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