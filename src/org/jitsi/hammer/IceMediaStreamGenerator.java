package org.jitsi.hammer;

import org.ice4j.ice.Agent;
import org.ice4j.ice.IceMediaStream;
import org.ice4j.ice.Component;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.ice.harvest.StunCandidateHarvester;
import org.ice4j.ice.harvest.TurnCandidateHarvester;

import java.util.Map;
import java.util.Set;

import java.io.IOException;



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
			TransportAddress turnAddresses[]) throws IOException
	{
		Agent agent = new Agent();
		agent.setControlling(false && true);

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
				
				if( (CURRENT_COMPONENT_PORT + 1) >= MAX_COMPONENT_PORT ) CURRENT_COMPONENT_PORT = MIN_COMPONENT_PORT;

				agent.createComponent(stream, Transport.UDP, CURRENT_COMPONENT_PORT, CURRENT_COMPONENT_PORT, CURRENT_COMPONENT_PORT + 49);
				agent.createComponent(stream, Transport.UDP, CURRENT_COMPONENT_PORT+1, CURRENT_COMPONENT_PORT+1, CURRENT_COMPONENT_PORT + 50);
	
				CURRENT_COMPONENT_PORT+=50;
			}
		}

		return agent;
	}

}
