/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
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
 * This class is used to generate <tt>IceMediaStream</tt> without overlapping
 * the port numbers used for each.
 * 
 * This class propose a static instance of itself (with default value for min
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
     * 
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
    public Agent generateIceMediaStream (
            Set<String> mediaNameSet,
            TransportAddress stunAddresses[],
            TransportAddress turnAddresses[])
        throws IOException
    {
        Agent agent = new Agent();
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

        return agent;
    }

}
