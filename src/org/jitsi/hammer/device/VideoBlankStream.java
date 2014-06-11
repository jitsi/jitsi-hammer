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
 * Implements a <tt>PullBufferStream</tt> which provides blank image in the form
 * of video media.
 */
public class VideoBlankStream
	extends AbstractVideoPullBufferStream<VideoBlankCaptureDevice>
{
	/**
	 * The indicator which determines whether {@link #start()} has been
	 * invoked on this instance without an intervening {@link #stop()}.
	 */
	private boolean started;
	
	private long seqNo;
	
	private int color = 0;
	private boolean increment = true;

	/**
	 * Initializes a new <tt>VideoBlankStream</tt> which is to be exposed
	 * by a specific <tt>VideoBlankCaptureDevice</tt> and which is to have
	 * its <tt>Format</tt>-related information abstracted by a specific
	 * <tt>FormatControl</tt>.
	 *
	 * @param dataSource the <tt>VideoBlankCaptureDevice</tt> which is
	 * initializing the new instance and which is to expose it in its array
	 * of <tt>PushBufferStream</tt>s
	 * @param formatControl the <tt>FormatControl</tt> which is to abstract
	 * the <tt>Format</tt>-related information of the new instance
	 */
	public VideoBlankStream(
			VideoBlankCaptureDevice dataSource,
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
		/*VideoFormat format = (VideoFormat) getFormat();
		long frameSizeInBytes
			= FFmpeg.FF_INPUT_BUFFER_PADDING_SIZE + (int) (
			 format.getSize().getHeight()
			* format.getSize().getWidth()
			* 4);

		byte[] data
			= AbstractCodec2.validateByteArraySize(
					buffer,
					frameSizeInBytes,
					false);

		Arrays.fill(data, 0, frameSizeInBytes, (byte) 0);

		buffer.setFormat(format);
		buffer.setLength(frameSizeInBytes);
		buffer.setOffset(0);
		*/
	    
		Format format = buffer.getFormat();

        if (format == null)
        {
            format = getFormat();
            if (format != null)
                buffer.setFormat(format);
        }
        /*
        if(format instanceof AVFrameFormat)
        {
            Object o = buffer.getData();
            AVFrame frame;

            if (o instanceof AVFrame)
                frame = (AVFrame) o;
            else
            {
                frame = new AVFrame();
                buffer.setData(frame);
            }

            AVFrameFormat avFrameFormat = (AVFrameFormat) format;
            Dimension size = avFrameFormat.getSize();
            
            int frameSizeInBytes
			= FFmpeg.FF_INPUT_BUFFER_PADDING_SIZE + (int) (
			  size.getHeight()
			* size.getWidth()
			* 4);
            
            ByteBuffer datas = new ByteBuffer(frameSizeInBytes);
            if (frame.avpicture_fill(datas, avFrameFormat) < 0)
            {
                datas.free();
                throw new IOException("avpicture_fill");
            }
        }
        else*/
        {
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
            
            if(increment) color+=2;
            else color-=2;
            if(color >= 254) increment = false;
            else if(color <= 0) increment = true;

            buffer.setData(data);
            buffer.setOffset(0);
            buffer.setLength(bytes.length);
        }
		
		
		buffer.setHeader(null);
        buffer.setTimeStamp(System.nanoTime());
        buffer.setSequenceNumber(seqNo);
        buffer.setFlags(Buffer.FLAG_SYSTEM_TIME | Buffer.FLAG_LIVE_DATA);
        seqNo++;
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