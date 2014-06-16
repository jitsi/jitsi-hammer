/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer.utils;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidateType;
import net.java.sip.communicator.service.protocol.media.DynamicPayloadTypeRegistry;

import org.jitsi.hammer.device.*;
import org.jitsi.service.libjitsi.*;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.device.*;
import org.jitsi.service.neomedia.format.*;
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
public class HammerUtils {
    
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
                break;
            case VIDEO:
                returnedDevice = new VideoGreyFadingMediaDevice();
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
            
            if(stream != null)
            {

                transports = content.getFirstChildOfType(IceUdpTransportPacketExtension.class);
            
                stream.setRemotePassword(transports.getPassword());
                stream.setRemoteUfrag(transports.getUfrag());
            
                candidates = transports.getChildExtensionsOfType(CandidatePacketExtension.class);
                Collections.sort(candidates);
            
                for(CandidatePacketExtension candidate : candidates)
                {
                    component = stream.getComponent(candidate.getComponent());
                
                    if((component != null) && (candidate.getGeneration() == agent.getGeneration()))
                    {
                        if((candidate.getIP() != null) && (candidate.getPort() > 0))
                        {

                            mainAddr = new TransportAddress(candidate.getIP(), candidate.getPort(), Transport.parse(candidate.getProtocol().toLowerCase()));
                    
                    
                            relatedCandidate = null;
                            if ((candidate.getRelAddr() != null) && (candidate.getRelPort() > 0))
                            {
                                relatedAddr = new TransportAddress(candidate.getRelAddr(), candidate.getRelPort(), Transport.parse(candidate.getProtocol().toLowerCase()));
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
            
            
            stream = mediaService.createMediaStream(
                    null,
                    format.getMediaType(),
                    mediaService.createSrtpControl(SrtpControlType.DTLS_SRTP));
            
            device = selectMediaDevice(format.getMediaType().toString());
            if(device != null) stream.setDevice(device);
            stream.setFormat(format);
            
            stream.setName(mediaName);
            stream.setRTPTranslator(mediaService.createRTPTranslator());
            
            if(format.getRTPPayloadType()
               ==  MediaFormat.RTP_PAYLOAD_TYPE_UNKNOWN)
            {    
                stream.addDynamicRTPPayloadType(
                        ptRegistry.getPayloadType(format),
                        format);
            }
            
            
            //FIXME
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
                        //(the first encounter setup will be taken)
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
                    dtlsControl.setSetup(getDtlsSetupForAnswer(dtlsSetup));
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
}
