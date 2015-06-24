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

import org.jivesoftware.smack.packet.*;
import java.util.*;

/**
 * @author Thomas Kuntz
 * 
 * MediaPacketExtension is used by MediaProvider.
 * This class is used to store attributes of XML sub-documents
 * starting with "media" tag.
 */
public class MediaPacketExtension implements PacketExtension
{
    /**
     * The namespace of this <tt>MediaPacketExtension</tt>
     */
    private String namespace = MediaProvider.NAMESPACE;
    
    /**
     * The list of <tt>Source</tt> that are childs of this media element
     */
    private List<Source> sourceList = new ArrayList<Source>();

    /**
     * Get the element name of the <tt>MediaPacketExtension</tt>.
     * @return the element name of the <tt>MediaPacketExtension</tt>.
     */
    public String getElementName()
    {
        return MediaProvider.ELEMENT_NAME;
    }
    
    /**
     * Get the namespace of the <tt>MediaPacketExtension</tt>.
     * @return the namespace of the <tt>MediaPacketExtension</tt>.
     */
    public String getNamespace()
    {
        return namespace;
    }
    
    /**
     * Set the namespace of the <tt>MediaPacketExtension</tt>.
     * @param ns the namespace to be set of the <tt>MediaPacketExtension</tt>.
     */
    public void setNamespace(String ns)
    {
        namespace = ns;
    }

    /**
     * toXML returns XML document equivalent
     * to the informations this class carries.
     * 
     * @return XML document in a String equivalent
     * to the informations this class carries.
     */
    public String toXML()
    {
        StringBuilder bldr = new StringBuilder();
        bldr.append("<media xmlns=\""+getNamespace()+"\">");
        for(Source src : sourceList)
        {
            bldr.append("<source ");
            bldr.append("type=\""+src.getType()+"\" ");
            bldr.append("ssrc=\""+src.getSsrc()+"\" ");
            bldr.append("direction=\""+src.getDirection()+"\" ");
            bldr.append(" />");
        }
        bldr.append("</media>");
        return bldr.toString();
    }

    
    /**
     * Add a <tt>Source</tt> object to the <tt>sourceList</tt>
     * representing a <source /> line.
     * 
     * @arg type the type of the <tt>Source</tt>
     * @arg ssrc the ssrc of the <tt>Source</tt>
     * @arg direction the direction of the <tt>Source</tt>
     */
    public void addSource(String type,String ssrc,String direction)
    {
        sourceList.add(new Source(type,ssrc,direction));
    }

    
    
    /**
     * Source represent a <source /> single-line child of a "media" xml sub-document
     * It implements attributes that a <source /> tag is expected to have.
     */
    public class Source
    {
        /**
         * The type attribute of the <tt>Source</tt>.
         */
        private String type;
        
        /**
         * The ssrc attribute of the <tt>Source</tt>.
         */
        private String ssrc;
        
        /**
         * The direction attribute of the <tt>Source</tt>.
         */
        private String direction;
        
        /**
         * Instantiate a <tt>Source</tt>.
         * 
         * @param type the type attribute of the <tt>Source</tt>.
         * @param ssrc the ssrc attribute of the <tt>Source</tt>.
         * @param direction the direction attribute of the <tt>Source</tt>.
         */
        public Source(String type,String ssrc,String direction)
        {
            this.type = type;
            this.ssrc = ssrc;
            this.direction = direction;
        }

        /**
         * Get the type attribute of the <tt>Source</tt>. 
         * @return the type attribute of the <tt>Source</tt>.
         */
        public String getType()
        {
            return type;
        }
        
        /**
         * Get the ssrc attribute of the <tt>Source</tt>. 
         * @return the ssrc attribute of the <tt>Source</tt>.
         */
        public String getSsrc()
        {
            return ssrc;
        }
        
        /**
         * Get the direction attribute of the <tt>Source</tt>. 
         * @return the direction attribute of the <tt>Source</tt>.
         */
        public String getDirection()
        {
            return direction;
        }
    }
}
