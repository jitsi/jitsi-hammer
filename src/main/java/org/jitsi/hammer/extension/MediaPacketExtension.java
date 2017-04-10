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

package org.jitsi.hammer.extension;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.NewAbstractExtensionElement;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.NewSourcePacketExtension;
import org.jivesoftware.smack.packet.*;
import java.util.*;

/**
 * @author Thomas Kuntz
 * 
 * MediaPacketExtension is used by MediaProvider.
 * This class is used to store attributes of XML sub-documents
 * starting with "media" tag.
 */
public class MediaPacketExtension
        extends NewAbstractExtensionElement
{
    private static String ELEMENT_NAME = "media";
    /**
     * The namespace of this <tt>MediaPacketExtension</tt>
     */
    private static String NAMESPACE = MediaProvider.NAMESPACE;

    public MediaPacketExtension()
    {
        super(ELEMENT_NAME, NAMESPACE);
    }

    /**
     * Add a <tt>Source</tt> object to the <tt>sourceList</tt>
     * representing a <source /> line.
     * 
     * @arg type the type of the <tt>Source</tt>
     * @arg ssrc the ssrc of the <tt>Source</tt>
     * @arg direction the direction of the <tt>Source</tt>
     */
    public void addSource(String type, String ssrc, String direction)
    {
        addChildExtension(new NewSourcePacketExtension(type, ssrc, direction));
    }
}
