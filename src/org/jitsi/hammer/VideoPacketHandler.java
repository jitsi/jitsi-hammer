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
import org.jitsi.impl.neomedia.*;
import org.jitsi.impl.neomedia.codec.video.vp8.*;
import org.jitsi.util.*;

import java.io.*;

/**
 * The <tt>PacketHandler</tt> implementation that loops through the PCAP
 * file. It keeps track of VP8 keyframe indexes in the pcap file and
 * is able to restart streaming from that specific location.
 *
 * @author George Politis
 */
class VideoPacketHandler
    implements PacketHandler
{
    /**
     * The <tt>Logger</tt> used by {@link FakeStream} and its instances
     * for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(VideoPacketHandler.class);

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
     * Holds a value that represents an invalid index.
     */
    private static final long UNINITIALIZED_IDX = -1L;

    /**
     * Holds a value that represents an invalid SSRC.
     */
    private static final long INVALID_SSRC = -1L;

    private FakeStream fakeStream;
    
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

    public VideoPacketHandler(FakeStream fakeStream)
    {
        this.fakeStream = fakeStream;
    }

    /**
     * Re-initializes this <tt>VideoPacketHandler</tt>.
     */
    public synchronized void restart()
    {
        for (PacketInjector packetInjector : fakeStream.packetInjectors.values())
        {
            packetInjector.restart();
        }

        logger.info("Restarting this packet handler.");
        stopped = false;
        idxCurrentPacket = UNINITIALIZED_IDX;
    }

    /**
     * Instructs this instance to stop processing packets.
     */
    public synchronized void stop()
    {
        logger.info("Stopping this packet handler.");
        this.stopped = true;
    }

    @Override
    public boolean nextPacket(Packet packet) throws
        IOException
    {
        synchronized (this)
        {
            if (stopped)
            {
                // We've been instructed to stop -> skip all subsequent
                // packets.
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

        if (!fakeStream.packetInjectors.containsKey(ssrc))
        {
            logger.info("Creating a new PacketInjector.");
            PacketInjector packetInjector = new PacketInjector(fakeStream, ssrc);
            packetInjector.start();
            fakeStream.packetInjectors.put(ssrc, packetInjector);
        }

        PacketInjector packetInjector = fakeStream.packetInjectors.get(ssrc);
        try
        {
            packetInjector.queue.put(next);
        }
        catch (InterruptedException e)
        {
        }

        return true;
    }
}
