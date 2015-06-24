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

import org.ice4j.ice.*;
import org.ice4j.*;
import org.ice4j.ice.harvest.*;
import java.util.*;
import java.io.*;


/**
 *
 * @author Thomas Kuntz
 *
 * This class is used to generate <tt>IceMediaStream</tt> without overlapping
 * the port numbers used for each.
 *
 * This class proposes a static instance of itself (with default value for min
 * and max port number) to directly use it without having to instantiate one.
 */
public class IceMediaStreamGenerator
{
    /**
     * The static instance of the IceMediaStreamGenerator.
     */
    private static IceMediaStreamGenerator generator =
            new IceMediaStreamGenerator();

    /**
     * The minimum value that will be used as port when generating new
     * <tt>IceMediaStream</tt>.
     */
    private int MIN_COMPONENT_PORT = 6000;
    /**
     * The maximum value that will be used as port when generating new
     * <tt>IceMediaStream</tt>.
     */
    private int MAX_COMPONENT_PORT = 9000;

    /**
     * The current value that will be used as port when generating new
     * <tt>IceMediaStream</tt>.
     */
    private int CURRENT_COMPONENT_PORT = MIN_COMPONENT_PORT;


    /**
     * Initializes a new <tt>IceMediaStream</tt> instance with
     * default value for the minimum and maximum port value.
     */
    public IceMediaStreamGenerator() {}

    /**
     * Initializes a new <tt>IceMediaStream</tt> instance with
     * the given minimum and maximum port number value.
     * @arg min_port The minimum value a port
     * of an generated <tt>IceMediaStream</tt> can be.
     * @arg max_port The maximum value a port
     * of an generated <tt>IceMediaStream</tt> can be.
     */
    public IceMediaStreamGenerator(int min_port, int max_port)
    {
        MIN_COMPONENT_PORT = min_port;
        CURRENT_COMPONENT_PORT = min_port;
        MAX_COMPONENT_PORT = max_port;
    }

    /**
     * Get the static instance of the <tt>IceMediaStreamGenerator</tt>
     * return the static instance of the <tt>IceMediaStreamGenerator</tt>
     */
    public static IceMediaStreamGenerator getInstance()
    {
        return generator;
    }

    /**
     * Generate an <tt>Agent</tt> having an <tt>IceMediaStream</tt>
     * for each media name in <tt>mediaNameSet</tt>.
     * The <tt>Agent</tt> will try to harvest all candidates for STUN and TURN
     * contained in <tt>stunAddresses</tt> and <tt>turnAddresses</tt>.
     *
     * Each <tt>IceMediaStrem</tt> will have 2 <tt>Component</tt>, one for RTP
     * and one for RTCP, and using UDP as transport protocol.
     * @param agent the agent in which will be created the <tt>IceMediaStream</tt>
     * @param mediaNameSet A set of media name used to create
     * <tt>IceMediaStream</tt>.
     * @param stunAddresses An array of STUN server that could be used be the
     * <tt>Agent</tt>.
     * @param turnAddresses An array of TURN server that could be used be the
     * <tt>Agent</tt>.
     * @return An <tt>Agent</tt> having an <tt>IceMediaStream</tt>, with 2
     * <tt>Component</tt> (RTP and RTCP), for each name in <tt>mediaNameSet</tt>.
     *
     * @throws IOException if anything goes wrong when the <tt>Component<tt>
     * are created.
     */
    public void generateIceMediaStream (
            Agent agent,
            Set<String> mediaNameSet,
            TransportAddress stunAddresses[],
            TransportAddress turnAddresses[])
        throws IOException
    {
        agent.setControlling(false);

        IceMediaStream stream = null;

        if( stunAddresses != null )
        {
            for( TransportAddress stunAddress : stunAddresses )
            {
                agent.addCandidateHarvester(
                        new StunCandidateHarvester(stunAddress) );
            }
        }


        if( turnAddresses != null )
        {
            for( TransportAddress turnAddress : turnAddresses )
            {
                agent.addCandidateHarvester(
                        new TurnCandidateHarvester(turnAddress) );
            }
        }


        synchronized(this)
        {
            for(String name : mediaNameSet)
            {
                //FIXME if the stream is a data one, we don't create an IceMediaStream
                //(normally the data content should have been remove from the Set
                //But better safe than sorry
                if(name.equalsIgnoreCase("data")) continue;

                stream = agent.createMediaStream(name);

                if( (CURRENT_COMPONENT_PORT + 1) >= MAX_COMPONENT_PORT )
                    CURRENT_COMPONENT_PORT = MIN_COMPONENT_PORT;

                agent.createComponent(
                        stream,
                        Transport.UDP,
                        CURRENT_COMPONENT_PORT,
                        CURRENT_COMPONENT_PORT,
                        CURRENT_COMPONENT_PORT + 50);

                agent.createComponent(
                        stream,
                        Transport.UDP,
                        CURRENT_COMPONENT_PORT+1,
                        CURRENT_COMPONENT_PORT+1,
                        CURRENT_COMPONENT_PORT + 50);

                CURRENT_COMPONENT_PORT+=50;
            }
        }
    }

}
