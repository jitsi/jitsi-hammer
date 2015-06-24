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

package org.jitsi.hammer.utils;


import org.jitsi.hammer.*;
import org.jitsi.impl.neomedia.jmfext.media.protocol.greyfading.*;
import org.jitsi.impl.neomedia.jmfext.media.protocol.ivffile.*;
import org.jitsi.impl.neomedia.jmfext.media.protocol.rtpdumpfile.*;
import org.jitsi.service.libjitsi.*;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.codec.*;
import org.jitsi.service.neomedia.device.*;
import org.jitsi.service.neomedia.format.MediaFormatFactory;
import org.jitsi.util.Logger;
import org.jitsi.videobridge.*;

import javax.media.*;
import javax.media.format.*;

/**
 * This class is used to get the chosen MediaDevice for a given MediaType.
 *
 * The choice is based on the option/argument given to the program (parsed
 * and stored in a <tt>CmdLineArguments</tt>).
 *
 * @author Thomas Kuntz
 */
public class MediaDeviceChooser
{
    /**
     * The <tt>Logger</tt> used by the <tt>MediaDeviceChooser</tt> class and its
     * instances for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(MediaDeviceChooser.class);

    /**
     * The chosen video MediaDevice of this <tt>MediaDeviceChooser</tt>
     */
    MediaDevice videoMediaDevice;

    /**
     * The chosen audio MediaDevice of this <tt>MediaDeviceChooser</tt>
     */
    MediaDevice audioMediaDevice;

    /**
     * Initialize an empty <tt>MediaDeviceChooser<tt>. No video or audio
     * MediaDevice will be chosen (they'll need to be set later).
     */
    public MediaDeviceChooser()
    {
        this(null);
    }

    /**
     * Initialize a <tt>MediaDeviceChooser</tt> based on a <tt>CmdLineArguments</tt>
     * (so based on the arguments/options the user gave to the program).
     *
     * @param cmdArg the <tt>CmdLineArguments</tt> containing the arguments/options
     * of the program, on which the audio and video MediaDevice will be chosen.
     */
    public MediaDeviceChooser(CmdLineArguments cmdArg)
    {
        if(cmdArg != null)
        {
            String str
                = "Creating a MediaDeviceChooser from console arguments :\n";

            MediaService service = LibJitsi.getMediaService();
            MediaFormatFactory factory = service.getFormatFactory();

            /*
             * If an rtpdump file is given, it has priority over
             * AudioSilence
             */
            if(cmdArg.getAudioRtpdumpFile() != null)
            {
                str = str + "-with rtpdump file " + cmdArg.getAudioRtpdumpFile()
                    + " for the audio stream.\n";
                AudioFormat opusFormat
                    = new AudioFormat(
                            Constants.OPUS_RTP,
                            48000,
                            Format.NOT_SPECIFIED,
                            2 /* channels */)
                {
                    /**
                     * FMJ depends on this value when it calculates the RTP
                     * timestamps on the packets that it sends.
                     *
                     * This limits the supported files to only files with 20ms
                     * opus frames.
                     */
                    @Override
                    public long computeDuration(long length)
                    {
                        return 20L * 1000 * 1000;
                    }
                };
                audioMediaDevice
                    = RtpdumpMediaDevice.createRtpdumpAudioMediaDevice(
                            cmdArg.getAudioRtpdumpFile(),
                            opusFormat);

            }
            else
            {
                str = str
                    + "-with AudioSilenceMediaDevice for the audio stream.\n";
                audioMediaDevice = new AudioSilenceMediaDevice();
            }

            /*
             * For the video MediaDevice, an rtpdump CaptureDevice has priority
             * over an ivf CaptureDevice that has priority over
             * the VideoGreyFading CaptureDevice.
             */
            if(cmdArg.getVideoRtpdumpFile() != null)
            {
                str = str + "-with rtpdump file " + cmdArg.getVideoRtpdumpFile()
                    + " for the video stream\n";
                videoMediaDevice
                    = RtpdumpMediaDevice.createRtpdumpVideoMediaDevice(
                            cmdArg.getVideoRtpdumpFile(),
                            Constants.VP8_RTP,
                            factory.createMediaFormat("vp8", 90000));
            }
            else if(cmdArg.getIVFFile() != null)
            {
                str = str + "-with ivf file " + cmdArg.getIVFFile()
                    + " for the video stream\n";
                videoMediaDevice = new IVFMediaDevice(cmdArg.getIVFFile());
            }
            else
            {
                str = str + "-with a fading from black to white to black..."
                    + " for the video stream\n";
                videoMediaDevice = new VideoGreyFadingMediaDevice();
            }
            logger.info(str);
        }
    }

    /**
     * Get the chosen <tt>MediaDevice</tt> from a <tt>MediaType</tt>
     * @return the chosen <tt>MediaDevice</tt>
     */
    public synchronized MediaDevice getMediaDevice(MediaType type)
    {
        MediaDevice returnedDevice = null;
        switch(type)
        {
            case AUDIO:
                returnedDevice = audioMediaDevice;
                break;
            case VIDEO:
                returnedDevice = videoMediaDevice;
                break;
            default:
                break;
        }

        return returnedDevice;
    }

    /**
     * Set a <tt>MediaDevice</tt> as the chosen audio or video <tt>MediaDevice<tt>
     * (depending of its <tt>MediaType</tt>)
     * @param dev the <tt>MediaDevice</tt> that will be considered chosen.
     */
    public synchronized void setMediaDevice(MediaDevice dev)
    {
        if(dev != null)
        {
            logger.info("Set " + dev + " as the MediaDevice for "
                    + dev.getMediaType() + " stream");
            switch(dev.getMediaType())
            {
                case VIDEO:
                    videoMediaDevice = dev;
                    break;
                case AUDIO:
                    audioMediaDevice = dev;
                    break;
            default:
                break;
            }
        }
    }
}
