package org.jitsi.hammer;

import org.ice4j.ice.*;
import org.ice4j.*;
import org.ice4j.ice.harvest.*;


import java.util.*;

import java.io.*;



class IceMediaStreamGenerator {
    protected static IceMediaStreamGenerator generator = new IceMediaStreamGenerator();

    protected int MIN_COMPONENT_PORT = 6000;
    protected int MAX_COMPONENT_PORT = 9000;

    protected int CURRENT_COMPONENT_PORT = MIN_COMPONENT_PORT;

    public IceMediaStreamGenerator() {}
    public IceMediaStreamGenerator(int min_port, int max_port)
    {
        MIN_COMPONENT_PORT = min_port;
        MAX_COMPONENT_PORT = max_port;
        CURRENT_COMPONENT_PORT = min_port;
    }

    public static IceMediaStreamGenerator getGenerator()
    {
        return generator;
    }

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
                agent.addCandidateHarvester(new StunCandidateHarvester(stunAddress) );
            }
        }

        
        if( turnAddresses != null )
        {
            for( TransportAddress turnAddress : turnAddresses )
            {
                agent.addCandidateHarvester(new TurnCandidateHarvester(turnAddress) );
            }
        }
        

        synchronized(this)
        {
            for(String name : mediaNameSet)
            {
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
