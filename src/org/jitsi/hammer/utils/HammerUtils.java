/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer.utils;

import javax.media.*;
import javax.media.format.*;

import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidateType;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.*;
import net.java.sip.communicator.service.protocol.media.*;

import org.jitsi.hammer.neomedia.jmfext.media.protocol.greyfading.*;
import org.jitsi.hammer.neomedia.jmfext.media.protocol.ivffile.*;
import org.jitsi.hammer.neomedia.jmfext.media.protocol.rtpdumpfile.RtpdumpMediaDevice;
import org.jitsi.hammer.extension.*;
import org.jitsi.service.libjitsi.*;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.device.*;
import org.jitsi.impl.neomedia.device.*;
import org.jitsi.service.neomedia.format.*;
import org.jitsi.service.neomedia.codec.*;
import org.ice4j.*;
import org.ice4j.ice.*;
import org.jitsi.videobridge.*;

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
     * Select the favorite <tt>MediaDevice</tt> for a given media type.
     * 
     * @param mediaType The type of the <tt>MediaDevice</tt> that you wish
     * to get.
     * 
     * @return the favorite <tt>MediaDevice</tt> for a given media type.
     */
    public static MediaDevice selectMediaDevice(String mediaType)
    {
        MediaDevice returnedDevice = null;
        
        
        switch(MediaType.parseString(mediaType))
        {
            case AUDIO:
                returnedDevice = new AudioSilenceMediaDevice();
                /*
                returnedDevice = new AudioMediaDeviceImpl(
                    new CaptureDeviceInfo2(
                        "rtpdump",
                        new MediaLocator("rtpdumpfile:./ressources/rtp_opus.rtpdump"),
                        new Format[]{ new AudioFormat(
                                Constants.OPUS_RTP,
                                48000,
                                Format.NOT_SPECIFIED, // sampleSizeInBits 
                                2,
                                Format.NOT_SPECIFIED, // endian 
                                Format.NOT_SPECIFIED, // signed
                                Format.NOT_SPECIFIED, // frameSizeInBits 
                                Format.NOT_SPECIFIED, // frameRate 
                                Format.byteArray) },
                        null,
                        null,
                        null));
                */
                break;
            case VIDEO:
                //returnedDevice = new VideoGreyFadingMediaDevice();
                /*
                returnedDevice = new MediaDeviceImpl(new CaptureDeviceInfo2(
                        "GreyFadingVideo",
                        new MediaLocator("greyfading:"),
                        null,null, null, null),
                        MediaType.VIDEO);
                */
                returnedDevice = new IVFMediaDevice("./ressources/big-buck-bunny_trailer_track1_eng.ivf");
                /*
                returnedDevice = new MediaDeviceImpl(new CaptureDeviceInfo2(
                        "Bunny",
                        new MediaLocator("ivffile:./ressources/big-buck-bunny_trailer_track1_eng.ivf"),
                        null, null, null, null),
                        MediaType.VIDEO);
                */
                //returnedDevice = new RtpdumpMediaDevice("./ressources/rtp_vp8.rtpdump", Constants.VP8_RTP);
                /*
                returnedDevice = new MediaDeviceImpl(new CaptureDeviceInfo2(
                        "rtpdump",
                        new MediaLocator("rtpdumpfile:./ressources/rtp_vp8.rtpdump"),
                        new Format[]{ new VideoFormat(Constants.VP8_RTP) }, null, null, null),
                        MediaType.VIDEO);
                */
                break;
            default :
                break;
        }
        
        return returnedDevice;
    }



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
                        candidate.setProtocol(localCandidate.getParentComponent().getTransport().toString());
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

    
    public static Map<String,MediaStream> generateMediaStream(
            Map<String, MediaFormat> mediaFormatMap,
            DynamicPayloadTypeRegistry ptRegistry)
    {
        MediaStream stream = null;
        MediaFormat format = null;
        MediaDevice device = null;
        Map<String,MediaStream> mediaStreamMap = new HashMap<String,MediaStream>();
        
        

        MediaService mediaService = LibJitsi.getMediaService();

        for(String mediaName : mediaFormatMap.keySet())
        {
            format = mediaFormatMap.get(mediaName);
            if(format == null) continue;
            
            
            stream = mediaService.createMediaStream(
                    null,
                    format.getMediaType(),
                    mediaService.createSrtpControl(SrtpControlType.DTLS_SRTP));
            
            device = selectMediaDevice(format.getMediaType().toString());
            if(device != null) stream.setDevice(device);
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
            
            
            //FIXME
            //I just add the dynamic payload type of RED (116) so that
            //the MediaStream don't complain when it will received RED packet
            //from the Jitsi Meet user
            if(format.getMediaType() == MediaType.VIDEO)
                stream.addDynamicRTPPayloadType(
                        (byte) 116,
                        format);
            
            
            mediaStreamMap.put(mediaName, stream);
        }
        return mediaStreamMap;
    }
    
    
    public static void addSocketToMediaStream(
            Agent agent,
            Map<String,MediaStream> mediaStreamMap)
    {
        IceMediaStream iceMediaStream = null;
        CandidatePair rtpPair = null;
        CandidatePair rtcpPair = null;
        DatagramSocket rtpSocket = null;
        DatagramSocket rtcpSocket = null;
        
        StreamConnector connector = null;
        MediaStream stream = null;
        
        for(String mediaName : mediaStreamMap.keySet())
        {
            iceMediaStream = agent.getStream(mediaName);
            stream = mediaStreamMap.get(mediaName);
            

            rtpPair = iceMediaStream.getComponent(Component.RTP).getSelectedPair();
            rtcpPair = iceMediaStream.getComponent(Component.RTCP).getSelectedPair();

            System.out.println(rtpPair);

            rtpSocket = rtpPair.getLocalCandidate().getDatagramSocket();
            rtcpSocket = rtcpPair.getLocalCandidate().getDatagramSocket();
            
            
            connector = new DefaultStreamConnector(rtpSocket, rtcpSocket);
            stream.setConnector(connector);
            
            stream.setTarget(
                    new MediaStreamTarget(
                        rtpPair.getRemoteCandidate().getTransportAddress(),
                        rtcpPair.getRemoteCandidate().getTransportAddress()) );
        }
    }
    
    
    
    
    

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
                    //fingerprint, so I'm not sure using a loop here is usefull
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
    
    public static void addSSRCToContent(
            Map<String,ContentPacketExtension> contentMap,
            Map<String,MediaStream> mediaStreamMap)
    {
        ContentPacketExtension content = null;
        RtpDescriptionPacketExtension description = null;
        MediaStream mediaStream = null;
        
        
        for(String mediaName : mediaStreamMap.keySet())
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
    public static void addSourceExtension(RtpDescriptionPacketExtension description,
                                    long ssrc)
    {
        MediaService mediaService = LibJitsi.getMediaService();
        String msLabel = UUID.randomUUID().toString();
        /*
         * IMPORTANT : The label need to be an unique ID for this stream.
         * At first, I copied part of Jitsi's code 
         */
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
