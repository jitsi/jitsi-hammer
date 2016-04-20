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

import org.jitsi.impl.neomedia.*;
import org.jitsi.impl.neomedia.format.*;
import org.jitsi.impl.neomedia.jmfext.media.protocol.rtpdumpfile.*;
import org.jitsi.service.neomedia.*;
import org.jitsi.util.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * The <tt>Thread</tt> responsible for emitting RTP packets for a specific
 * SSRC.
 *
 * @author George Politis
 */
class PacketInjector
    extends Thread
{
    /**
     * The <tt>Logger</tt> used by {@link FakeStream} and its instances
     * for logging output.
     */
    private static final Logger logger = Logger.getLogger(PacketInjector.class);

    /**
     * Determines how big the queue size should be for this
     * <tt>PacketInjector</tt>.
     */
    private static final int QUEUE_CAPACITY = 100;

    /**
     * Holds a value that represents an invalid timestamp.
     */
    private static final long INVALID_TIMESTAMP = -1;

    /**
     *
     */
    private final FakeStream fakeStream;

    /**
     * The queue of RTP packets waiting to be emitted.
     */
    final BlockingQueue<RawPacket> queue;

    /**
     * The <tt>RawPacketScheduler</tt> that schedules RTP packets for
     * emission.
     */
    private final RawPacketScheduler rawPacketScheduler;

    /**
     * The <tt>Random</tt> that generates initial sequence numbers. Instances of
     * {@code java.util.Random} are thread-safe since Java 1.7.
     */
    private static final Random RANDOM = new Random();

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
     * Ctor.
     */
    public PacketInjector(FakeStream fakeStream, long ssrc)
    {
        super("PacketInjector-" + Long.toString(ssrc));
        this.fakeStream = fakeStream;
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

        long ssrc = fakeStream.ssrcsMap.get(rtpPacket.getSSRCAsLong());

        int sn = rewriteSeqNum++;

        lastTimestampDiff
            = TimeUtils.rtpDiff(rtpPacketTimestamp, timestampBase);

        if (rewriteTimestampBase == INVALID_TIMESTAMP)
        {
            rewriteTimestampBase = fakeStream.rewriteTimestampBaseInit;
        }

        long ts = rewriteTimestampBase + lastTimestampDiff;

        fakeStream.rewriteTimestampBaseInit = ts;

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

            if (fakeStream.closed)
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

            if (fakeStream.closed)
            {
                break;
            }

            try
            {
                if (next != null)
                {
                    fakeStream.stream.injectPacket(next, data, null);
                }
            }
            catch (TransmissionFailedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
