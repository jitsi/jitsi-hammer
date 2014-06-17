/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
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
    private String namespace;
    
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
