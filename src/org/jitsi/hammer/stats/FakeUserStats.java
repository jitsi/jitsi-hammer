/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer.stats;

import org.jitsi.service.neomedia.*;

/**
 * @author Thomas Kuntz
 *
 */
public class FakeUserStats
{
    public static final String jsonMediaStreamStatsTemplate =
        "{"
        + " \"ssrc\":%s"
        + " , \"DownloadJitterMs\":%s"
        + " , \"DownloadPercentLoss\":%s"
        + " , \"DownloadRateKiloBitPerSec\":%s"
        + " , \"JitterBufferDelayMs\":%s"
        + " , \"JitterBufferDelayPackets\":%s"
        + " , \"NbDiscarded\":%s"
        + " , \"NbDiscardedFull\":%s"
        + " , \"NbDiscardedLate\":%s"
        + " , \"NbDiscardedReset\":%s"
        + " , \"NbDiscardedShrink\":%s"
        + " , \"NbFec\":%s"
        + " , \"NbPackets\":%s"
        + " , \"NbPacketsLost\":%s"
        + " , \"NbReceivedBytes\":%s"
        + " , \"NbSentBytes\":%s"
        + " , \"PacketQueueCountPackets\":%s"
        + " , \"PacketQueueSize\":%s"
        + " , \"PercentDiscarded\":%s"
        + " , \"RttMs\":%s"
        + " , \"UploadJitterMs\":%s"
        + " , \"UploadPercentLoss\":%s"
        + " , \"UploadRateKiloBitPerSec\":%s"
        + " }";

    public static final String jsonTemplate =
              "  {\n"
            + "      \"audio\" : "
            + FakeUserStats.jsonMediaStreamStatsTemplate
            + ",\n"
            + "      \"video\" : "
            + FakeUserStats.jsonMediaStreamStatsTemplate
            + "\n"
            + "  }";

    private String username = "Hammer";
    private long audioSSRC = -1;
    private MediaStreamStats audioStats;
    private long videoSSRC = -1;
    private MediaStreamStats videoStats;

    public void setMediaStreamStats(MediaStream mediaStream)
    {
        if(mediaStream == null)
        {
            throw new NullPointerException("MediaStream can't be null");
        }

        if(mediaStream instanceof AudioMediaStream)
        {
            audioSSRC = mediaStream.getLocalSourceID();
            audioStats = mediaStream.getMediaStreamStats();
        }
        else if(mediaStream instanceof VideoMediaStream)
        {
            videoSSRC = mediaStream.getLocalSourceID();
            videoStats = mediaStream.getMediaStreamStats();
        }
        //other MediaType are not tracked.
    }

    public void setUsername(String username)
    {
        this.username = username;
    }


    public void updateStats()
    {
        audioStats.updateStats();
        videoStats.updateStats();
    }


    public MediaStreamStats getMediaStreamStats(MediaType type)
    {
        MediaStreamStats stats = null;
        switch(type)
        {
        case AUDIO:
            stats = audioStats;
            break;
        case VIDEO:
            stats = videoStats;
            break;
        default:
            break;
        }

        return stats;
    }


    public String getStatsJSON(int spaceIdent)
    {
        String ident = new String(new char[spaceIdent]).replace("\0" ,  " ");
        StringBuilder builder = new StringBuilder();
        builder.append(ident + "{\n");
        builder.append(ident + "  \"username\" : \""+ this.username +"\" , \n");
        builder.append(ident + "  \"streams\" :\n");

        String str = String.format(jsonTemplate,
            audioSSRC ,
            audioStats.getDownloadJitterMs() ,
            audioStats.getDownloadPercentLoss() ,
            audioStats.getDownloadRateKiloBitPerSec() ,
            audioStats.getJitterBufferDelayMs() ,
            audioStats.getJitterBufferDelayPackets() ,
            audioStats.getNbDiscarded() ,
            audioStats.getNbDiscardedFull() ,
            audioStats.getNbDiscardedLate() ,
            audioStats.getNbDiscardedReset() ,
            audioStats.getNbDiscardedShrink() ,
            audioStats.getNbFec() ,
            audioStats.getNbPackets() ,
            audioStats.getNbPacketsLost() ,
            audioStats.getNbReceivedBytes() ,
            audioStats.getNbSentBytes() ,
            audioStats.getPacketQueueCountPackets() ,
            audioStats.getPacketQueueSize() ,
            audioStats.getPercentDiscarded() ,
            audioStats.getRttMs() ,
            audioStats.getUploadJitterMs(),
            audioStats.getUploadPercentLoss() ,
            audioStats.getUploadRateKiloBitPerSec() ,

            videoSSRC ,
            videoStats.getDownloadJitterMs() ,
            videoStats.getDownloadPercentLoss() ,
            videoStats.getDownloadRateKiloBitPerSec() ,
            videoStats.getJitterBufferDelayMs() ,
            videoStats.getJitterBufferDelayPackets() ,
            videoStats.getNbDiscarded() ,
            videoStats.getNbDiscardedFull() ,
            videoStats.getNbDiscardedLate() ,
            videoStats.getNbDiscardedReset() ,
            videoStats.getNbDiscardedShrink() ,
            videoStats.getNbFec() ,
            videoStats.getNbPackets() ,
            videoStats.getNbPacketsLost() ,
            videoStats.getNbReceivedBytes() ,
            videoStats.getNbSentBytes() ,
            videoStats.getPacketQueueCountPackets() ,
            videoStats.getPacketQueueSize() ,
            videoStats.getPercentDiscarded() ,
            videoStats.getRttMs() ,
            videoStats.getUploadJitterMs(),
            videoStats.getUploadPercentLoss() ,
            videoStats.getUploadRateKiloBitPerSec());
        str = ident + str.replaceAll("\n", "\n"+ident);
        builder.append(str + '\n');

        builder.append(ident + "}");

        return builder.toString();
    }
}
