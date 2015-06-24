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

import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidateType;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.*;
import net.java.sip.communicator.service.protocol.media.*;

import org.ice4j.socket.*;
import org.jitsi.hammer.extension.*;
import org.jitsi.service.libjitsi.*;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.device.*;
import org.jitsi.service.neomedia.format.*;
import org.jitsi.util.Logger;
import org.ice4j.*;
import org.ice4j.ice.*;

import java.net.*;
import java.util.*;


/**
 * The class contains a number of utility methods that are meant to facilitate
 * the handling of a Jingle session and the created ICE stream and media stream.
 *
 * @author Thomas Kuntz
 */
public class HammerUtils
{
    /**
     * The <tt>Logger</tt> used by the <tt>HammerUtils</tt> class and its
     * instances for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(HammerUtils.class);

    /**
     * Select the favorite <tt>MediaFormat</tt> of a list of <tt>MediaFormat</tt>
     *
     * @param mediaType The type of the <tt>MediaFormat</tt>
     * in <tt>mediaFormatList</tt>
     *
     * @param mediaFormatList a list of <tt>MediaFormat</tt>
     * (their <tt>MediaType</tt> should be the same as <tt>mediaType</tt>
     *
     *
     * @return the favorite <tt>MediaFormat</tt>
     * of a list of <tt>MediaFormat</tt>
     */
    public static MediaFormat selectFormat(
        String mediaType,
        List<MediaFormat> mediaFormatList)
    {
        MediaFormat returnedFormat = null;


        /*
         * returnedFormat take the value of the first element in the list,
         * so that if the favorite MediaFormat isn't found on the list,
         * then this function return the first MediaFormat of the list.
         *
         * For now, this function prefer opus for the audio format, and
         * vp8 for the video format
         */
        switch(MediaType.parseString(mediaType))
        {
        case AUDIO:
            for(MediaFormat fmt : mediaFormatList)
            {
                if(returnedFormat == null) returnedFormat = fmt;
                if(fmt.getEncoding().equalsIgnoreCase("opus"))
                {
                    returnedFormat = fmt;
                    break;
                }
            }
            break;

        case VIDEO:
            for(MediaFormat fmt : mediaFormatList)
            {
                if(returnedFormat == null) returnedFormat = fmt;
                if(fmt.getEncoding().equalsIgnoreCase("vp8"))
                {
                    returnedFormat = fmt;
                    break;
                }
            }
            break;
        default :
            break;
        }

        return returnedFormat;
    }


    /**
     * Add the remote transport candidates of each
     * <tt>ContentPacketExtension</tt> in <tt>contentList</tt> to their
     * associated <tt>IceMediaStream</tt> inside <tt>agent</tt>.
     *
     * @param agent the <tt>Agent</tt> containing the IceMediaStream to which
     * will be added the remote transport candidates.
     * @param contentList the list of <tt>ContentPacketExtension</tt> containing
     * the remote transport candidates to add to the <tt>Agent</tt>.
     */
    public static void addRemoteCandidateToAgent(
        Agent agent,
        Collection<ContentPacketExtension> contentList)
    {
        IceUdpTransportPacketExtension transports = null;
        List<CandidatePacketExtension> candidates = null;
        String contentName = null;
        IceMediaStream stream = null;
        Component component = null;

        RemoteCandidate relatedCandidate = null;
        TransportAddress mainAddr = null, relatedAddr = null;
        RemoteCandidate remoteCandidate;

        for(ContentPacketExtension content : contentList)
        {
            contentName = content.getName();
            stream = agent.getStream(contentName);
            if(stream == null) continue;

            transports = content.getFirstChildOfType(IceUdpTransportPacketExtension.class);
            if(transports == null)
                continue;

            stream.setRemotePassword(transports.getPassword());
            stream.setRemoteUfrag(transports.getUfrag());

            candidates = transports.getChildExtensionsOfType(CandidatePacketExtension.class);
            Collections.sort(candidates);

            for(CandidatePacketExtension candidate : candidates)
            {
                component = stream.getComponent(candidate.getComponent());

                if( (component != null)
                    && (candidate.getGeneration() == agent.getGeneration()))
                {
                    if((candidate.getIP() != null) && (candidate.getPort() > 0))
                    {

                        mainAddr = new TransportAddress(
                            candidate.getIP(),
                            candidate.getPort(),
                            Transport.parse(candidate.getProtocol().toLowerCase()));


                        relatedCandidate = null;
                        if( (candidate.getRelAddr() != null)
                            && (candidate.getRelPort() > 0))
                        {
                            relatedAddr = new TransportAddress(
                                candidate.getRelAddr(),
                                candidate.getRelPort(),
                                Transport.parse(candidate.getProtocol().toLowerCase()));
                            relatedCandidate = component.findRemoteCandidate(relatedAddr);
                        }

                        remoteCandidate = new RemoteCandidate(
                            mainAddr,
                            component,
                            org.ice4j.ice.CandidateType.parse(candidate.getType().toString()),
                            candidate.getFoundation(),
                            candidate.getPriority(),
                            relatedCandidate);

                        component.addRemoteCandidate(remoteCandidate);
                    }
                }
            }
        }
    }


    /**
     * Add the local transport candidates contained in <tt>agent</tt> to
     * their associated (by the stream/content name)
     * <tt>ContentPacketExtension</tt>.
     *
     * @param agent the <tt>Agent</tt> from which we will get the local
     * transport candidates.
     * @param contentList the list of <tt>ContentPacketExtension</tt> to which
     * will be added the local transport candidates.
     */
    public static void addLocalCandidateToContentList(
        Agent agent,
        Collection<ContentPacketExtension> contentList)
    {
        IceMediaStream iceMediaStream = null;
        IceUdpTransportPacketExtension transport = null;
        DtlsFingerprintPacketExtension fingerprint = null;
        CandidatePacketExtension candidate = null;
        long candidateID = 0;

        for(ContentPacketExtension content : contentList)
        {
            transport = new IceUdpTransportPacketExtension();

            iceMediaStream = agent.getStream(content.getName());

            transport.setPassword( agent.getLocalPassword() );
            transport.setUfrag( agent.getLocalUfrag() );

            if(iceMediaStream != null)
            {
                fingerprint = new DtlsFingerprintPacketExtension();

                fingerprint.setFingerprint("");
                fingerprint.setHash("");

                for(Component component : iceMediaStream.getComponents())
                {
                    for(LocalCandidate localCandidate : component.getLocalCandidates())
                    {
                        candidate = new CandidatePacketExtension();

                        candidate.setNamespace(IceUdpTransportPacketExtension.NAMESPACE);
                        candidate.setFoundation(localCandidate.getFoundation());
                        candidate.setComponent(localCandidate.getParentComponent().getComponentID());
                        candidate.setProtocol(localCandidate.getTransport().toString());
                        candidate.setPriority(localCandidate.getPriority());
                        candidate.setIP(localCandidate.getTransportAddress().getHostAddress());
                        candidate.setPort(localCandidate.getTransportAddress().getPort());
                        candidate.setType(CandidateType.valueOf(localCandidate.getType().toString()));
                        candidate.setGeneration(agent.getGeneration());
                        candidate.setNetwork(0);
                        candidate.setID(String.valueOf(candidateID++));
                        if( localCandidate.getRelatedAddress() != null )
                        {
                            candidate.setRelAddr(localCandidate.getRelatedAddress().getHostAddress());
                            candidate.setRelPort(localCandidate.getRelatedAddress().getPort());
                        }

                        transport.addCandidate(candidate);
                    }
                }
            }

            content.addChildExtension(transport);
        }
    }

    /**
     * Create a Map of <tt>MediaStream</tt> containing an AUDIO and VIDEO stream,
     * indexed by the String equivalent of their <tt>MediaType</tt> , with
     * just the <tt>MediaType</tt> set and with a <tt>DtlsControl</tt> for
     * <tt>SrtpControl</tt>. Anything else need to be set later
     *
     * @return a Map of newly created <tt>MediaStream</tt>, indexed by
     * the String equivalent of their <tt>MediaType</tt>*
     */
    public static Map<String,MediaStream> createMediaStreams()
    {
        MediaService mediaService = LibJitsi.getMediaService();
        Map<String,MediaStream> mediaStreamMap = new HashMap<String,MediaStream>();
        MediaStream stream = null;

        /*
         * AUDIO STREAM
         */
        stream = mediaService.createMediaStream(
            null,
            MediaType.AUDIO,
            mediaService.createSrtpControl(SrtpControlType.DTLS_SRTP));
        mediaStreamMap.put(MediaType.AUDIO.toString(), stream);

        /*
         * VIDEO STREAM
         */
        stream = mediaService.createMediaStream(
            null,
            MediaType.VIDEO,
            mediaService.createSrtpControl(SrtpControlType.DTLS_SRTP));
        mediaStreamMap.put(MediaType.VIDEO.toString(), stream);

        return mediaStreamMap;
    }


    /**
     * Configure the <tt>MediaStream</tt> contained in <tt>mediaStreamMap</tt>
     * with the informations the others arguments gives.
     *
     * It will set the streams with the <tt>MediaFormat</tt> associated to its
     * name/MediaType, and with the selected <tt>MediaDevice</tt> returned by
     * <tt>mediaDeviceChooser</tt> for the <tt>MediaType</tt> of the
     * <tt>MediaFormat</tt> of the stream.
     *
     * It will also create the streams with a <tt>DtlsControl</tt> that need
     * to be configured later.
     * The stream will be set to SENDONLY.
     *
     * @param mediaFormatMap a <tt>Map</tt> of <tt>MediaFormat</tt> indexed by
     * the name/<tt>MediaType</tt> of the MediaStreams set with this
     * <tt>MediaFormat</tt>.
     * @param mediaDeviceChooser used to chose the MediaDevice for each stream
     * @param ptRegistry the <tt>DynamicPayloadTypeRegistry</tt> containing
     * the dynamic payload type of the <tt>MediaFormat</tt> (if necessary).
     * @param rtpExtRegistry
     */
    public static void configureMediaStream(
        Map<String,MediaStream> mediaStreamMap,
        Map<String,MediaFormat> mediaFormatMap,
        Map<String,List<RTPExtension>> rtpExtensionMap,
        MediaDeviceChooser mediaDeviceChooser,
        DynamicPayloadTypeRegistry ptRegistry,
        DynamicRTPExtensionsRegistry rtpExtRegistry)
    {
        MediaStream stream = null;
        MediaFormat format = null;
        MediaDevice device = null;

        MediaService mediaService = LibJitsi.getMediaService();

        for(String mediaName : mediaFormatMap.keySet())
        {
            format = mediaFormatMap.get(mediaName);
            if(format == null)
                continue;

            stream = mediaStreamMap.get(mediaName);

            device = mediaDeviceChooser.getMediaDevice(format.getMediaType());
            if(device != null)
                stream.setDevice(device);

            stream.setFormat(format);

            stream.setName(mediaName);
            stream.setRTPTranslator(mediaService.createRTPTranslator());

            /* XXX if SENDRECV is set instead of SENDONLY or RECVONLY,
             * the audio stream will take 100% of a core of the CPU
             *
             * It also seems like if I remove the 2 function of the
             * AudioSilenceMediaDevice createPlayer and createSession, that
             * return null for the Player, the bug is also avoided : maybe
             * libjitsi doesn't handle correctly a null player..
             */
            stream.setDirection(MediaDirection.SENDONLY);

            if(format.getRTPPayloadType()
                ==  MediaFormat.RTP_PAYLOAD_TYPE_UNKNOWN)
            {
                stream.addDynamicRTPPayloadType(
                    ptRegistry.getPayloadType(format),
                    format);
            }

            /*
             * Add the rtp extension learned in the session-initial, and
             * supported by the MediaDevice of this MediaStream
             */
            for(RTPExtension rtpExtension : rtpExtensionMap.get(mediaName))
            {
                byte extensionID
                = rtpExtRegistry.getExtensionMapping(rtpExtension);
                stream.addRTPExtension(extensionID , rtpExtension);
            }


            //I just add the dynamic payload type of RED (116) so that
            //the MediaStream don't complain when it will received RED packet
            //from the Jitsi Meet user
            if(format.getMediaType() == MediaType.VIDEO)
                stream.addDynamicRTPPayloadType(
                    (byte) 116,
                    mediaService.getFormatFactory().createMediaFormat("red"));

            mediaStreamMap.put(mediaName, stream);
        }
    }


    /**
     * Add the <tt>DatagramSocket</tt> created by the IceMediaStreams of an
     * <tt>Agent</tt> (so after ICE was TERMINATED) to their associated
     * <tt>MediaStream</tt> contained in a <tt>Map</tt> and indexed by the
     * name of their associated IceMediaStream.
     *
     * @param agent the <tt>Agent</tt> containing the <tt>IceMediaStream</tt>
     * from which we will get the <tt>DatagramSocket</tt>
     * @param mediaStreamMap the <tt>Map</tt> of <tt>MediaStream</tt> to which
     * will be added the <tt>DatagramSocket</tt> of their corresponding
     * <tt>IceMediaStream</tt> contained in the <tt>Agent</tt>.
     */
    public static void addSocketToMediaStream(
        Agent agent,
        Map<String,MediaStream> mediaStreamMap,
        boolean dropIncomingRtpPackets)
    {
        IceMediaStream iceMediaStream = null;
        CandidatePair rtpPair = null;
        CandidatePair rtcpPair = null;
        DatagramSocket rtpSocket = null;
        DatagramSocket rtcpSocket = null;

        StreamConnector connector = null;
        MediaStream stream = null;

        String str = "Transport candidates selected for RTP:\n";
        for(String mediaName : agent.getStreamNames())
        {
            iceMediaStream = agent.getStream(mediaName);
            stream = mediaStreamMap.get(mediaName);

            rtpPair = iceMediaStream.getComponent(Component.RTP)
                .getSelectedPair();
            rtcpPair = iceMediaStream.getComponent(Component.RTCP)
                .getSelectedPair();

            str = str + "-" + mediaName + " stream :\n" + rtpPair + "\n";

            rtpSocket = rtpPair.getIceSocketWrapper().getUDPSocket();

            if (dropIncomingRtpPackets &&
                    rtpSocket instanceof MultiplexingDatagramSocket)
            {
                try
                {
                    // We are not going to handle any incoming RTP packets
                    // anyway, so we might as well drop them early and not
                    // waste resources processing them further.
                    // This sets up a filtered socket, which receives only
                    // DTLS packets.
                    rtpSocket
                        = ((MultiplexingDatagramSocket) rtpSocket)
                            .getSocket(new DTLSDatagramFilter());
                }
                catch (SocketException se)
                {
                    // Whatever, this is just an optimization, anyway.
                }
            }
            rtcpSocket = rtcpPair.getIceSocketWrapper().getUDPSocket();

            connector = new DefaultStreamConnector(rtpSocket, rtcpSocket);
            stream.setConnector(connector);

            stream.setTarget(
                new MediaStreamTarget(
                    rtpPair.getRemoteCandidate().getTransportAddress(),
                    rtcpPair.getRemoteCandidate().getTransportAddress()) );
        }
        logger.info(str);
    }



    /**
     * Add the remote fingerprint & hash function contained in
     * <tt>remoteContentList</tt> to the <tt>DtlsControl</tt> of the
     * <tt>MediaStream</tt>.
     * Add the local fingerprint & hash function from the <tt>DtlsControl</tt> of
     * the <tt>MediaStream</tt> to the <tt>localContentList</tt>.
     *
     * @param mediaStreamMap a Map containing the <tt>MediaStream</tt> to
     * which will be added the remote fingerprints, from which we will get
     * the local fingerprints.
     * @param localContentList The list of <tt>ContentPacketExtension</tt> to
     * which will be added the local fingerprints
     * @param remoteContentList The list of <tt>ContentPacketExtension</tt> from
     * which we will get the remote fingerprints
     */
    public static void setDtlsEncryptionOnTransport(
        Map<String,MediaStream> mediaStreamMap,
        List<ContentPacketExtension> localContentList,
        List<ContentPacketExtension> remoteContentList)
    {
        MediaStream stream = null;
        IceUdpTransportPacketExtension transport = null;
        List<DtlsFingerprintPacketExtension> fingerprints = null;
        SrtpControl srtpControl = null;
        DtlsControl dtlsControl = null;
        DtlsControl.Setup dtlsSetup = null;


        for(ContentPacketExtension remoteContent : remoteContentList)
        {
            transport = remoteContent.getFirstChildOfType(IceUdpTransportPacketExtension.class);
            dtlsSetup = null;

            stream = mediaStreamMap.get(remoteContent.getName());
            if(stream == null) continue;
            srtpControl = stream.getSrtpControl();
            if(srtpControl == null) continue;


            if( (srtpControl instanceof DtlsControl) && (transport != null) )
            {
                dtlsControl = (DtlsControl)srtpControl;

                fingerprints = transport.getChildExtensionsOfType(
                    DtlsFingerprintPacketExtension.class);

                if (!fingerprints.isEmpty())
                {
                    Map<String,String> remoteFingerprints
                    = new LinkedHashMap<String,String>();

                    //XXX videobridge send a session-initiate with only one
                    //fingerprint, so I'm not sure using a loop here is useful
                    for(DtlsFingerprintPacketExtension fingerprint : fingerprints)
                    {
                        remoteFingerprints.put(
                            fingerprint.getHash(),
                            fingerprint.getFingerprint());

                        //get the setup attribute of the fingerprint
                        //(the first setup found will be taken)
                        if(dtlsSetup == null)
                        {
                            String setup = fingerprint.getAttributeAsString("setup");
                            if(setup != null)
                            {
                                dtlsSetup = DtlsControl.Setup.parseSetup(setup);
                            }
                            else
                            {
                                // Default to ACTPASS (and not ACTIVE, as
                                // RFC4145 defines), because this is what is
                                // expected in a jitsi-videobridge + (jicofo or
                                // jitsi-meet) environment.
                                dtlsSetup = DtlsControl.Setup.ACTPASS;
                            }
                        }
                    }


                    dtlsControl.setRemoteFingerprints(remoteFingerprints);
                    dtlsSetup = getDtlsSetupForAnswer(dtlsSetup);
                    dtlsControl.setSetup(dtlsSetup);
                }
            }
        }


        //This code add the fingerprint of the local MediaStream to the content
        //that will be sent with the session-accept
        for(ContentPacketExtension localContent : localContentList)
        {
            transport = localContent.getFirstChildOfType(
                IceUdpTransportPacketExtension.class);

            stream = mediaStreamMap.get(localContent.getName());
            if(stream == null) continue;
            srtpControl = stream.getSrtpControl();

            if( (srtpControl instanceof DtlsControl) && (transport != null))
            {
                DtlsFingerprintPacketExtension fingerprint =
                    new DtlsFingerprintPacketExtension();
                dtlsControl = (DtlsControl) srtpControl;


                fingerprint.setHash(dtlsControl.getLocalFingerprintHashFunction());
                fingerprint.setFingerprint(dtlsControl.getLocalFingerprint());
                fingerprint.setAttribute("setup", dtlsSetup);

                transport.addChildExtension(fingerprint);
            }
        }
    }

    /**
     * Get a correct DTLS Setup SDP attribute for the local DTLS engine from
     * the Setup offered by the remote target.
     * @param setup The DTLS Setup offered by the remote target.
     * @return a correct DTLS Setup SDP attribute for the local DTLS engine from
     * the Setup offered by the remote target.
     */
    public static DtlsControl.Setup getDtlsSetupForAnswer(DtlsControl.Setup setup)
    {
        DtlsControl.Setup returnedSetup = null;
        if(setup != null)
        {
            if(setup.equals(DtlsControl.Setup.ACTPASS))
                returnedSetup = DtlsControl.Setup.ACTIVE;
            else if(setup.equals(DtlsControl.Setup.PASSIVE))
                returnedSetup = DtlsControl.Setup.ACTIVE;
            else if(setup.equals(DtlsControl.Setup.ACTIVE))
                returnedSetup = DtlsControl.Setup.PASSIVE;
            else if(setup.equals(DtlsControl.Setup.HOLDCONN))
                returnedSetup = DtlsControl.Setup.HOLDCONN;
        }
        return returnedSetup;
    }

    /**
     * Set the ssrc attribute of each <tt>MediaStream</tt> to their corresponding
     * <tt>RtpDescriptionPacketExtension</tt>, and also add a 'source' element
     * to it, describing the msid,mslabel,label and cname of the stream.
     *
     * @param contentMap the Map of <tt>ContentPacketExtension</tt> to which
     * will be set the ssrc and addec the "source" element.
     * @param mediaStreamMap the Map of <tt>MediaStream</tt> from which will be
     * gotten the ssrc and other informations.
     */
    public static void addSSRCToContent(
        Map<String,ContentPacketExtension> contentMap,
        Map<String,MediaStream> mediaStreamMap)
    {
        ContentPacketExtension content = null;
        RtpDescriptionPacketExtension description = null;
        MediaStream mediaStream = null;


        for(String mediaName : contentMap.keySet())
        {
            long ssrc;

            content = contentMap.get(mediaName);
            mediaStream = mediaStreamMap.get(mediaName);
            if((content == null) || (mediaStream == null)) continue;

            ssrc = mediaStream.getLocalSourceID();

            description = content.getFirstChildOfType(
                RtpDescriptionPacketExtension.class);

            description.setSsrc(String.valueOf(ssrc));
            addSourceExtension(description, ssrc);
        }
    }



    /**
     * Adds a <tt>SourcePacketExtension</tt> as a child element of
     * <tt>description</tt>. See XEP-0339.
     *
     * @param description the <tt>RtpDescriptionPacketExtension</tt> to which
     * a child element will be added.
     * @param ssrc the SSRC for the <tt>SourcePacketExtension</tt> to use.
     */
    public static void addSourceExtension(
        RtpDescriptionPacketExtension description,
        long ssrc)
    {
        MediaService mediaService = LibJitsi.getMediaService();
        String msLabel = UUID.randomUUID().toString();
        String label = UUID.randomUUID().toString();

        SourcePacketExtension sourcePacketExtension =
            new SourcePacketExtension();
        SsrcPacketExtension ssrcPacketExtension =
            new SsrcPacketExtension();


        sourcePacketExtension.setSSRC(ssrc);
        sourcePacketExtension.addChildExtension(
            new ParameterPacketExtension("cname",
                mediaService.getRtpCname()));
        sourcePacketExtension.addChildExtension(
            new ParameterPacketExtension("msid", msLabel + " " + label));
        sourcePacketExtension.addChildExtension(
            new ParameterPacketExtension("mslabel", msLabel));
        sourcePacketExtension.addChildExtension(
            new ParameterPacketExtension("label", label));
        description.addChildExtension(sourcePacketExtension);



        ssrcPacketExtension.setSsrc(String.valueOf(ssrc));
        ssrcPacketExtension.setCname(mediaService.getRtpCname());
        ssrcPacketExtension.setMsid(msLabel + " " + label);
        ssrcPacketExtension.setMslabel(msLabel);
        ssrcPacketExtension.setLabel(label);
        description.addChildExtension(ssrcPacketExtension);
    }

    /**
     * Create a relatively empty <tt>ContentPacketExtension</tt> for 'data'
     * (<tt>MediaType.DATA</tt>) rtp content type, because
     * <tt>JingleUtils.createDescription</tt> doesn't handle this type for now.
     *
     * @param creator indicates whether the person who originally created this
     * content was the initiator or the responder of the jingle session.
     * @param senders indicates the direction of the media in this stream.
     * @return a <tt>ContentPacketExtension</tt> for 'data' content.
     */
    public static ContentPacketExtension createDescriptionForDataContent(
        CreatorEnum                  creator,
        SendersEnum                  senders)
    {
        ContentPacketExtension content = new ContentPacketExtension();
        RtpDescriptionPacketExtension description
        = new RtpDescriptionPacketExtension();


        content.setCreator(creator);
        content.setName("data");

        //senders - only if we have them and if they are different from default
        if(senders != null && senders != SendersEnum.both)
            content.setSenders(senders);

        description.setMedia("data");
        //RTP description
        content.addChildExtension(description);

        return content;
    }
}
