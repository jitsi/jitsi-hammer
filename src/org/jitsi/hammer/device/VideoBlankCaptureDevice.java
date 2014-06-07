/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jitsi.hammer.device;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.protocol.*;


import org.jitsi.impl.neomedia.codec.*;
import org.jitsi.impl.neomedia.jmfext.media.protocol.*;

/**
 * Implements a <tt>CaptureDevice</tt> which provides silence in the form of
 * audio media.
 *
 * @author Lyubomir Marinov
 */
public class VideoBlankCaptureDevice
    extends AbstractPushBufferCaptureDevice
{

    public final static float FRAMERATE = 25;
    
    /**
     * The interval of time in milliseconds between two consecutive ticks of the
     * clock used by <tt>AudioSilenceCaptureDevice</tt> and, more specifically,
     * <tt>AudioSilenceStream</tt>.
     */
    private static final long CLOCK_TICK_INTERVAL = 20;

    /**
     * The list of <tt>Format</tt>s supported by the
     * <tt>VideoBlankCaptureDevice</tt> instances.
     */
    private static final Format[] SUPPORTED_FORMATS
        = new Format[]
                {
                    new VideoFormat(
                            VideoFormat.RGB,
                            new Dimension(853,480),
                            Format.NOT_SPECIFIED,
                            Format.byteArray,
                            FRAMERATE)
                };

    /**
     * {@inheritDoc}
     *
     * Implements
     * {@link AbstractPushBufferCaptureDevice#createStream(int, FormatControl)}.
     */
    protected VideoBlankStream createStream(
            int streamIndex,
            FormatControl formatControl)
    {
        return new VideoBlankStream(this, formatControl);
    }

    /**
     * {@inheritDoc}
     *
     * Overrides the super implementation in order to return the list of
     * <tt>Format</tt>s hardcoded as supported in
     * <tt>VideoBlankCaptureDevice</tt> because the super looks them up by
     * <tt>CaptureDeviceInfo</tt> and this instance does not have one.
     */
    @Override
    protected Format[] getSupportedFormats(int streamIndex)
    {
        return SUPPORTED_FORMATS.clone();
    }

    /**
     * Implements a <tt>PushBufferStream</tt> which provides silence in the form
     * of audio media.
     */
    private static class VideoBlankStream
        extends AbstractPushBufferStream<VideoBlankCaptureDevice>
        implements Runnable
    {
        /**
         * The indicator which determines whether {@link #start()} has been
         * invoked on this instance without an intervening {@link #stop()}.
         */
        private boolean started;

        /**
         * The <tt>Thread</tt> which pushes available media data out of this
         * instance to its consumer i.e. <tt>BufferTransferHandler</tt>.
         */
        private Thread thread;

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
        public void read(Buffer buffer)
            throws IOException
        {
            VideoFormat format = (VideoFormat) getFormat();
            int frameSizeInBytes
                = (int) (
                 format.getSize().getHeight()
                * format.getSize().getWidth()
                * 3);

            byte[] data
                = AbstractCodec2.validateByteArraySize(
                        buffer,
                        frameSizeInBytes,
                        false);

            Arrays.fill(data, 0, frameSizeInBytes, (byte) 255);

            buffer.setFormat(format);
            buffer.setLength(frameSizeInBytes);
            buffer.setOffset(0);
        }

        /**
         * Runs in {@link #thread} and pushes available media data out of this
         * instance to its consumer i.e. <tt>BufferTransferHandler</tt>.
         */
        public void run()
        {
            try
            {
                /*
                 * The method implements a clock which ticks at a certain and
                 * regular interval of time which is not affected by the
                 * duration of the execution of, for example, the invocation of
                 * BufferTransferHandler.transferData(PushBufferStream).
                 *
                 * XXX The implementation utilizes System.currentTimeMillis()
                 * and, consequently, may be broken by run-time adjustments to
                 * the system time. 
                 */
                long tickTime = System.currentTimeMillis();

                while (true)
                {
                    long sleepInterval = tickTime - System.currentTimeMillis();
                    boolean tick = (sleepInterval <= 0);

                    if (tick)
                    {
                        /*
                         * The current thread has woken up just in time or too
                         * late for the next scheduled clock tick and,
                         * consequently, the clock should tick right now.
                         */
                        tickTime += CLOCK_TICK_INTERVAL;
                    }
                    else
                    {
                        /*
                         * The current thread has woken up too early for the
                         * next scheduled clock tick and, consequently, it
                         * should sleep until the time of the next scheduled
                         * clock tick comes.
                         */
                        try
                        {
                            Thread.sleep(sleepInterval);
                        }
                        catch (InterruptedException ie)
                        {
                        }
                        /*
                         * The clock will not tick and spurious wakeups will be
                         * handled. However, the current thread will first check
                         * whether it is still utilized by this
                         * VideoBlankStream in order to not delay stop
                         * requests.
                         */
                    }

                    synchronized (this)
                    {
                        /*
                         * If the current Thread is no longer utilized by this
                         * VideoBlankStream, it no longer has the right to
                         * touch it. If this VideoBlankStream has been
                         * stopped, the current Thread should stop as well. 
                         */
                        if ((thread != Thread.currentThread()) || !started)
                            break;
                    }

                    if (tick)
                    {
                        BufferTransferHandler transferHandler
                            = this.transferHandler;

                        if (transferHandler != null)
                        {
                            try
                            {
                                transferHandler.transferData(this);
                            }
                            catch (Throwable t)
                            {
                                if (t instanceof ThreadDeath)
                                    throw (ThreadDeath) t;
                                else
                                {
                                    // TODO Auto-generated method stub
                                }
                            }
                        }
                    }
                }
            }
            finally
            {
                synchronized (this)
                {
                    if (thread == Thread.currentThread())
                    {
                        thread = null;
                        started = false;
                        notifyAll();
                    }
                }
            }
        }

        /**
         * Starts the transfer of media data from this instance.
         *
         * @throws IOException if an error has prevented the start of the
         * transfer of media from this instance
         */
        @Override
        public synchronized void start()
            throws IOException
        {
            if (thread == null)
            {
                String className = getClass().getName();

                thread = new Thread(this, className);
                thread.setDaemon(true);

                boolean started = false;

                try
                {
                    thread.start();
                    started = true;
                }
                finally
                {
                    this.started = started;
                    if (!started)
                    {
                        thread = null;
                        notifyAll();

                        throw new IOException("Failed to start " + className);
                    }
                }
            }
        }

        /**
         * Stops the transfer of media data from this instance.
         *
         * @throws IOException if an error has prevented the stopping of the
         * transfer of media from this instance
         */
        @Override
        public synchronized void stop()
            throws IOException
        {
            this.started = false;
            notifyAll();

            boolean interrupted = false;

            while (thread != null)
            {
                try
                {
                    wait();
                }
                catch (InterruptedException ie)
                {
                    interrupted = true;
                }
            }
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }
}
