/*
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.hammer;

import io.pkts.*;
import io.pkts.packet.*;
import io.pkts.protocol.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;
import org.jitsi.hammer.extension.*;
import org.jitsi.hammer.utils.*;
import org.jitsi.impl.neomedia.*;
import org.jitsi.impl.neomedia.codec.video.vp8.*;
import org.jitsi.impl.neomedia.format.*;
import org.jitsi.impl.neomedia.jmfext.media.protocol.rtpdumpfile.*;
import org.jitsi.impl.neomedia.rtcp.*;
import org.jitsi.impl.neomedia.rtp.*;
import org.jitsi.service.libjitsi.*;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.device.*;
import org.jitsi.service.neomedia.format.*;
import org.jitsi.service.neomedia.rtp.*;
import org.jitsi.util.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * The <tt>FakeStream</tt> represents a media stream from a <tt>FakeUser</tt>.
 * It wraps a <tt>MediaStream</tt> and it can be used to stream from a
 * <tt>MediaDevice</tt> or from a raw PCAP file without setting up a
 * <tt>MediaDevice.</tt>
 *
 * @author George Politis
 */
public class FakeStream
{
    /**
     * The <tt>Logger</tt> used by {@link FakeStream} and its instances
     * for logging output.
     */
    private static final Logger logger = Logger.getLogger(FakeStream.class);

    /**
     * The <tt>Random</tt> that generates initial sequence numbers. Instances of
     * {@code java.util.Random} are thread-safe since Java 1.7.
     */
    private static final Random RANDOM = new Random();

    /**
     * Holds a value that represents an invalid index.
     */
    private static final long UNINITIALIZED_IDX = -1L;

    /**
     * Holds a value that represents an invalid SSRC.
     */
    private static final long INVALID_SSRC = -1L;

    /**
     * The RED payload type. This is required for the packet handler to be
     * able to detect keyframes.
     *
     * FIXME This information should be included in the .txt that describes
     * the PCAP file.
     */
    private static final byte RED_PT = 116;

    /**
     * The VP8 payload type. This is required for the packet handler to be
     * able to detect keyframes.
     *
     * FIXME This information should be included in the .txt that describes
     * the PCAP file.
     */
    private static final byte VP8_PT = 100;

    /**
     * With simulcast we have mini bursts of key frames (because they're
     * always being emitted in groups, for all streams). We want to restart
     * at the beginning of a burst, not the end.
     */
    private static final long KEYFRAME_MIN_DISTANCE = 20L;

    /**
     * Determines how big the queue size should be for this
     * <tt>PacketEmitter</tt>.
     */
    private static final int QUEUE_CAPACITY = 100;

    /**
     * Holds a value that represents an invalid timestamp.
     */
    private static final long INVALID_TIMESTAMP = -1;

    /**
     * The <tt>MediaStream</tt> that this instance wraps.
     */
    private final MediaStream stream;

    /**
     * The <tt>PcapChooser</tt> that is to be used to get the <tt>Pcap</tt>
     * file.
     */
    private final PcapChooser pcapChooser;

    /**
     * Maps the SSRCs from the PCAP file to the SSRCs used by this instance
     * (they are randomly generated per-instance to allow multiple instances to
     * stream the same file).
     */
    private final Map<Long, Long> ssrcsMap;

    /**
     * The indicator which determines whether {@link #close()} has been invoked
     * on this <tt>FakeStream</tt>.
     */
    private boolean closed = false;

    /**
     * The indicator which determines whether {@link #start()} has been invoked
     * on this <tt>FakeStream</tt>.
     */
    private boolean started = false;

    /**
     * The <tt>Thread</tt> the loops over the PCAP file.
     */
    private Thread loopThread;

    /**
     * The <tt>MediaFormat</tt> of the wrapped <tt>MediaStream</tt>. We store it
     * here because if we're streaming a PCAP there's no
     * <tt>MediaDeviceSession</tt> and stream.getFormat() returns null.
     */
    private MediaFormat format;

    /**
     * Maps SSRCs to <tt>PacketEmitter</tt>s.
     */
    private final Map<Long, PacketEmitter> emitterMap;

    /**
     * The <tt>PacketHandler</tt> instance that loops through the video PCAP.
     *
     * FIXME this should be generic for both audio and video.
     */
    private final VideoPacketHandler packetHandler;

    private RTCPListener RTCPListener = new RTCPListenerAdapter()
    {
        @Override
        public void firReceived(FIRPacket fir)
        {
            System.out.println("FIR Received.");
            packetHandler.stop();
        }

        @Override
        public void pliReceived(PLIPacket pli)
        {
            System.out.println("PLI Received.");

            packetHandler.stop();

        }

        @Override
        public void nackReceived(NACKPacket nack)
        {
            System.out.println("NACK Received.");

            packetHandler.stop();
        }
    };

    /**
     * Ctor.
     *
     * @param pcapChooser
     * @param stream
     */
    public FakeStream(PcapChooser pcapChooser, MediaStream stream)
    {
        this.stream = stream;
        if (!isAudio())
        {
            long[] ssrcs = pcapChooser.getVideoSsrcs();
            if (ssrcs == null || ssrcs.length == 0)
            {
                this.pcapChooser = null;
                this.ssrcsMap = null;
                this.packetHandler = null;
                this.emitterMap = null;
            }
            else
            {
                this.stream.getMediaStreamStats().addNackListener(RTCPListener);
                this.pcapChooser = pcapChooser;
                this.packetHandler = new VideoPacketHandler();
                this.emitterMap = new HashMap<>();

                this.ssrcsMap = new ConcurrentHashMap<>(ssrcs.length);
                for (long ssrc : ssrcs)
                {
                    this.ssrcsMap.put(ssrc,
                        Math.abs(
                            new Random().nextInt()) & 0xFFFFFFFFL);
                }
            }
        }
        else
        {
            this.pcapChooser = null;
            this.ssrcsMap = null;
            this.packetHandler = null;
            this.emitterMap = null;
        }
    }

    /**
     *
     * @return true if the wrapped <tt>MediaStream</tt> is an instance of
     * <tt>AudioMediaStream</tt>, false otherwise.
     */
    public boolean isAudio()
    {
        return stream instanceof AudioMediaStream;
    }

    /**
     * Returns the synchronization source (SSRC) identifier of the local
     * participant or <tt>-1</tt> if that identifier is not yet known at this
     * point.
     *
     * @return the synchronization source (SSRC) identifier of the local
     * participant or <tt>-1</tt> if that identifier is not yet known at this
     * point.
     */
    public long getLocalSourceID()
    {
        return stream.getLocalSourceID();
    }

    /**
     * Returns a <tt>MediaStreamStats</tt> object used to get statistics about
     * this <tt>MediaStream</tt>.
     *
     * @return the <tt>MediaStreamStats</tt> object used to get statistics about
     * this <tt>MediaStream</tt>.
     */
    public MediaStreamStats getMediaStreamStats()
    {
        return stream.getMediaStreamStats();
    }

    /**
     * Releases the resources allocated by this instance in the course of its
     * execution and prepares it to be garbage collected.
     */
    public void close()
    {
        synchronized (this)
        {
            if (closed)
            {
                return;
            }

            closed = true;
        }

        if (loopThread != null)
        {
            loopThread.interrupt();
            try
            {
                loopThread.join();
            }
            catch (InterruptedException e)
            {
            }
        }
        stream.close();
    }

    /**
     * Sets the device that this stream should use to play back and capture
     * media.
     *
     * @param device the <tt>MediaDevice</tt> that this stream should use to
     * play back and capture media.
     */
    public void setDevice(MediaDevice device)
    {
        stream.setDevice(device);
    }

    /**
     * Sets the <tt>MediaFormat</tt> that this <tt>MediaStream</tt> should
     * transmit in.
     *
     * @param format the <tt>MediaFormat</tt> that this <tt>MediaStream</tt>
     * should transmit in.
     */
    public void setFormat(MediaFormat format)
    {
        this.format = format;
        stream.setFormat(format);
    }

    /**
     * Sets the name of this stream. Stream names are used by some protocols,
     * for diagnostic purposes mostly. In XMPP for example this is the name of
     * the content element that describes a stream.
     *
     * @param name the name of this stream or <tt>null</tt> if no name has been
     * set.
     */
    public void setName(String name)
    {
        stream.setName(name);
    }

    /**
     * Sets the <tt>RTPTranslator</tt> which is to forward RTP and RTCP traffic
     * between this and other <tt>MediaStream</tt>s.
     *
     * @param rtpTranslator the <tt>RTPTranslator</tt> which is to forward RTP
     * and RTCP traffic between this and other <tt>MediaStream</tt>s
     */
    public void setRTPTranslator(RTPTranslator rtpTranslator)
    {
        stream.setRTPTranslator(rtpTranslator);
    }

    /**
     * Sets the direction in which media in this <tt>MediaStream</tt> is to be
     * streamed. If this <tt>MediaStream</tt> is not currently started, calls to
     * {@link #start()} later on will start it only in the specified
     * <tt>direction</tt>. If it is currently started in a direction different
     * than the specified, directions other than the specified will be stopped.
     *
     * @param direction the <tt>MediaDirection</tt> in which this
     * <tt>MediaStream</tt> is to stream media when it is started
     */
    public void setDirection(MediaDirection direction)
    {
        stream.setDirection(direction);
    }

    /**
     * Adds a new association in this <tt>MediaStream</tt> of the specified RTP
     * payload type with the specified <tt>MediaFormat</tt> in order to allow it
     * to report <tt>rtpPayloadType</tt> in RTP flows sending and receiving
     * media in <tt>format</tt>. Usually, <tt>rtpPayloadType</tt> will be in the
     * range of dynamic RTP payload types.
     *
     * @param payloadType the RTP payload type to be associated in this
     * <tt>MediaStream</tt> with the specified <tt>MediaFormat</tt>
     * @param format the <tt>MediaFormat</tt> to be associated in this
     * <tt>MediaStream</tt> with <tt>rtpPayloadType</tt>
     */
    public void addDynamicRTPPayloadType(Byte payloadType, MediaFormat format)
    {
        stream.addDynamicRTPPayloadType(payloadType, format);
    }

    /**
     * Adds or updates an association in this <tt>MediaStream</tt> mapping the
     * specified <tt>extensionID</tt> to <tt>rtpExtension</tt> and enabling or
     * disabling its use according to the direction attribute of
     * <tt>rtpExtension</tt>.
     *
     * @param extensionID the ID that is mapped to <tt>rtpExtension</tt> for
     * the lifetime of this <tt>MediaStream</tt>.
     * @param rtpExtension the <tt>RTPExtension</tt> that we are mapping to
     * <tt>extensionID</tt>.
     */
    public void addRTPExtension(byte extensionID, RTPExtension rtpExtension)
    {
        stream.addRTPExtension(extensionID, rtpExtension);
    }

    /**
     * Adds the SSRCs of the wrapped <tt>MediaStream</tt> to the content passed
     * in as a parameter.
     *
     * @param content the <tt>ContentPacketExtension</tt> to add the SSRCs to.
     */
    public void updateContentPacketExtension(ContentPacketExtension content)
    {
        RtpDescriptionPacketExtension
                description = content.getFirstChildOfType(
                RtpDescriptionPacketExtension.class);

        Set<Long> ssrcs;
        if (ssrcsMap == null || (ssrcs = ssrcsMap.keySet()).isEmpty())
        {
            long ssrc = stream.getLocalSourceID();

            description.setSsrc(String.valueOf(ssrc));
            MediaService mediaService = LibJitsi.getMediaService();
            String msLabel = UUID.randomUUID().toString();
            String label = UUID.randomUUID().toString();

            SourcePacketExtension sourcePacketExtension =
                    new SourcePacketExtension();
            SsrcPacketExtension ssrcPacketExtension =
                    new SsrcPacketExtension();


            sourcePacketExtension.setSSRC(ssrc);
            sourcePacketExtension.addChildExtension(
                    new ParameterPacketExtension("cname",
                            mediaService.getRtpCname()));
            sourcePacketExtension.addChildExtension(
                    new ParameterPacketExtension("msid", msLabel + " " + label));
            sourcePacketExtension.addChildExtension(
                    new ParameterPacketExtension("mslabel", msLabel));
            sourcePacketExtension.addChildExtension(
                    new ParameterPacketExtension("label", label));
            description.addChildExtension(sourcePacketExtension);


            ssrcPacketExtension.setSsrc(String.valueOf(ssrc));
            ssrcPacketExtension.setCname(mediaService.getRtpCname());
            ssrcPacketExtension.setMsid(msLabel + " " + label);
            ssrcPacketExtension.setMslabel(msLabel);
            ssrcPacketExtension.setLabel(label);
            description.addChildExtension(ssrcPacketExtension);
        }
        else
        {

            MediaService mediaService = LibJitsi.getMediaService();
            String msLabel = UUID.randomUUID().toString();
            String label = UUID.randomUUID().toString();

            List<SourcePacketExtension> sources = new ArrayList<>();
            for (long ssrc : ssrcs)
            {
                SourcePacketExtension sourcePacketExtension
                    = new SourcePacketExtension();

                sourcePacketExtension.setSSRC(ssrcsMap.get(ssrc));
                sourcePacketExtension.addChildExtension(
                        new ParameterPacketExtension("cname",
                                mediaService.getRtpCname()));
                sourcePacketExtension.addChildExtension(
                        new ParameterPacketExtension(
                                "msid", msLabel + " " + label));
                sourcePacketExtension.addChildExtension(
                        new ParameterPacketExtension("mslabel", msLabel));
                sourcePacketExtension.addChildExtension(
                        new ParameterPacketExtension("label", label));
                sources.add(sourcePacketExtension);
                description.addChildExtension(sourcePacketExtension);
            }

            if (ssrcs.size() > 1)
            {
                SourceGroupPacketExtension sourceGroupPacketExtension =
                        SourceGroupPacketExtension.createSimulcastGroup();
                sourceGroupPacketExtension.addSources(sources);
                description.addChildExtension(sourceGroupPacketExtension);
            }
        }
    }

    /**
     * The <tt>ZrtpControl</tt> which controls the ZRTP for this stream.
     *
     * @return the <tt>ZrtpControl</tt> which controls the ZRTP for this stream
     */
    public SrtpControl getSrtpControl()
    {
        return stream.getSrtpControl();
    }

    /**
     * Holds the value to be used to initialize the rewrite timestamp bases.
     * We're starting with a value of 30000 to make pcap debugging easier and
     * then it holds the last timestamp that has been emitted by any of the
     * <tt>PacketEmitter</tt>s
     */
    private long rewriteTimestampBaseInit = 30000;

    /**
     * The <tt>Thread</tt> responsible for emitting RTP packets for a specific
     * SSRC.
     */
    class PacketEmitter extends Thread
    {
        /**
         * The <tt>RawPacketScheduler</tt> that schedules RTP packets for
         * emission.
         */
        private final RawPacketScheduler rawPacketScheduler;

        /**
         * The sequence number for the next packet to be emitted by this SSRC.
         */
        private int rewriteSeqNum = RANDOM.nextInt(0x10000);

        /**
         * The RTP timestamp base for the SSRC we rewrite into.
         */
        private long rewriteTimestampBase = INVALID_TIMESTAMP;

        /**
         * The RTP timestamp base for this SSRC.
         */
        private long timestampBase = INVALID_TIMESTAMP;

        /**
         *
         */
        private long lastTimestampDiff = 0L;

        /**
         * The queue of RTP packets waiting to be emitted.
         */
        final BlockingQueue<RawPacket> queue;

        /**
         * Ctor.
         */
        public PacketEmitter(long ssrc)
        {
            super("PacketEmitter-" + Long.toString(ssrc));
            queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
            rawPacketScheduler = new RawPacketScheduler(
                    (long) VideoMediaFormatImpl.DEFAULT_CLOCK_RATE);
        }

        public void restart()
        {
            logger.info("Restarting this packet emitter.");
            queue.clear();
            rewriteTimestampBase += lastTimestampDiff;
        }

        /**
         * Rewrites the SSRC, sequence number and timestamps.
         *
         * @param rtpPacket the <tt>RawPacket</tt> to rewrite.
         * @return the rewritten <tt>RawPacket</tt> or null.
         */
        private RawPacket rewriteRTP(RawPacket rtpPacket)
        {
            final long rtpPacketTimestamp = rtpPacket.getTimestamp();
            if (timestampBase == INVALID_TIMESTAMP)
            {
                timestampBase = rtpPacketTimestamp;
            }

            long ssrc = ssrcsMap.get(rtpPacket.getSSRCAsLong());

            int sn = rewriteSeqNum++;

            lastTimestampDiff
                = TimeUtils.rtpDiff(rtpPacketTimestamp, timestampBase);

            if (rewriteTimestampBase == INVALID_TIMESTAMP)
            {
                rewriteTimestampBase = rewriteTimestampBaseInit;
            }

            long ts = rewriteTimestampBase + lastTimestampDiff;

            rewriteTimestampBaseInit = ts;

            if (logger.isInfoEnabled())
            {
                logger.info("Injecting RTP ssrc=" + rtpPacket.getSSRCAsLong()
                        + "->" + ssrc
                        + ", seq=" + rtpPacket.getSequenceNumber()
                        + "->" + sn
                        + ", ts=" + rtpPacketTimestamp
                        + "->" + ts);
            }

            rtpPacket.setSSRC((int) ssrc);
            rtpPacket.setSequenceNumber(sn);
            rtpPacket.setTimestamp(ts);

            return rtpPacket;
        }

        /**
         * Triggers RTCP report generation based on our own statistics.
         *
         * @param rtcpPacket the <tt>RawPacket</tt> to rewrite.
         * @return the rewritten <tt>RawPacket</tt> or null.
         */
        private RawPacket rewriteRTCP(RawPacket rtcpPacket)
        {
            // TODO trigger RTCP generation upon RTCP reception.

            return null;
        }

        /**
         *
         */
        public void run()
        {
            while (true)
            {
                RawPacket next = null;

                try
                {
                    next = queue.take();
                }
                catch (InterruptedException e)
                {
                }

                if (closed)
                {
                    break;
                }

                boolean data =  RTPPacketPredicate.INSTANCE.test(next);

                // We want to make the rewriting before the scheduling because
                // the scheduling code takes into account the RTP timestamps.
                next = data ? rewriteRTP(next) : rewriteRTCP(next);

                try
                {
                    rawPacketScheduler.schedule(next);
                }
                catch (InterruptedException e)
                {

                }

                if (closed)
                {
                    break;
                }

                try
                {
                    if (next != null)
                    {
                        stream.injectPacket(next, data, null);
                    }
                }
                catch (TransmissionFailedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Starts capturing media from this stream's <tt>MediaDevice</tt> and then
     * streaming it through the local <tt>StreamConnector</tt> toward the
     * stream's target address and port. The method also puts the
     * <tt>MediaStream</tt> in a listening state that would make it play all
     * media received from the <tt>StreamConnector</tt> on the stream's
     * <tt>MediaDevice</tt>.
     */
    public void start()
    {
        synchronized (this)
        {
            if (started)
            {
                return;
            }

            started = true;
        }

        stream.start();

        if (pcapChooser == null)
        {
            return;
        }

        loopThread = new Thread(() -> {

            Pcap pcap = pcapChooser.getVideoPcap();
            if (pcap == null)
            {
                return;
            }

            while (!closed)
            {
                // Make sure the packet handler is not stopped.
                packetHandler.restart();

                // Loop through the file while we're not closed.
                try
                {
                    pcap.loop(packetHandler);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                // We need a new Pcap in order to loop again.
                pcap = pcapChooser.getVideoPcap();
            }

            for (PacketEmitter pem : emitterMap.values())
            {
                pem.interrupt();
                try
                {
                    pem.join();
                }
                catch (InterruptedException e)
                {
                }
            }

        }, "loopThread");

        loopThread.start();
    }

    /**
     * Sets the <tt>StreamConnector</tt> to be used by this <tt>MediaStream</tt>
     * for sending and receiving media.
     *
     * @param connector the <tt>StreamConnector</tt> to be used by this
     * <tt>MediaStream</tt> for sending and receiving media
     */
    public void setConnector(StreamConnector connector)
    {
        stream.setConnector(connector);
    }

    /**
     * Sets the target of this <tt>MediaStream</tt> to which it is to send and
     * from which it is to receive data (e.g. RTP) and control data (e.g. RTCP).
     *
     * @param target the <tt>MediaStreamTarget</tt> describing the data
     * (e.g. RTP) and the control data (e.g. RTCP) locations to which this
     * <tt>MediaStream</tt> is to send and from which it is to receive
     */
    public void setTarget(MediaStreamTarget target)
    {
        stream.setTarget(target);
    }

    /**
     * Adds the SSRCs of the wrapped <tt>MediaStream</tt> to the mediaPacket
     * passed in as a parameter.
     *
     * @param mediaPacket the <tt>MediaPacketExtension</tt> to add the SSRCs to.
     */
    public void updateMediaPacketExtension(MediaPacketExtension mediaPacket)
    {
        Set<Long> ssrcs;
        if (ssrcsMap == null || (ssrcs = ssrcsMap.keySet()).isEmpty())
        {
            String str = String.valueOf(stream.getLocalSourceID());
            mediaPacket.addSource(
                    getFormat().getMediaType().toString(),
                    str,
                    MediaDirection.SENDRECV.toString());
        }
        else
        {
            for (long ssrc : ssrcs)
            {
                mediaPacket.addSource(getFormat().getMediaType().toString(),
                                      String.valueOf(ssrcsMap.get(ssrc)),
                                      MediaDirection.SENDRECV.toString());
            }
        }
    }

    /**
     * Returns the <tt>MediaFormat</tt> that this stream is currently
     * transmitting in.
     *
     * @return the <tt>MediaFormat</tt> that this stream is currently
     * transmitting in.
     */
    public MediaFormat getFormat()
    {
        return format;
    }

    /**
     * The <tt>PacketHandler</tt> implementation that loops through the PCAP
     * file. It keeps track of VP8 keyframe indexes in the pcap file and
     * is able to restart streaming from that specific location.
     */
    class VideoPacketHandler implements PacketHandler
    {
        /**
         * The index of the most recent keyframe.
         */
        private long idxKeyframe = UNINITIALIZED_IDX;

        /**
         * The index of the current packet being processed.
         */
        private long idxCurrentPacket = UNINITIALIZED_IDX;

        /**
         * The indicator which determines whether this packet handler should
         * keep on looping or not.
         */
        private boolean stopped = false;

        /**
         * Re-initializes this <tt>VideoPacketHandler</tt>.
         */
        public synchronized void restart()
        {
            for (PacketEmitter pem : emitterMap.values())
            {
                pem.restart();
            }

            logger.info("Restarting this packet handler.");
            stopped = false;
            idxCurrentPacket = UNINITIALIZED_IDX;
        }

        public synchronized void stop()
        {
            logger.info("Stopping this packet handler.");
            this.stopped = true;
        }

        @Override
        public boolean nextPacket(Packet packet) throws IOException
        {
            synchronized (this)
            {
                if (stopped)
                {
                    // We've been instructed to stop -> skip all subsequent packets.
                    return false;
                }
            }

            idxCurrentPacket++;

            if (idxCurrentPacket < idxKeyframe)
            {
                // We're not yet at the restart location.
                return true;
            }

            byte[] sharedbuff = packet.getPacket(Protocol.UDP)
                .getPayload().getArray();

            byte[] buff = new byte[sharedbuff.length];

            // We just want to avoid problems with shared buffers. Not sure
            // if the underlying library supports that.
            System.arraycopy(sharedbuff, 0, buff, 0, sharedbuff.length);
            RawPacket next = new RawPacket(buff, 0, buff.length);

            // Check if this is a keyframe and update the
            // latestKeyFrameIdentification.

            if (Utils.isKeyFrame(next, RED_PT, VP8_PT))
            {
                if (idxKeyframe == UNINITIALIZED_IDX
                    || idxCurrentPacket - idxKeyframe > KEYFRAME_MIN_DISTANCE)
                {
                    logger.debug("New keyframe idx: " + idxKeyframe);
                    idxKeyframe = idxCurrentPacket;
                }
            }

            long ssrc = RTPPacketPredicate.INSTANCE.test(next)
                ? next.getSSRCAsLong() : INVALID_SSRC;

            if (!emitterMap.containsKey(ssrc))
            {
                logger.info("Creating a new PacketEmitter.");
                PacketEmitter pem = new PacketEmitter(ssrc);
                pem.start();
                emitterMap.put(ssrc, pem);
            }

            PacketEmitter pem = emitterMap.get(ssrc);
            try
            {
                pem.queue.put(next);
            }
            catch (InterruptedException e)
            {
            }

            return true;
        }
    }
}
