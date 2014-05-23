package org.jitsi.hammer;

import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.muc.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.provider.ProviderManager;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;
import java.util.ArrayList;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.SendersEnum;
import org.ice4j.ice.Agent;
import org.ice4j.ice.IceProcessingState;
import java.util.Map;
import java.util.List;

import org.jitsi.service.libjitsi.*;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.device.*;
import org.jitsi.service.neomedia.format.*;

import java.io.IOException;

/*
 * Hammer is the class that 
 * 	setup the connection to the XMPP server.
 * 	add a MediaProvider to correctly parse <media></media> XML documents received from a jitmeet session (I'm not sure it's usefull).
 * 	add a JingleIQProvider to parse <jingle></jingle> XML documents received from a jitmeet session (necessary to setup the connection with jitsi-videobridge).
 * 	Enable/Disable smack debugger
 * 	connect and login anonymously to the XMPP server
 * 	connect to the muc given in HostInfo.
 */
public class Hammer {
	protected HostInfo serverInfo;
	protected ConnectionConfiguration config;
	protected XMPPConnection connection;
	protected MultiUserChat muc;
	protected JingleIQ initiateSessionInfo;

	public Hammer(HostInfo host) throws XMPPException
	{
		serverInfo = host;

		ProviderManager.getInstance().addExtensionProvider(MediaProvider.ELEMENT_NAME,MediaProvider.NAMESPACE, new MediaProvider());
		ProviderManager.getInstance().addIQProvider(JingleIQ.ELEMENT_NAME,JingleIQ.NAMESPACE,new JingleIQProvider());

		config = new ConnectionConfiguration(serverInfo.getMUC(),5222,serverInfo.getDomain());
		connection = new XMPPConnection(config);
		connection.addPacketListener(new JingleListener(this),new JingleFilter());
		
		System.out.println();
		config.setDebuggerEnabled(true);
		//Connection.DEBUG_ENABLED = false;
		


		connection.connect();
		connection.loginAnonymously();

		muc = new MultiUserChat(connection, serverInfo.getRoomName()+"@"+serverInfo.getMUC());
		muc.join("JitMeet-Hammer");
		muc.sendMessage("Hello World!");
		muc.changeNickname("JitMeet-Hammer");
		muc.addMessageListener(new MyPacketListener(muc,serverInfo.getRoomName()+"@"+serverInfo.getMUC() +"/" + muc.getNickname()));
	}

	protected void setJingleInfo(JingleIQ info)
	{
		this.initiateSessionInfo = info;
	}



	/*
	 * Send a simple ACK message for a Jingle message (an IQ result message).
	 */
	protected void ackJingleIQ(JingleIQ packetToAck)
	{
		IQ ackPacket = IQ.createResultIQ(packetToAck);
		connection.sendPacket(ackPacket);
		System.out.println("Ack sent for JingleIQ");
	}



	/*
	 * acceptJingleSession create a accept-session Jingle message and send it to the initiator of the session.
	 * The initiator is taken from the From attribut of the the initiate-session message.
	 *
	 * For now, the accept-session message doesn't contains any <content/> childs
	 */
	protected void acceptJingleSession()
	{
		ArrayList<ContentPacketExtension> contentList = null;
		Map<String,SelectedMedia> selectedMedias = null;
		List<MediaStream> mediaStreamList = null;
		IceMediaStreamGenerator iceMediaStramGenerator = IceMediaStreamGenerator.getGenerator();
		Agent agent = null;

		// Now is the code section where we generate the content list for the accept-session
		////////////////////////////////////
		
		//For now, we just generate an empty list (no <content/> childs will be added to the message)
		contentList = new ArrayList<ContentPacketExtension>();
		
		selectedMedias = JingleUtils.generateAcceptedContentListFromSessionInitiateIQ(contentList,initiateSessionInfo,SendersEnum.both);
		try
		{
			agent = iceMediaStramGenerator.generateIceMediaStream(selectedMedias.keySet(),null,null);
		}
		catch (IOException e)
		{
			System.err.println(e);
		}

		JingleUtils.addRemoteCandidateToAgent(agent,initiateSessionInfo.getContentList());
		JingleUtils.addLocalCandidateToContentList(agent,contentList);

		agent.startConnectivityEstablishment();

		////////////////////////////////////
		// End of the code section generating the content list of the accept-session

		//check establishment connectivity Agent + start stream
		//TODO

		//Creation of a session-accept message and its sending
		JingleIQ accept = JinglePacketFactory.createSessionAccept(
				initiateSessionInfo.getTo(),
				initiateSessionInfo.getFrom(),
				initiateSessionInfo.getSID(),
				contentList);
		connection.sendPacket(accept);
		System.out.println("Jingle accept-session message sent");

		while(IceProcessingState.TERMINATED != agent.getState())
		{
			System.out.println("Connectivity Establishment in process");
			try
			{
				Thread.sleep(1500);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		mediaStreamList = JingleUtils.generateMediaStreamFromAgent(agent,selectedMedias);
		for(MediaStream stream : mediaStreamList)
		{
			stream.start();
		}
	}

	/*
	 * A packet listener that only process Jingle packet
	 * When a Jingle packet is received, this class set the JingleIQ to the Hammer class in hammerParent.
	 * It also print a message in the terminal when a JingleIQ is received.
	 */
	private class JingleListener implements PacketListener
	{
		protected Hammer hammerParent;

		public JingleListener(Hammer parent)
		{
			this.hammerParent = parent;
		}

		public void processPacket(Packet packet)
		{
			if(packet instanceof JingleIQ)
			{
				JingleIQ p = (JingleIQ)packet;
				hammerParent.setJingleInfo(p);
				System.out.println("Jingle initiate-session message received");
				ackJingleIQ(p);
				if(p.getAction() == JingleAction.SESSION_INITIATE) acceptJingleSession();
			}
		}
	}


	/*
	 * A simple PacketFilter that only accept JingleIQ packet
	 */
	private class JingleFilter implements PacketFilter
	{
		public boolean accept(Packet packet)
		{
			return (packet instanceof JingleIQ);
		}
	}
}

