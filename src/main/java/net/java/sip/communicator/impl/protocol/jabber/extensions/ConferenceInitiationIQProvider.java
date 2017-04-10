package net.java.sip.communicator.impl.protocol.jabber.extensions;

import org.jivesoftware.smack.provider.IntrospectionProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by bbaldino on 4/7/17.
 */
public class ConferenceInitiationIQProvider
        extends IntrospectionProvider.IQIntrospectionProvider<ConferenceInitiationIQ>
{
    public ConferenceInitiationIQProvider()
    {
        super(ConferenceInitiationIQ.class);
    }

    @Override
    public ConferenceInitiationIQ parse(XmlPullParser parser, int initialDepth)
            throws XmlPullParserException, IOException
    {
        ConferenceInitiationIQ confIq = new ConferenceInitiationIQ();

        int attrCount = parser.getAttributeCount();
        for (int i = 0; i < attrCount; ++i)
        {
            String attrName = parser.getAttributeName(i);
            String attrValue = parser.getAttributeValue(i);
        }

        //String roomUrl = parser.getAttributeValue("", ConferenceInitiationIQ.ROOM_ATTR_NAME);

        return confIq;
    }
}
