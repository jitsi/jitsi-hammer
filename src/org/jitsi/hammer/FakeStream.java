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
import io.pkts.protocol.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;
import org.jitsi.hammer.extension.*;
import org.jitsi.hammer.utils.*;
import org.jitsi.impl.neomedia.*;
import org.jitsi.impl.neomedia.format.VideoMediaFormatImpl;
import org.jitsi.impl.neomedia.jmfext.media.protocol.rtpdumpfile.*;
import org.jitsi.service.libjitsi.*;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.device.*;
import org.jitsi.service.neomedia.format.*;
import org.jitsi.util.*;

import java.io.IOException;
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
     * The <tt>Logger</tt> used by <tt>RtpdumpStream</tt> and its instances
     * for logging output.
     */
    private static final Logger logger = Logger.getLogger(FakeStream.class);

    /**
     * The <tt>MediaStream</tt> that this instance wraps.
     */
    private final MediaStream stream;

    /**
     * The PCAP file to stream, if any.
     */
    private final Pcap pcap;

    /**
     * The SSRCs in the PCAP file to stream, if any.
     */
    private final Map<Long, Long> ssrcsMap;

    /**
     * The SSRCs in the PCAP file to stream, if any.
     */
    private final long[] ssrcs;

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
            this.pcap = pcapChooser.getVideoPcap();
            this.ssrcs = pcapChooser.getVideoSsrcs();
            if (this.ssrcs == null || this.ssrcs.length == 0)
            {
                this.ssrcsMap = null;
                return;
            }

            this.ssrcsMap = new ConcurrentHashMap<>(ssrcs.length);
            for (int i = 0; i < ssrcs.length; i++)
            {
                this.ssrcsMap.put(ssrcs[i],
                        Math.abs(new Random().nextLong()) % Integer.MAX_VALUE);
            }
        }
        else
        {
            this.pcap = null;
            this.ssrcsMap = null;
            this.ssrcs = null;
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

        if (ssrcs == null || ssrcs.length == 0)
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
            for (int i = 0; i < ssrcs.length; i++)
            {
                SourcePacketExtension sourcePacketExtension =
                        new SourcePacketExtension();

                sourcePacketExtension.setSSRC(ssrcsMap.get(ssrcs[i]));
                sourcePacketExtension.addChildExtension(
                        new ParameterPacketExtension("cname",
                                mediaService.getRtpCname()));
                sourcePacketExtension.addChildExtension(
                        new ParameterPacketExtension("msid", msLabel + " " + label));
                sourcePacketExtension.addChildExtension(
                        new ParameterPacketExtension("mslabel", msLabel));
                sourcePacketExtension.addChildExtension(
                        new ParameterPacketExtension("label", label));
                sources.add(sourcePacketExtension);
                description.addChildExtension(sourcePacketExtension);
            }

            if (ssrcs.length > 1)
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
     * The <tt>Thread</tt> responsible for emitting RTP packets for a specific
     * SSRC.
     */
    class PacketEmitter extends Thread
    {
        private static final int QUEUE_SIZE = 100;

        private final RawPacketScheduler rawPacketScheduler;

        final BlockingQueue<RawPacket> queue;

        public PacketEmitter()
        {
            queue = new LinkedBlockingQueue<>(QUEUE_SIZE);
            rawPacketScheduler = new RawPacketScheduler(
                    (long) VideoMediaFormatImpl.DEFAULT_CLOCK_RATE);
        }

        private RawPacket rewriteRTP(RawPacket rtpPacket)
        {
            long ssrc = ssrcsMap.get(rtpPacket.getSSRCAsLong());
            if (logger.isDebugEnabled())
            {
                logger.debug("Injecting RTP ssrc=" + rtpPacket.getSSRCAsLong()
                        + "->" + ssrc
                        + ", seq=" + rtpPacket.getSequenceNumber()
                        + ", ts=" + rtpPacket.getTimestamp());
            }
            rtpPacket.setSSRC((int) ssrc);

            // XXX Eventually (and in order to support pcap looping) we'll have
            // to rewrite sequence numbers and timestamps.

            return rtpPacket;
        }

        private RawPacket rewriteRTCP(RawPacket rtcpPacket)
        {
            // TODO trigger RTCP generation upon RTCP reception.

            return null;
        }

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
                    boolean data =  RTPPacketPredicate.INSTANCE.test(next);

                    next = data ? rewriteRTP(next) : rewriteRTCP(next);

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

        if (pcap == null)
        {
            return;
        }

        final Map<Long, PacketEmitter> emitterMap = new HashMap<>();

        loopThread = new Thread(() -> {
            try
            {
                pcap.loop(packet -> {

                    if (closed)
                    {
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

                        return;
                    }

                    byte[] buff = packet.getPacket(Protocol.UDP)
                            .getPayload().getArray();
                    RawPacket next = new RawPacket(buff, 0, buff.length);

                    long ssrc = RTPPacketPredicate.INSTANCE.test(next)
                            ? next.getSSRCAsLong()
                            : -1;

                    if (!emitterMap.containsKey(ssrc))
                    {
                        PacketEmitter pem = new PacketEmitter();
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
                });
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });

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
        if (ssrcs == null || ssrcs.length == 0)
        {
            String str = String.valueOf(stream.getLocalSourceID());
            mediaPacket.addSource(
                    getFormat().getMediaType().toString(), str,
                    MediaDirection.SENDRECV.toString());
        }
        else
        {
            for (int i = 0; i < ssrcs.length; i++)
            {
                mediaPacket.addSource(getFormat().getMediaType().toString(),
                        String.valueOf(ssrcsMap.get(ssrcs[i])),
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
}
