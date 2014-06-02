package org.jitsi.hammer;

import org.ice4j.ice.*;
import org.jitsi.service.neomedia.*;
import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.muc.*;
import org.jivesoftware.smackx.packet.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.provider.*;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.*;

import java.io.*;
import java.util.*;

class JingleSession implements PacketListener {

    protected HostInfo serverInfo;
    protected String username;
    protected ConnectionConfiguration config;
    protected XMPPConnection connection;
    protected MultiUserChat muc;
    protected JingleIQ initiateSessionInfo;

    public JingleSession(HostInfo hostInfo)
    {
        this(hostInfo,null);
    }
    public JingleSession(HostInfo hostInfo,String username)
    {
        this.serverInfo = hostInfo;
        this.username = (username == null) ? "User" : username;

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


    public void start()
        throws XMPPException
    {
        connection.connect();
        connection.loginAnonymously();

        
        muc = new MultiUserChat(connection, serverInfo.getRoomName()+"@"+serverInfo.getMUC());
        muc.join(username);
        muc.sendMessage("Hello World!");
        
        Packet nicknamePacket = new Presence(Presence.Type.available);
        nicknamePacket.addExtension(new Nick(username));
        nicknamePacket.setTo(serverInfo.getRoomName()+"@"+serverInfo.getMUC());
        connection.sendPacket(nicknamePacket);
        
        muc.addMessageListener(new MyPacketListener(muc,serverInfo.getRoomName()+"@"+serverInfo.getMUC() +"/" + muc.getNickname()));
    }

    /*
     * acceptJingleSession create a accept-session Jingle message and send it to the initiator of the session.
     * The initiator is taken from the From attribut of the the initiate-session message.
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
     * Callback function used when a Jingle IQ is received by the XMPP connector.
     */
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
