/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer.extension;

import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.provider.*;
import org.xmlpull.v1.*;

import java.io.*;


/**
 * 
 * @author Thomas Kuntz.
 * 
 * <tt>SsrcProvider</tt> is used to parse "ssrc" element in "description"
 * element of a Jingle IQ
 */
public class SsrcProvider implements PacketExtensionProvider
{
    /**
     * The name of the "ssrc" element. 
     */
    public final static String ELEMENT_NAME = "ssrc";
    /**
     * The namespace of the "ssrc" element.
     */
    public final static String NAMESPACE = "http://estos.de/ns/ssrc";

    
    /**
     * parseExtension normally parse a XML sub-document beginning with
     *  a "ssrc" tag and SsrcProvider.NAMESPACE as namespace.
     *  
     * This function parse the XML to create a SsrcPacketExtension 
     * object equivalent to the XML documents.
     * 
     * @param parser the <tt>XmlPullParser</tt> used to parse the xml
     * sub-document
     * @throws XmlPullParserException 
     * @throws IOException
     */
    public PacketExtension parseExtension(XmlPullParser parser)
        throws IOException, XmlPullParserException
    {
        
        SsrcPacketExtension packet = null;
        boolean done = false;

        /*
         * If the name of the tag and the namespace is correct,
         * we can move to the parsing section
        */
        if(     parser.getName().equals(SsrcProvider.ELEMENT_NAME)
            &&  parser.getNamespace().equals(SsrcProvider.NAMESPACE))
        {
            packet = new SsrcPacketExtension();
            
        
            while(done != true)
            {
                
                switch (parser.next())
                {
                    case XmlPullParser.END_TAG:
                        if(parser.getName().equals(SsrcProvider.ELEMENT_NAME))
                            done = true;
                        break;

                    case XmlPullParser.START_TAG:
                        if(parser.getName().equals(SsrcProvider.ELEMENT_NAME))
                        {
                            packet.setNamespace(
                                    parser.getNamespace());
                            packet.setCname(
                                    parser.getAttributeValue("","cname"));
                            packet.setMsid(
                                    parser.getAttributeValue("","msid"));
                            packet.setMslabel(
                                    parser.getAttributeValue("","mslabel"));
                            packet.setLabel(
                                    parser.getAttributeValue("","label"));
                            packet.setSsrc(
                                    parser.getAttributeValue("","ssrc"));
                        }
                        break;
                    default:
                        break;
                }
                
            }
        
        }
        /*
         * packet is null if the XML document parsed
         * don't begin with a media tag with NAMESPACE as namespace
         */
        return packet;
    }
}
