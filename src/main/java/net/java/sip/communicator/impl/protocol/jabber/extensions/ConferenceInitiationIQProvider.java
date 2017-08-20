/*
 * Copyright @ 2017 Atlassian Pty Ltd
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

package net.java.sip.communicator.impl.protocol.jabber.extensions;

import org.jivesoftware.smack.provider.IntrospectionProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * A provider for <tt>ConferenceInitiationIQ</tt> elements.
 *
 * Note(brian): currently this doesn't do any actual parsing.  It was written for the hammer, which
 * doesn't currently do anything with the <tt>ConferenceInitiationIQ</tt>, but smack was having issues
 * if there wasn't a provider for it.
 *
 * @author Brian Baldino
 * FIXME: this class is duplicated from Jicofo
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
        return new ConferenceInitiationIQ();
    }
}
