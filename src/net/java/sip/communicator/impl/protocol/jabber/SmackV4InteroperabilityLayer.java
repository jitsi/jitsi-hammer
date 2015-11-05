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
package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.service.protocol.jabber.*;
import org.jivesoftware.smack.provider.*;

/**
 * Smack4 interoperability layer
 * 
 * @author Maksym Kulish
 */
public class SmackV4InteroperabilityLayer
        extends AbstractSmackInteroperabilityLayer {

    /**
     * Add <tt>PacketExtensionProvider</tt> to the list of known
     * providers
     *
     * @param elementName The element name where the matching is happening
     * @param namespace The XML namespace used in that element
     * @param provider <tt>PacketExtensionProvider</tt> implementation to be 
     *                 used
     */
    @Override
    public void addExtensionProvider(
            String elementName, String namespace, Object provider)
    {
        ProviderManager.addExtensionProvider(elementName, namespace, provider);
    }

    /**
     * Add <tt>IQProvider</tt> to the list of known
     * providers
     *
     * @param elementName The element name where the matching is happening
     * @param namespace The XML namespace used in that element
     * @param provider <tt>IQProvider</tt> implementation to be 
     *                 used
     */
    @Override
    public void
    addIQProvider(String elementName, String namespace, Object provider)
    {
        ProviderManager.addIQProvider(elementName, namespace, provider);
    }

    /**
     * Get the <tt>PacketExtensionProvider</tt> for given element name and XML 
     * namespace
     *
     * @param elementName The element name where the matching is happening
     * @param namespace The XML namespace used in that element
     * @return <tt>PacketExtensionProvider</tt> implementation to be 
     *                 used
     */
    @Override
    public PacketExtensionProvider getExtensionProvider(
            String elementName, String namespace)
    {
        return (PacketExtensionProvider)
                ProviderManager.getExtensionProvider(elementName, namespace);
    }
    
}
