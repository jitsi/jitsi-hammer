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

package org.jitsi.hammer.stats;

import org.jitsi.service.neomedia.*;

/**
 * @author Thomas Kuntz
 *
 */
public class FakeUserStats
{
    /**
     * A JSON template of all the stats a <tt>MediaStreamStats</tt> have.
     * It needs to be formatted with String.format() .
     */
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

    /**
     * A JSON template of all the stats a <tt>FakeUser</tt> have (
     * stats for an audio stream, and for video stream).
     * It needs to be formatted with String.format() .
     */
    public static final String jsonTemplate =
              "  {\n"
            + "      \"audio\" : "
            + FakeUserStats.jsonMediaStreamStatsTemplate
            + ",\n"
            + "      \"video\" : "
            + FakeUserStats.jsonMediaStreamStatsTemplate
            + "\n"
            + "  }";

    /**
     * The username of the <tt>FakeUser</tt> corresponding to this
     * <tt>FakeUserStats</tt>.
     */
    private String username = "Hammer";

    /**
     * The SSRC of the audio <tt>MediaStream</tt> of the <tt>FakeUser</tt>
     * corresponding to this <tt>FakeUserStats</tt>.
     */
    private long audioSSRC = -1;

    /**
     * The <tt>MediaStreamStats</tt> of the audio <tt>MediaStream</tt> of the
     * <tt>FakeUser</tt> corresponding to this <tt>FakeUserStats</tt>.
     */
    private MediaStreamStats audioStats;

    /**
     * The SSRC of the video <tt>MediaStream</tt> of the <tt>FakeUser</tt>
     * corresponding to this <tt>FakeUserStats</tt>.
     */
    private long videoSSRC = -1;

    /**
     * The <tt>MediaStreamStats</tt> of the video <tt>MediaStream</tt> of the
     * <tt>FakeUser</tt> corresponding to this <tt>FakeUserStats</tt>.
     */
    private MediaStreamStats videoStats;

    public FakeUserStats(String username)
    {
        this.username = username;
    }

    /**
     * Set the SSRC and <tt>MediaStreamStats</tt> of mediaStream
     * depending of the stream type.
     * @param mediaStream
     */
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

    /**
     * Set the username of used by this <tt>FakeUserStats</tt>.
     * @param username the username that will be set.
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * Call updateStats() of the 2 <tt>MediaStreamStats</tt> of this
     * <tt>FakeUserStats</tt> corresponding to the audio and video stream.
     */
    public void updateStats()
    {
        audioStats.updateStats();
        videoStats.updateStats();
    }

    /**
     * Get the audio or video <tt>MediaStreamStats</tt> this
     * <tt>FakeUserStats</tt> has depending on value of type
     * @param type the MediaType used to know if the method must return
     * the <tt>MediaStreamStats</tt> of the audio or video <tt>MediaStream</tt>
     * of the corresponding FakeUser.
     * @return the audio or video <tt>MediaStreamStats</tt> this
     * <tt>FakeUserStats</tt> has depending on value of type
     */
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

    /**
     * Get the stats of the audio and video stream from the corresponding
     * <tt>MediaStreamStats</tt> in JSON.
     * @param spaceIndent the number of space added to indent each line
     * of the returned JSON.
     * @return the stats of the audio and video stream from the corresponding
     * <tt>MediaStreamStats</tt> in JSON.
     */
    public String getStatsJSON(int spaceIndent)
    {
        String indent = new String(new char[spaceIndent]).replace("\0" ,  " ");
        StringBuilder builder = new StringBuilder();
        builder.append(indent + "{\n");
        builder.append(indent + "  \"username\" : \""+ this.username +"\" , \n");
        builder.append(indent + "  \"streams\" :\n");

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
        str = indent + str.replaceAll("\n", "\n"+indent);
        builder.append(str + '\n');

        builder.append(indent + "}");

        return builder.toString();
    }
}
