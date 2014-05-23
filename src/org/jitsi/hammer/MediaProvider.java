package org.jitsi.hammer;

import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.provider.*;
import org.xmlpull.v1.*;

/*
 * MediaProvider is used to correctly parse <media> XML document (with MediaProvider.NAMESPACE as namespace).
 * It especially correctly parses <source /> lines
 */
public class MediaProvider implements PacketExtensionProvider
{
	public final static String ELEMENT_NAME = "media";
	public final static String NAMESPACE = "http://estos.de/ns/mjs";

	/*
	 * parseExtension normally parse a XML sub-document beginning with a <media> tag and MediaProvider.NAMESPACE as namespace.
	 * This function parse the XML to create a MediaPacketExtension object equivalent to the XML documents.
	 */
	public PacketExtension parseExtension(XmlPullParser parser) throws Exception
	{
		MediaPacketExtension packet = null;
		boolean done = false;

		//If the name of the tag and the namespace is correct, we can move to the parsing section
		if(parser.getName().equals(MediaProvider.ELEMENT_NAME) && parser.getNamespace().equals(MediaProvider.NAMESPACE))
		{
			packet = new MediaPacketExtension();
			packet.setNamespace("http://estos.de/ns/mjs");
		
			while(done != true)
			{
				
				switch (parser.next())
				{
					case XmlPullParser.END_TAG:
						//If the tag is </media>, then this functions is done parsing
						if(parser.getName().equals(MediaProvider.ELEMENT_NAME)) done = true;
						break;

					case XmlPullParser.START_TAG:
						//If we read a <source /> line, we can add a Source to "packet", the MediaPacketExtension returned by this function
						if(parser.getName().equals("source"))
						{
							packet.addSource(parser.getAttributeValue("","type"),parser.getAttributeValue("","ssrc"),parser.getAttributeValue("","direction"));
						}
						break;
					default:
						break;
				}
				
			}
		
		}
		//packet is null if the XML document parsed don't begin with a media tag with NAMESPACE as namespace
		return packet;
	}
}
