package org.jitsi.hammer;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidateType;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.*;

//import org.jitsi.impl.neomedia.transform.sdes.*;
import org.jitsi.impl.neomedia.*;
import org.jitsi.service.libjitsi.*;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.device.*;
import org.jitsi.service.neomedia.format.*;
import org.ice4j.*;
import org.ice4j.ice.*;
import org.jitsi.videobridge.*;

import java.net.*;
import java.util.*;

class JingleUtils {

    public static Map<String,SelectedMedia> generateAcceptedContentListFromSessionInitiateIQ(
        List<ContentPacketExtension> contentList, 
        JingleIQ jiq, 
        SendersEnum senders)
    {
        if(jiq.getAction() != JingleAction.SESSION_INITIATE) return null;

        Map<String,SelectedMedia> selectedMediaMap = new HashMap<String,SelectedMedia>();

        List<RtpDescriptionPacketExtension> rtpDescriptions = null;
        List<PayloadTypePacketExtension> payloadTypes = null;
        MediaDevice device = null;
        MediaFormat supportedFormat = null;
        String descriptionMediaName = null;
        
        ContentPacketExtension contentForSessionAccept = null;
        RtpDescriptionPacketExtension descriptionOfContentForSessionAccept = null;
        

        MediaService mediaService = LibJitsi.getMediaService();
        
        for(ContentPacketExtension content : jiq.getContentList())
        {
            contentForSessionAccept = createContentPacketExtension(content.getName(),CreatorEnum.responder, senders);
            contentList.add(contentForSessionAccept);
            
            rtpDescriptions = content.getChildExtensionsOfType(RtpDescriptionPacketExtension.class);
            for(RtpDescriptionPacketExtension rtpDescription : rtpDescriptions)
            {
                descriptionMediaName = rtpDescription.getMedia();
                descriptionOfContentForSessionAccept = new RtpDescriptionPacketExtension();
                descriptionOfContentForSessionAccept.setMedia(descriptionMediaName);
                contentForSessionAccept.addChildExtension(descriptionOfContentForSessionAccept);
                

                device = null;
                if(descriptionMediaName.equals("audio"))
                {
                    device = mediaService.getDefaultDevice(MediaType.AUDIO,MediaUseCase.CALL);
                    device = new AudioSilenceMediaDevice();
                    //device = mediaService.createMixer(device);
                }
                else if(descriptionMediaName.equals("video"))
                {
                    device = mediaService.getDefaultDevice(MediaType.VIDEO,MediaUseCase.CALL);
                    continue;
                }
                else
                {
                    return null;
                }
                if(device == null) return null;

                //We can add to the content of the accept IQ the accepted format (what we do here),
                //or we can add all the format we can handle (what we don't do here).
                payloadTypes = rtpDescription.getPayloadTypes();
                for(PayloadTypePacketExtension payloadType : payloadTypes)
                {
                    supportedFormat = getSupportedFormatFromPayloadType( device, payloadType );
                    if(supportedFormat != null)
                    {
                        //Yeah, add this format to accepted content (if one wasn't already in)
                        if(selectedMediaMap.containsKey(descriptionMediaName) != true)
                        {
                            selectedMediaMap.put(
                                    descriptionMediaName,
                                    new SelectedMedia(
                                            device,
                                            supportedFormat,
                                            (byte)payloadType.getID()));
                        }
                        
                        
                        descriptionOfContentForSessionAccept.addPayloadType(
                                createPayloadType(
                                        supportedFormat,
                                        (byte)payloadType.getID()));
                        break;
                    }
                }
            }
        }

        return selectedMediaMap;
    }


    public static ContentPacketExtension createContentPacketExtension(
            String contentName,
            CreatorEnum contentCreator,
            SendersEnum contentSenders)
    {
        ContentPacketExtension content = new ContentPacketExtension();

        content.setCreator(contentCreator);
        content.setName(contentName);
        content.setSenders(contentSenders);
    
        return content;
    }


    public static ContentPacketExtension createContentPacketExtension(
            String contentName,
            CreatorEnum contentCreator,
            SendersEnum contentSenders,
            MediaDevice device)
    {
        ContentPacketExtension content = new ContentPacketExtension();
        RtpDescriptionPacketExtension description = new RtpDescriptionPacketExtension();
        

        content.setCreator(contentCreator);
        content.setName(contentName);
        content.setSenders(contentSenders);
    
        content.addChildExtension(description);

        List<MediaFormat> mediaFormats = device.getSupportedFormats();
        description.setMedia(mediaFormats.get(0).getMediaType().toString());
        for (MediaFormat mediaFormat : mediaFormats)
        {
            description.addPayloadType(createPayloadType(mediaFormat));
        }

        return content;
    }


    public static ContentPacketExtension createContentPacketExtension(
            String contentName,
            CreatorEnum contentCreator,
            SendersEnum contentSenders,
            MediaFormat mediaFormat)
    {
        ContentPacketExtension content = new ContentPacketExtension();
        RtpDescriptionPacketExtension description = new RtpDescriptionPacketExtension();
        

        content.setCreator(contentCreator);
        content.setName(contentName);
        content.setSenders(contentSenders);
    
        content.addChildExtension(description);

        description.setMedia(mediaFormat.getMediaType().toString());
        description.addPayloadType(createPayloadType(mediaFormat));

        return content;
        
    }

    public static PayloadTypePacketExtension createPayloadType(MediaFormat mediaFormat)
    {
        PayloadTypePacketExtension payloadExtension = new PayloadTypePacketExtension();

        int rtpPayloadType = mediaFormat.getRTPPayloadType();

        payloadExtension.setId(rtpPayloadType);
        payloadExtension.setName(mediaFormat.getEncoding());
        payloadExtension.setClockrate((int)mediaFormat.getClockRate());

        //Audio format have a number of channel that we need to add to its payload extension
        if(mediaFormat instanceof AudioMediaFormat)
        {
            AudioMediaFormat audioMediaFormat = (AudioMediaFormat) mediaFormat;
            payloadExtension.setChannels(audioMediaFormat.getChannels());
        }

        //If the mediaFormat has parameter or advanced attributes, we have to add them too as payload parameter
        for(Map.Entry<String, String> paramEntry : mediaFormat.getFormatParameters().entrySet())
        {
            ParameterPacketExtension paramExtension = new ParameterPacketExtension();
            
            paramExtension.setName(paramEntry.getKey());
            paramExtension.setValue(paramEntry.getValue());
            
            payloadExtension.addParameter(paramExtension);
        }
        for(Map.Entry<String, String> attributEntry : mediaFormat.getAdvancedAttributes().entrySet())
        {
            ParameterPacketExtension paramExtension = new ParameterPacketExtension();

            paramExtension.setName(attributEntry.getKey());
            paramExtension.setValue(attributEntry.getValue());
            
            payloadExtension.addParameter(paramExtension);
        }

        return payloadExtension;
    }   
    
    
    public static PayloadTypePacketExtension createPayloadType( 
            MediaFormat mediaFormat,
            byte dynamicPayloadType )
    {
        PayloadTypePacketExtension payloadExtension = new PayloadTypePacketExtension();

        int rtpPayloadType = mediaFormat.getRTPPayloadType();
        if(rtpPayloadType == MediaFormat.RTP_PAYLOAD_TYPE_UNKNOWN)
        {
            rtpPayloadType = dynamicPayloadType;
        }

        payloadExtension.setId(rtpPayloadType);
        payloadExtension.setName(mediaFormat.getEncoding());
        payloadExtension.setClockrate((int)mediaFormat.getClockRate());

        //Audio format have a number of channel that we need to add to its payload extension
        if(mediaFormat instanceof AudioMediaFormat)
        {
            AudioMediaFormat audioMediaFormat = (AudioMediaFormat) mediaFormat;
            payloadExtension.setChannels(audioMediaFormat.getChannels());
        }

        //If the mediaFormat has parameter or advanced attributes, we have to add them too as payload parameter
        for(Map.Entry<String, String> paramEntry : mediaFormat.getFormatParameters().entrySet())
        {
            ParameterPacketExtension paramExtension = new ParameterPacketExtension();
            
            paramExtension.setName(paramEntry.getKey());
            paramExtension.setValue(paramEntry.getValue());
            
            payloadExtension.addParameter(paramExtension);
        }
        for(Map.Entry<String, String> attributEntry : mediaFormat.getAdvancedAttributes().entrySet())
        {
            ParameterPacketExtension paramExtension = new ParameterPacketExtension();

            paramExtension.setName(attributEntry.getKey());
            paramExtension.setValue(attributEntry.getValue());
            
            payloadExtension.addParameter(paramExtension);
        }

        return payloadExtension;
    }   



    private static MediaFormat getSupportedFormatFromPayloadType(
            MediaDevice device,
            PayloadTypePacketExtension payloadType)
    {
        if((device != null) && (payloadType != null))
        {
            for(MediaFormat mediaFormat : device.getSupportedFormats())
            {
                System.out.print(mediaFormat);
                System.out.println(" |||| " + payloadType.toXML());
                if((mediaFormat.getClockRateString().equals(String.valueOf(payloadType.getClockrate())))
                    && (mediaFormat.getEncoding().equals(payloadType.getName()))
                    && (
                            (mediaFormat.getRTPPayloadType() == MediaFormat.RTP_PAYLOAD_TYPE_UNKNOWN)
                            ||(mediaFormat.getRTPPayloadType() == payloadType.getID())
                       )
                    //These attribute are not available in a MediaFormat object (maybe with getAdditionnalCodecSetting or getAdvancedAttributes)
                    //&& (payloadType.getChannels())
                    //&& (payloadType.getMaxptime())
                    //&& (payloadType.getPTtime())
                  )
                {
                    return mediaFormat;
                }
            }
        }
            return null;
    }

    public static void addRemoteCandidateToAgent(
            Agent agent,
            List<ContentPacketExtension> contentList)
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
            List<ContentPacketExtension> contentList)
    {
        IceMediaStream iceMediaStream = null;
        IceUdpTransportPacketExtension transport = null;
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
                for(Component component : iceMediaStream.getComponents())
                {
                    for(LocalCandidate localCandidate : component.getLocalCandidates())
                    {
                        candidate = new CandidatePacketExtension();
                        
                        candidate.setFoundation(localCandidate.getFoundation());
                        candidate.setComponent(localCandidate.getParentComponent().getComponentID());
                        candidate.setProtocol(localCandidate.getParentComponent().getTransport().toString());
                        candidate.setPriority(localCandidate.getPriority());
                        candidate.setIP(localCandidate.getTransportAddress().getHostAddress());
                        candidate.setPort(localCandidate.getTransportAddress().getPort());
                        candidate.setType(CandidateType.valueOf(localCandidate.getType().toString()));
                        candidate.setGeneration(agent.getGeneration());
                        candidate.setNetwork(1);//FIXME How do I decide which network it is?
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

    public static List<MediaStream> generateMediaStreamFromAgent(
            Agent agent,
            Map<String,SelectedMedia> selectedMediaMap)
    {
        ArrayList<MediaStream> streamList = new ArrayList<MediaStream>();
        
        IceMediaStream iceMediaStream = null;
        CandidatePair rtpPair = null;
        CandidatePair rtcpPair = null;
        DatagramSocket rtpSocket = null;
        DatagramSocket rtcpSocket = null;
        
        SelectedMedia selectedMedia = null;
        StreamConnector connector = null;
        MediaStream stream = null;
        
        
        MediaService mediaService = LibJitsi.getMediaService();
        
        
        
        for(String mediaName : selectedMediaMap.keySet())
        {
            //System.out.println(mediaName);
            iceMediaStream = agent.getStream(mediaName);
            selectedMedia = selectedMediaMap.get(mediaName);

            rtpPair = iceMediaStream.getComponent(Component.RTP).getSelectedPair();
            rtcpPair = iceMediaStream.getComponent(Component.RTCP).getSelectedPair();

            System.out.println(rtpPair);

            rtpSocket = rtpPair.getLocalCandidate().getDatagramSocket();
            rtcpSocket = rtcpPair.getLocalCandidate().getDatagramSocket();
            
            
            connector = new DefaultStreamConnector(rtpSocket, rtcpSocket);
            stream = mediaService.createMediaStream(
                    connector,
                    selectedMedia.mediaFormat.getMediaType(),
                    mediaService.createSrtpControl(SrtpControlType.DTLS_SRTP));
            stream.setFormat(selectedMedia.mediaFormat);
            stream.setSSRCFactory(
                    new SSRCFactoryImpl((new Random()).nextInt() & 0xFFFFFFFFL));
            //XXX The pair is given in the StreamConnector constructor,
            //should I also give it to the stream?
            stream.setTarget(
                    new MediaStreamTarget(
                            rtpPair.getRemoteCandidate().getTransportAddress(),
                            rtcpPair.getRemoteCandidate().getTransportAddress()));
            stream.setName(mediaName);
            stream.setRTPTranslator(mediaService.createRTPTranslator());
            if(selectedMedia.mediaFormat.getRTPPayloadType()
               ==  MediaFormat.RTP_PAYLOAD_TYPE_UNKNOWN)
            {    
                stream.addDynamicRTPPayloadType(
                        selectedMedia.dynamicPayloadType,
                        selectedMedia.mediaFormat);
            }
            /*
            //FIXME Useless when the hammer will handle red correctly
            if(selectedMedia.mediaFormat.getMediaType() == MediaType.VIDEO)
                tream.addDynamicRTPPayloadType(
                        (byte) 116,
                        selectedMedia.mediaFormat);
            */
            stream.getSrtpControl().start(selectedMedia.mediaFormat.getMediaType());
            stream.setDevice(selectedMedia.mediaDevice);
            //stream.setDirection(MediaDirection.SENDRECV);
            
            streamList.add(stream);
        }
        
        return streamList;
    }
}
