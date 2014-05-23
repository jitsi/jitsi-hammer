package org.jitsi.hammer;

import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.muc.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.provider.ProviderManager;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.SendersEnum;

import java.util.ArrayList;

class JingleSession implements PacketListener {

	protected HostInfo serverInfo;
	protected String username;
	protected ConnectionConfiguration config;
	protected XMPPConnection connection;
	protected MultiUserChat muc;
	protected JingleIQ initiateSessionInfo;

	public JingleSession(HostInfo hostInfo,String username)
	{
		this.serverInfo = hostInfo;
		this.username = (username == null) ? "JitMeet-Hammer" : username;

		ProviderManager.getInstance().addExtensionProvider(MediaProvider.ELEMENT_NAME,MediaProvider.NAMESPACE, new MediaProvider());
		ProviderManager.getInstance().addIQProvider(JingleIQ.ELEMENT_NAME,JingleIQ.NAMESPACE,new JingleIQProvider());

		config = new ConnectionConfiguration(serverInfo.getMUC(),serverInfo.getPort(),serverInfo.getDomain());
		connection = new XMPPConnection(config);
		connection.addPacketListener(this,new PacketFilter()
			{
				public boolean accept(Packet packet)
				{
					return (packet instanceof JingleIQ);
				}
			});
		
		config.setDebuggerEnabled(true);
		//Connection.DEBUG_ENABLED = false;
	}


	public void start() throws XMPPException
	{
		connection.connect();
		connection.loginAnonymously();

		muc = new MultiUserChat(connection, serverInfo.getRoomName()+"@"+serverInfo.getMUC());
		muc.join(username);
		muc.sendMessage("Hello World!");
		muc.changeNickname(username);
		muc.addMessageListener(new MyPacketListener(muc,serverInfo.getRoomName()+"@"+serverInfo.getMUC() +"/" + muc.getNickname()));
	}

	protected void acceptJingleSession()
	{
	
	}

	public void processPacket(Packet packet)
	{
		JingleIQ jiq = (JingleIQ)packet;
		System.out.println("Jingle initiate-session message received");
		ackJingleIQ(jiq);
		switch(jiq.getAction())
		{
			case SESSION_INITIATE:
				initiateSessionInfo = jiq;
				acceptJingleSession();
				break;
			default:
				System.out.println("Unknown Jingle IQ");
				break;
		}
	}

	protected void ackJingleIQ(JingleIQ packetToAck)
	{
		IQ ackPacket = IQ.createResultIQ(packetToAck);
		connection.sendPacket(ackPacket);
		System.out.println("Ack sent for JingleIQ");
	}
}
