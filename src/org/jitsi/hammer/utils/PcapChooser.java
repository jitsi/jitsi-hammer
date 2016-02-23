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
 * @author George Politis
 */
public class PcapChooser {

    private Pcap videoPcap;

    private long[] ssrcs;

    public PcapChooser(CmdLineArguments cmdArgs)
    {
        String videoPcapPathname = cmdArgs.getVideoPcapFile();
        if (StringUtils.isNullOrEmpty(videoPcapPathname))
        {
            return;
        }

        File videoPcapFile = new File(videoPcapPathname);
        if (!videoPcapFile.exists() || !videoPcapFile.isFile())
        {
            return;
        }

        try
        {
            videoPcap = Pcap.openStream(videoPcapFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (videoPcap == null)
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


    public Pcap getAudioPcap()
    {
        // We don't need to stream audio from a pcap atm.
        return null;
    }

    public Pcap getVideoPcap()
    {
        return videoPcap;
    }

    public long[] getVideoSsrcs()
    {
        return ssrcs;
    }
}
