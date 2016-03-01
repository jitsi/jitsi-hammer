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

import io.pkts.Pcap;
import org.jitsi.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * In the same spirit as the <tt>MediaDeviceChooser</tt>, this class is used to
 * get the chosen Pcap file for Video and Audio (although audio is not
 * currently supported).
 *
 * @author George Politis
 */
public class PcapChooser {

    /**
     * Holds the SSRCs of the RTP streams inside the Pcap file.
     */
    private long[] ssrcs;

    /**
     * The <tt>CmdLineArguments</tt> containing the arguments/options of the
     * program, on which the audio and video Pcap will be chosen.
     */
    private CmdLineArguments cmdLineArguments;

    /**
     * Ctor.
     *
     * @param cmdArgs the <tt>CmdLineArguments</tt> containing the
     * arguments/options of the program, on which the audio and video Pcap will
     * be chosen.
     */
    public PcapChooser(CmdLineArguments cmdArgs)
    {
        this.cmdLineArguments = cmdArgs;

        String videoPcapPathname = cmdLineArguments.getVideoPcapFile();
        if (StringUtils.isNullOrEmpty(videoPcapPathname))
        {
            return;
        }

        File videoPcapFile = new File(videoPcapPathname);
        if (!videoPcapFile.exists() || !videoPcapFile.isFile())
        {
            return;
        }

        String name = videoPcapFile.getName();
        int pos = name.lastIndexOf(".");
        if (pos > 0) {
            name = name.substring(0, pos);
        }

        String videoDescPathname = videoPcapFile.getParent() + "/" + name + ".txt";
        File videoDescFile = new File(videoDescPathname);
        try (BufferedReader br = new BufferedReader(new FileReader(videoDescFile)))
        {
            String line = "";
            while ((line = br.readLine()) != null)
            {
                if (!StringUtils.isNullOrEmpty(line))
                {
                    break;
                }
            }

            String[] strings = line.split(" ");
            ssrcs = new long[strings.length];

            for (int i = 0; i < strings.length; i++)
            {
                ssrcs[i] = Long.decode(strings[i]);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Always returns null, audio is not supported right now.
     */
    public Pcap getAudioPcap()
    {
        // We don't need to stream audio from a pcap atm.
        return null;
    }

    /**
     * Returns a _new_ Pcap file based on the command line arguments.
     */
    public Pcap getVideoPcap()
    {
        // XXX we want a new Pcap because we want multiple processes (fake
        // users) to be able to loop through the same Pcap file independently.
        String videoPcapPathname = cmdLineArguments.getVideoPcapFile();
        if (StringUtils.isNullOrEmpty(videoPcapPathname))
        {
            return null;
        }

        File videoPcapFile = new File(videoPcapPathname);
        if (!videoPcapFile.exists() || !videoPcapFile.isFile())
        {
            return null;
        }

        try
        {
            return Pcap.openStream(videoPcapFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the SSRCs of the RTP streams inside the Pcap file.
     */
    public long[] getVideoSsrcs()
    {
        return ssrcs;
    }
}
