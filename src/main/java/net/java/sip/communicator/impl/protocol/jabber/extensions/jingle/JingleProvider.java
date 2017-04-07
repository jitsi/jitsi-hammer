package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;
import org.jivesoftware.smack.packet.Element;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.IntrospectionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by bbaldino on 4/3/17.
 */
public class JingleProvider extends IntrospectionProvider.IQIntrospectionProvider<NewJingleIQ>
{

    public JingleProvider()
    {
        super(NewJingleIQ.class);
//        AbstractSmackInteroperabilityLayer smackInteroperabilityLayer = AbstractSmackInteroperabilityLayer.getInstance();
        //smackInteroperabilityLayer.addExtensionProvider("description", "urn:xmpp:jingle:apps:rtp:1", new NewAbstractExtensionElementProvider(NewRtpDescriptionPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("payload-type", "urn:xmpp:jingle:apps:rtp:1", new NewAbstractExtensionElementProvider(NewPayloadTypePacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("parameter", "urn:xmpp:jingle:apps:rtp:1", new NewAbstractExtensionElementProvider(ParameterPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("rtp-hdrext", "urn:xmpp:jingle:apps:rtp:rtp-hdrext:0", new NewAbstractExtensionElementProvider(NewRTPHdrExtPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("sctpmap", "urn:xmpp:jingle:transports:dtls-sctp:1", new NewSctpMapExtensionProvider());
//        smackInteroperabilityLayer.addExtensionProvider("encryption", "urn:xmpp:jingle:apps:rtp:1", new NewAbstractExtensionElementProvider(NewEncryptionPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("zrtp-hash", "urn:xmpp:jingle:apps:rtp:zrtp:1", new NewAbstractExtensionElementProvider(ZrtpHashPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("crypto", "urn:xmpp:jingle:apps:rtp:1", new NewAbstractExtensionElementProvider(NewCryptoPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("bundle", "http://estos.de/ns/bundle", new NewAbstractExtensionElementProvider(BundlePacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("group", "urn:xmpp:jingle:apps:grouping:0", new NewAbstractExtensionElementProvider(GroupPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("transport", "urn:xmpp:jingle:transports:ice-udp:1", new NewAbstractExtensionElementProvider(NewIceUdpTransportPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("transport", "urn:xmpp:jingle:transports:raw-udp:1", new NewAbstractExtensionElementProvider(NewRawUdpTransportPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("candidate", "urn:xmpp:jingle:transports:ice-udp:1", new NewAbstractExtensionElementProvider(NewCandidatePacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("candidate", "urn:xmpp:jingle:transports:raw-udp:1", new NewAbstractExtensionElementProvider(NewCandidatePacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("remote-candidate", "urn:xmpp:jingle:transports:ice-udp:1", new NewAbstractExtensionElementProvider(RemoteCandidatePacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("inputevt", "http://jitsi.org/protocol/inputevt", new NewAbstractExtensionElementProvider(InputEvtPacketExtension.class));
//        //smackInteroperabilityLayer.addExtensionProvider("conference-info", "", new NewAbstractExtensionElementProvider(CoinPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("fingerprint", "urn:xmpp:jingle:apps:dtls:0", new NewAbstractExtensionElementProvider(NewDtlsFingerprintPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("transfer", "urn:xmpp:jingle:transfer:0", new NewAbstractExtensionElementProvider(TransferPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("transferred", "urn:xmpp:jingle:transfer:0", new NewAbstractExtensionElementProvider(TransferredPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("callid", "http://jitsi.org/protocol/condesc", new NewAbstractExtensionElementProvider(CallIdPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("rtcp-fb", "urn:xmpp:jingle:apps:rtp:rtcp-fb:0", new NewAbstractExtensionElementProvider(NewRtcpFbPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("rtcp-mux", "urn:xmpp:jingle:transports:ice-udp:1", new NewAbstractExtensionElementProvider(NewRtcpmuxPacketExtension.class));
//        smackInteroperabilityLayer.addExtensionProvider("ssrc-info", "http://jitsi.org/jitmeet", new NewAbstractExtensionElementProvider(NewSSRCInfoPacketExtension.class));
    }

    @Override
    public NewJingleIQ parse(XmlPullParser parser, int initialDepth)
            throws XmlPullParserException, IOException
    {
        NewJingleIQ jingleIQ = new NewJingleIQ();
        JingleAction action = JingleAction.parseString(parser.getAttributeValue("", "action"));
        String initiator = parser.getAttributeValue("", "initiator");
        String responder = parser.getAttributeValue("", "responder");
        String sid = parser.getAttributeValue("", "sid");

        jingleIQ.setAction(action);
        jingleIQ.setInitiator(initiator);
        jingleIQ.setResponder(responder);
        jingleIQ.setSID(sid);
        boolean done = false;
        //NewAbstractExtensionElementProvider<NewContentPacketExtension> contentProvider =
        //        new NewAbstractExtensionElementProvider<NewContentPacketExtension>(NewContentPacketExtension.class);
        //ReasonProvider reasonProvider = new ReasonProvider();
//        NewAbstractExtensionElementProvider transferProvider = new NewAbstractExtensionElementProvider(TransferPacketExtension.class);
//        NewAbstractExtensionElementProvider coinProvider = new NewAbstractExtensionElementProvider(CoinPacketExtension.class);
//        NewAbstractExtensionElementProvider callidProvider = new NewAbstractExtensionElementProvider(CallIdPacketExtension.class);
//
        try
        {
            while (!done)
            {
                int eventType = parser.next();
                String elementName = parser.getName();
                String namespace = parser.getNamespace();
                if (eventType == 2)
                {
                    ExtensionElementProvider provider = ProviderManager.getExtensionProvider(elementName, namespace);
                    if (provider != null)
                    {
                        Element child = null;
                        try
                        {
                            child = provider.parse(parser);
                        }
                        catch (Exception e)
                        {
                            System.out.println("Got exception: " + e.toString());
                        }
                        if (child instanceof NewContentPacketExtension)
                        {
                            System.out.println("Got a content");
                            jingleIQ.addContent((NewContentPacketExtension)child);
                        }
                        System.out.println("done processing top level");



                    }
//                    if (elementName.equals("reason"))
//                    {
//                        ReasonPacketExtension type1 = reasonProvider.parseExtension(parser);
//                        jingleIQ.setReason(type1);
//                    }
//                    else if (elementName.equals("content"))
//                    {
//
//                        ContentPacketExtension type = (ContentPacketExtension) contentProvider.parse(parser);
//                         jingleIQ.addContent(type);
//
//                    }
//                    if (elementName.equals("content"))
//                    {
//                        NewContentPacketExtension type = (NewContentPacketExtension) contentProvider.parse(parser);
//                        jingleIQ.addContent(type);
//                    } else if (elementName.equals("reason"))
//                    {
//                        ReasonPacketExtension type1 = reasonProvider.parseExtension(parser);
//                        jingleIQ.setReason(type1);
//                    } else if (elementName.equals("transfer") && namespace.equals("urn:xmpp:jingle:transfer:0"))
//                    {
//                        //jingleIQ.addExtension(transferProvider.parse(parser));
//                    } else if (elementName.equals("conference-info"))
//                    {
//                        //jingleIQ.addExtension(coinProvider.parse(parser));
//                    } else if (elementName.equals("callid"))
//                    {
//                        //jingleIQ.addExtension(callidProvider.parse(parser));
//                    }
//
//                    if (namespace.equals("urn:xmpp:jingle:apps:rtp:info:1"))
//                    {
//                        SessionInfoType type2 = SessionInfoType.valueOf(elementName);
//                        if (type2 != SessionInfoType.mute && type2 != SessionInfoType.unmute)
//                        {
//                            jingleIQ.setSessionInfo(new SessionInfoPacketExtension(type2));
//                        } else
//                        {
//                            String name = parser.getAttributeValue("", "name");
//                            jingleIQ.setSessionInfo(new MuteSessionInfoPacketExtension(type2 == SessionInfoType.mute, name));
//                        }
//                    }
                }

                if (eventType == 3 && parser.getName().equals("jingle"))
                {
                    done = true;
                }
            }
        }
        catch (Exception e)
        {
        }

        return jingleIQ;
    }
}
