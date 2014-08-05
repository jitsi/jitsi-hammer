/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer;


import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.*;
import org.jivesoftware.smackx.packet.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.filter.*;
import org.ice4j.ice.*;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.format.*;
import org.jitsi.hammer.stats.*;
import org.jitsi.hammer.utils.*;
import org.jitsi.hammer.extension.*;

import net.java.sip.communicator.impl.protocol.jabber.jinglesdp.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.*;
import net.java.sip.communicator.service.protocol.media.*;

import java.io.*;
import java.util.*;


/**
 *
 * @author Thomas Kuntz
 *
 * <tt>FakeUser</tt> represent a Jingle,ICE and RTP/RTCP session with
 * jitsi-videobridge : it simulate a jitmeet user by setting up an
 * ICE stream and then sending fake audio/video data using RTP
 * to the videobridge.
 *
 */
public class FakeUser implements PacketListener {
    /**
     * The XMPP server info to which this <tt>FakeUser</tt> will
     * communicate
     */
    private HostInfo serverInfo;

    /**
     * The <tt>MediaDeviceChooser</tt> that will be used to choose the
     * <tt>MediaDevice</tt>s of this <tt>FakeUser</tt>
     */
    private MediaDeviceChooser mediaDeviceChooser;

    /**
     * The nickname/nickname taken by this <tt>FakeUser</tt> in the
     * MUC chatroom
     */
    private String nickname;


    /**
     * The <tt>ConnectionConfiguration</tt> equivalent of <tt>serverInfo</tt>.
     */
    private ConnectionConfiguration config;

    /**
     * The object use to connect to and then communicate with the XMPP server.
     */
    private XMPPConnection connection;

    /**
     * The object use to connect to and then send message to the MUC chatroom.
     */
    private MultiUserChat muc;


    /**
     * The registry containing the dynamic payload types learned in the
     * session-initiate (to use back in the session-accept)
     */
    DynamicPayloadTypeRegistry ptRegistry =
        new DynamicPayloadTypeRegistry();

    /**
     * The registry containing the dynamic RTP extensions learned in the
     * session-initiate
     */
    DynamicRTPExtensionsRegistry rtpExtRegistry =
        new DynamicRTPExtensionsRegistry();

    /**
     * A Map mapping a media type (audio, video, data), with a list of format
     * that can be handle by libjitsi
     */
    Map<String,List<MediaFormat>> possibleFormatMap =
        new HashMap<String,List<MediaFormat>>();

    /**
     * A Map mapping a media type (audio, video, data), with a <tt>MediaFormat</tt>
     * representing the selected format for the stream handling this media type.
     *
     * The MediaFormat in this Map has been chosen in <tt>possibleFormatMap</tt>
     */
    Map<String,MediaFormat> selectedFormat =
        new HashMap<String,MediaFormat>();

    /**
     * A Map mapping a media type (audio, video, data), with a list of
     * RTPExtension representing the selected RTP extensions for the format
     * (and its corresponding <tt>MediaDevice</tt>)
     */
    Map<String,List<RTPExtension>> selectedRtpExtension =
        new HashMap<String,List<RTPExtension>>();

    /**
     * The IQ message received by the XMPP server to initiate the Jingle session.
     *
     * It contains a list of <tt>ContentPacketExtension</tt> representing
     * the media and their formats the videobridge is offering to send/receive
     * and their corresponding transport information (IP, port, etc...).
     */
    private JingleIQ sessionInitiate;

    /**
     * The IQ message send by this <tt>FakeUser</tt> to the XMPP server
     * to accept the Jingle session.
     *
     * It contains a list of <tt>ContentPacketExtension</tt> representing
     * the media and format, with their corresponding transport information,
     * that this <tt>FakeUser</tt> accept to receive and send.
     */
    private JingleIQ sessionAccept;

    /**
     * A Map of the different <tt>MediaStream</tt> this <tt>FakeUser</tt>
     * handles.
     */
    private Map<String,MediaStream> mediaStreamMap;

    /**
     * The <tt>Agent</tt> handling the ICE protocol of the stream
     */
    private Agent agent;

    /**
     * <tt>Presence</tt> packet containing the SSRC of the streams of this
     * <tt>FakeUser</tt> (ns = http://estos.de/ns/mjs).
     *
     * The packet is saved in these variable because it can be send multiple
     * times if needed (to copy Jitsi Meet behavior), but now it's only send
     * once (during the jingle accept).
     */
    private Packet presencePacketWithSSRC;

    /**
     * The <tt>FakeUserStats</tt> that represents the stats of the streams of
     * this <tt>FakeUser</tt>
     */
    FakeUserStats fakeUserStats = new FakeUserStats();



    /**
     * Instantiates a <tt>FakeUser</tt> with a default nickname that
     * will connect to the XMPP server contained in <tt>hostInfo</tt>.
     *
     * @param hostInfo the XMPP server informations needed for the connection.
     * @param mdc The <tt>MediaDeviceChooser</tt> that will be used by this
     * <tt>FakeUser</tt> to choose the <tt>MediaDevice</tt> for each of its
     * <tt>MediaStream</tt>s.
     * @param hammerStats The <tt>HammerStat</tt> to which this
     * <tt>FakeUser</tt> will register its <tt>MediaStream</tt>s for their
     * stats.
     */
    public FakeUser(
        HostInfo hostInfo,
        MediaDeviceChooser mdc)
    {
        this(hostInfo, mdc, null);
    }

    /**
     * Instantiates a <tt>FakeUser</tt> with a specified <tt>nickname</tt>
     * that will connect to the XMPP server contained in <tt>hostInfo</tt>.
     *
     * @param hostInfo the XMPP server informations needed for the connection.
     * @param mdc The <tt>MediaDeviceChooser</tt> that will be used by this
     * <tt>FakeUser</tt> to choose the <tt>MediaDevice</tt> for each of its
     * <tt>MediaStream</tt>s.
     * @param hammerStats The <tt>HammerStat</tt> to which this
     * <tt>FakeUser</tt> will register its <tt>MediaStream</tt>s for their
     * stats.
     * @param nickname the nickname used by this <tt>FakeUser</tt> in the
     * connection.
     *
     */
    public FakeUser(
        HostInfo hostInfo,
        MediaDeviceChooser mdc,
        String nickname)
    {
        this(hostInfo, mdc, nickname, false);
    }

    /**
     * Instantiates a <tt>FakeUser</tt> with a specified <tt>nickname</tt>
     * that will connect to the XMPP server contained in <tt>hostInfo</tt>.
     *
     * @param hostInfo the XMPP server informations needed for the connection.
     * @param mdc The <tt>MediaDeviceChooser</tt> that will be used by this
     * <tt>FakeUser</tt> to choose the <tt>MediaDevice</tt> for each of its
     * <tt>MediaStream</tt>s.
     * @param nickname the nickname used by this <tt>FakeUser</tt> in the
     * connection.
     * @param hammerStats The <tt>HammerStat</tt> to which this
     * <tt>FakeUser</tt> will register its <tt>MediaStream</tt>s for their
     * stats.
     * @param smackDebug the boolean activating or not the debug screen of smack
     */
    public FakeUser(
        HostInfo hostInfo,
        MediaDeviceChooser mdc,
        String nickname,
        boolean smackDebug)
    {
        this.serverInfo = hostInfo;
        this.mediaDeviceChooser = mdc;
        this.nickname = (nickname == null) ? "Anonymous" : nickname;

        config = new ConnectionConfiguration(
            serverInfo.getXMPPHostname(),
            serverInfo.getPort(),
            serverInfo.getXMPPDomain());
        config.setDebuggerEnabled(smackDebug);

        connection = new XMPPConnection(config);
        connection.addPacketListener(this,new PacketFilter()
        {
            public boolean accept(Packet packet)
            {
                return (packet instanceof JingleIQ);
            }
        });

        /*
         * Creation in advance of the MediaStream that will be used later
         * so the HammerStats can register their MediaStreamStats now.
         */
        mediaStreamMap = HammerUtils.createMediaStreams();
        fakeUserStats.setMediaStreamStats(
            mediaStreamMap.get(MediaType.AUDIO.toString()));
        fakeUserStats.setMediaStreamStats(
            mediaStreamMap.get(MediaType.VIDEO.toString()));
        fakeUserStats.setUsername(this.nickname);


        ServiceDiscoveryManager discoManager =
            ServiceDiscoveryManager.getInstanceFor(connection);
        discoManager.addFeature(JingleIQ.NAMESPACE);
        discoManager.addFeature(RtpDescriptionPacketExtension.NAMESPACE);
        discoManager.addFeature(RawUdpTransportPacketExtension.NAMESPACE);
        discoManager.addFeature(IceUdpTransportPacketExtension.NAMESPACE);
        discoManager.addFeature(DtlsFingerprintPacketExtension.NAMESPACE);
        discoManager.addFeature(RTPHdrExtPacketExtension.NAMESPACE);
        discoManager.addFeature("urn:xmpp:jingle:apps:rtp:audio");
        discoManager.addFeature("urn:xmpp:jingle:apps:rtp:video");
    }

    /**
     * Connect to the XMPP server, login anonymously then join the MUC chatroom.
     * @throws XMPPException if the connection to the XMPP server goes wrong
     */
    public void start() throws XMPPException
    {
        connection.connect();
        connection.loginAnonymously();

        connectMUC();
    }

    /**
     * Connect to the XMPP server, login with the username and password given
     * then join the MUC chatroom.
     * @throws XMPPException if the connection to the XMPP server goes wrong
     */
    public void start(String username,String password) throws XMPPException
    {
        connection.connect();
        connection.login(username,password);

        connectMUC();
    }

    /**
     * Join the MUC, send a presence packet to display the current nickname
     * @throws XMPPException if the connection to the MUC goes wrong
     */
    private void connectMUC() throws XMPPException
    {
        String roomURL = serverInfo.getRoomName()+"@"+serverInfo.getMUCDomain();
        muc = new MultiUserChat(connection, roomURL);
        while(true)
        {
            try
            {
                muc.join(nickname);

                muc.sendMessage("Hello World!");

                /*
                 * Send a Presence packet containing a Nick extension so that the
                 * nickname is correctly displayed in jitmeet
                 */
                Packet presencePacket = new Presence(Presence.Type.available);
                presencePacket.setTo(roomURL + "/" + nickname);
                presencePacket.addExtension(new Nick(nickname));
                connection.sendPacket(presencePacket);
            }
            catch (XMPPException e)
            {
                /*
                 * IF the nickname is already taken in the MUC (code 409)
                 * then we append '_' to the nickname, and retry
                 */
                if((e.getXMPPError() != null) && (e.getXMPPError().getCode() == 409))
                {
                    nickname=nickname+'_';
                    continue;
                }
                else
                {
                    e.printStackTrace();
                }
            }
            break;
        }
    }

    /**
     * Stop and close all media stream
     * and disconnect from the MUC and the XMPP server
     */
    public void stop()
    {
        agent.free();
        for(MediaStream stream : mediaStreamMap.values())
        {
            stream.close();
        }

        connection.sendPacket(
            JinglePacketFactory.createSessionTerminate(
                sessionAccept.getFrom(),
                sessionAccept.getTo(),
                sessionAccept.getSID(),
                Reason.GONE,
                "Bye Bye"));

        muc.leave();
        connection.disconnect();
    }


    /**
     * acceptJingleSession create a accept-session Jingle message and
     * send it to the initiator of the session.
     * The initiator is taken from the From attribute
     * of the initiate-session message.
     */
    private void acceptJingleSession()
    {
        IceMediaStreamGenerator iceMediaStreamGenerator = null;
        List<MediaFormat> listFormat = null;
        List<RTPExtension> remoteRtpExtension = null;
        List<RTPExtension> supportedRtpExtension = null;
        List<RTPExtension> listRtpExtension = null;
        ContentPacketExtension content = null;
        RtpDescriptionPacketExtension description = null;
        Map<String,ContentPacketExtension> contentMap =
            new HashMap<String,ContentPacketExtension>();


        for(ContentPacketExtension cpe : sessionInitiate.getContentList())
        {
            //data isn't correctly handle by libjitsi for now, so we handle it
            //differently than the other MediaType
            if(cpe.getName().equalsIgnoreCase("data"))
            {
                content = HammerUtils.createDescriptionForDataContent(
                    CreatorEnum.responder,
                    SendersEnum.both);
            }
            else
            {
                description = cpe.getFirstChildOfType(
                    RtpDescriptionPacketExtension.class);


                listFormat = JingleUtils.extractFormats(
                    description,
                    ptRegistry);
                remoteRtpExtension = JingleUtils.extractRTPExtensions(
                    description,
                    rtpExtRegistry);
                supportedRtpExtension = getExtensionsForType(
                    MediaType.parseString(cpe.getName()));
                listRtpExtension = intersectRTPExtensions(
                    remoteRtpExtension,
                    supportedRtpExtension);


                possibleFormatMap.put(
                    cpe.getName(),
                    listFormat);

                selectedFormat.put(
                    cpe.getName(),
                    HammerUtils.selectFormat(cpe.getName(),listFormat));

                selectedRtpExtension.put(
                    cpe.getName(),
                    listRtpExtension);


                content = JingleUtils.createDescription(
                    CreatorEnum.responder,
                    cpe.getName(),
                    SendersEnum.both,
                    listFormat,
                    listRtpExtension,
                    ptRegistry,
                    rtpExtRegistry);
            }

            contentMap.put(cpe.getName(),content);
        }
        /*
         * We remove the content for the data (because data is not handle
         * for now by libjitsi)
         * FIXME
         */
        contentMap.remove("data");



        iceMediaStreamGenerator = IceMediaStreamGenerator.getInstance();
        try
        {
            agent = iceMediaStreamGenerator.generateIceMediaStream(
                contentMap.keySet(),
                null,
                null);
        }
        catch (IOException e)
        {
            System.err.println(e);
        }

        //Add the remote candidate to my agent, and add my local candidate of
        //my stream to the content list of the future session-accept
        HammerUtils.addRemoteCandidateToAgent(
            agent,
            sessionInitiate.getContentList());
        HammerUtils.addLocalCandidateToContentList(
            agent,
            contentMap.values());




        /*
         * configure the MediaStream created in the constructor with the
         * selected MediaFormat, and with the selected MediaDevice (through the
         * MediaDeviceChooser.
         */
        HammerUtils.configureMediaStream(
            mediaStreamMap,
            selectedFormat,
            selectedRtpExtension,
            mediaDeviceChooser,
            ptRegistry,
            rtpExtRegistry);

        //Now that the MediaStream are created, I can add their SSRC to the
        //content list of the future session-accept
        HammerUtils.addSSRCToContent(contentMap, mediaStreamMap);


        /*
         * Send the SSRC of the different media in a "media" tag
         * It's not necessary but its a copy of Jitsi Meet behavior
         *
         * Also, without sending this packet, there are error logged
         *  in the javascript console of the Jitsi Meet initiator :
         * "No video type for ssrc: 13365845"
         * It seems like Jitsi Meet can work arround this error,
         * but better safe than sorry.
         */
        presencePacketWithSSRC = new Presence(Presence.Type.available);
        String recipient =
            serverInfo.getRoomName()
            +"@"
            +serverInfo.getMUCDomain()
            + "/"
            + nickname;
        presencePacketWithSSRC.setTo(recipient);
        presencePacketWithSSRC.addExtension(new Nick(this.nickname));
        MediaPacketExtension mediaPacket = new MediaPacketExtension();
        for(String key : mediaStreamMap.keySet())
        {
            String str = String.valueOf(mediaStreamMap.get(key).getLocalSourceID());
            mediaPacket.addSource(
                key,
                str,
                MediaDirection.SENDRECV.toString());
        }
        presencePacketWithSSRC.addExtension(mediaPacket);
        connection.sendPacket(presencePacketWithSSRC);




        //Creation of a session-accept message
        sessionAccept = JinglePacketFactory.createSessionAccept(
            sessionInitiate.getTo(),
            sessionInitiate.getFrom(),
            sessionInitiate.getSID(),
            contentMap.values());
        sessionAccept.setInitiator(sessionInitiate.getFrom());


        //Set the remote fingerprint on my streams and add the fingerprints
        //of my streams to the content list of the session-accept
        HammerUtils.setDtlsEncryptionOnTransport(
            mediaStreamMap,
            sessionAccept.getContentList(),
            sessionInitiate.getContentList());


        //Send the session-accept IQ
        connection.sendPacket(sessionAccept);
        System.out.println("Jingle accept-session message sent");

        //Run ICE protocol on my streams.
        agent.startConnectivityEstablishment();
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



        //Add socket created by ice4j to their associated MediaStreams
        HammerUtils.addSocketToMediaStream(agent, mediaStreamMap);


        //Start the encryption of the MediaStreams
        for(MediaStream stream : mediaStreamMap.values())
        {
            SrtpControl control = stream.getSrtpControl();
            MediaType type = stream.getFormat().getMediaType();
            control.start(type);
        }

        //Start the MediaStream
        for(MediaStream stream : mediaStreamMap.values())
        {
            stream.start();
        }
    }



    /**
     * Callback function used when a JingleIQ is received by the XMPP connector.
     * @param packet the packet received by the <tt>FakeUser</tt>
     */
    public void processPacket(Packet packet)
    {
        JingleIQ jiq = (JingleIQ)packet;
        ackJingleIQ(jiq);
        switch(jiq.getAction())
        {
        case SESSION_INITIATE:
            System.out.println("Jingle session-initiate received");
            if(sessionInitiate == null)
            {
                sessionInitiate = jiq;
                acceptJingleSession();
            }
            else
            {
                System.out.println("but not processed (already got one)");
            }
            break;
        case ADDSOURCE:
            System.out.println("Jingle addsource received");
            break;
        case REMOVESOURCE:
            System.out.println("Jingle addsource received");
            break;
        default:
            System.out.println("Unknown Jingle IQ");
            break;
        }
    }


    /**
     * This function simply create an ACK packet to acknowledge the Jingle IQ
     * packet <tt>packetToAck</tt>.
     * @param packetToAck the <tt>JingleIQ</tt> that need to be acknowledge.
     */
    private void ackJingleIQ(JingleIQ packetToAck)
    {
        IQ ackPacket = IQ.createResultIQ(packetToAck);
        connection.sendPacket(ackPacket);
        System.out.println("Ack sent for JingleIQ");
    }


    /**
     * Copy from CallPeerMediaHandler class of Jitsi
     *
     * Returns a (possibly empty) <tt>List</tt> of <tt>RTPExtension</tt>s
     * supported by the device that this <tt>FakeUser</tt> uses to
     * handle media of the specified <tt>type</tt>.
     *
     * @param type the <tt>MediaType</tt> of the device whose
     * <tt>RTPExtension</tt>s we are interested in.
     *
     * @return a (possibly empty) <tt>List</tt> of <tt>RTPExtension</tt>s
     * supported by the device that this <tt>FakeUser</tt>
     * uses to handle media of the specified <tt>type</tt>.
     */
    protected List<RTPExtension> getExtensionsForType(MediaType type)
    {
        return mediaDeviceChooser.getMediaDevice(type).getSupportedExtensions();
    }


    /**
     * Copy from CallPeerMediaHandler class of Jitsi
     *
     * Compares a list of <tt>RTPExtension</tt>s offered by a remote party
     * to the list of locally supported <tt>RTPExtension</tt>s as returned
     * by one of our local <tt>MediaDevice</tt>s and returns a third
     * <tt>List</tt> that contains their intersection. The returned
     * <tt>List</tt> contains extensions supported by both the remote party and
     * the local device that we are dealing with. Direction attributes of both
     * lists are also intersected and the returned <tt>RTPExtension</tt>s have
     * directions valid from a local perspective. In other words, if
     * <tt>remoteExtensions</tt> contains an extension that the remote party
     * supports in a <tt>SENDONLY</tt> mode, and we support that extension in a
     * <tt>SENDRECV</tt> mode, the corresponding entry in the returned list will
     * have a <tt>RECVONLY</tt> direction.
     *
     * @param remoteExtensions the <tt>List</tt> of <tt>RTPExtension</tt>s as
     * advertised by the remote party.
     * @param supportedExtensions the <tt>List</tt> of <tt>RTPExtension</tt>s
     * that a local <tt>MediaDevice</tt> returned as supported.
     *
     * @return the (possibly empty) intersection of both of the extensions lists
     * in a form that can be used for generating an SDP media description or
     * for configuring a stream.
     */
    protected List<RTPExtension> intersectRTPExtensions(
        List<RTPExtension> remoteExtensions,
        List<RTPExtension> supportedExtensions)
        {
        if(remoteExtensions == null || supportedExtensions == null)
            return new ArrayList<RTPExtension>();

        List<RTPExtension> intersection = new ArrayList<RTPExtension>(
            Math.min(remoteExtensions.size(), supportedExtensions.size()));

        //loop through the list that the remote party sent
        for(RTPExtension remoteExtension : remoteExtensions)
        {
            RTPExtension localExtension = findExtension(
                supportedExtensions, remoteExtension.getURI().toString());

            if(localExtension == null)
                continue;

            MediaDirection localDir  = localExtension.getDirection();
            MediaDirection remoteDir = remoteExtension.getDirection();

            RTPExtension intersected = new RTPExtension(
                localExtension.getURI(),
                localDir.getDirectionForAnswer(remoteDir),
                remoteExtension.getExtensionAttributes());

            intersection.add(intersected);
        }

        return intersection;
        }

    /**
     * Copy from CallPeerMediaHandler class of Jitsi
     *
     * Returns the first <tt>RTPExtension</tt> in <tt>extList</tt> that uses
     * the specified <tt>extensionURN</tt> or <tt>null</tt> if <tt>extList</tt>
     * did not contain such an extension.
     *
     * @param extList the <tt>List</tt> that we will be looking through.
     * @param extensionURN the URN of the <tt>RTPExtension</tt> that we are
     * looking for.
     *
     * @return the first <tt>RTPExtension</tt> in <tt>extList</tt> that uses
     * the specified <tt>extensionURN</tt> or <tt>null</tt> if <tt>extList</tt>
     * did not contain such an extension.
     */
    private RTPExtension findExtension(List<RTPExtension> extList,
        String extensionURN)
    {
        for(RTPExtension rtpExt : extList)
            if (rtpExt.getURI().toASCIIString().equals(extensionURN))
                return rtpExt;
        return null;
    }

    /**
     * Returns a <tt>FakeUserStats</tt> object used to get statistics about this
     * <tt>FakeUser</tt>.
     * @return the <tt>FakeUserStats</tt> object used to get statistics about
     * this <tt>FakeUser</tt>.
     */
    public FakeUserStats getFakeUserStats()
    {
        return this.fakeUserStats;
    }
}
