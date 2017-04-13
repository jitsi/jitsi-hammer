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

package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import org.jivesoftware.smack.packet.Element;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.IntrospectionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * A provider for <tt>NewJingleIQ</tt> instances
 *
 * @author Brian Baldino
 */
public class JingleProvider extends IntrospectionProvider.IQIntrospectionProvider<NewJingleIQ>
{

    public JingleProvider()
    {
        super(NewJingleIQ.class);
    }

    @Override
    public NewJingleIQ parse(XmlPullParser parser, int initialDepth)
            throws XmlPullParserException, IOException
    {
        NewJingleIQ jingleIQ = new NewJingleIQ();
        NewJingleAction action = NewJingleAction.parseString(parser.getAttributeValue("", "action"));
        String initiator = parser.getAttributeValue("", "initiator");
        String responder = parser.getAttributeValue("", "responder");
        String sid = parser.getAttributeValue("", "sid");

        jingleIQ.setAction(action);
        jingleIQ.setInitiator(initiator);
        jingleIQ.setResponder(responder);
        jingleIQ.setSID(sid);
        boolean done = false;

        try
        {
            while (!done)
            {
                int eventType = parser.next();
                String elementName = parser.getName();
                String namespace = parser.getNamespace();
                if (eventType == XmlPullParser.START_TAG)
                {
                    ExtensionElementProvider provider = ProviderManager.getExtensionProvider(elementName, namespace);
                    if (provider != null)
                    {
                        Element child = provider.parse(parser);
                        if (child instanceof NewContentPacketExtension)
                        {
                            jingleIQ.addContent((NewContentPacketExtension)child);
                        }
                        else
                        {
                            throw new IOException("JingleProvider doesn't handle child element " + elementName +
                                " in namespace " + namespace);
                        }
                    }
                    else
                    {
                        throw new IOException("JingleProvider: no provider found for element " +
                                elementName + " in namespace " + namespace);
                    }
                }

                if (eventType == XmlPullParser.END_TAG && parser.getName().equals("jingle"))
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
