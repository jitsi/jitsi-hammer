package org.jitsi.hammer;

import org.jivesoftware.smack.packet.*;
import java.util.*;

/*
 * MediaPacketExtension is used by MediaProvider.
 * This class is used to store attributes of XML sub-documents starting with <media> tag.
 */
public class MediaPacketExtension implements PacketExtension
{
    protected String namespace;
    protected List<Source> sourceList = new ArrayList<Source>();

    public String getElementName()
    {
        return "media";
    }
    public String getNamespace()
    {
        return namespace;
    }
    
    public void setNamespace(String ns)
    {
        namespace = ns;
    }

    /*
     * toXML returns XML document equivalent to the informations this class carries.
     */
    public String toXML()
    {
        StringBuilder bldr = new StringBuilder();
        bldr.append("<media xmlns=\""+namespace+"\">");
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

    /*
     * addSource add a Source object to the sourceList representing a <source /> line.
     */
    public void addSource(String type,String ssrc,String direction)
    {
        sourceList.add(new Source(type,ssrc,direction));
    }

    /*
     * Source represent a <source /> line received from a jitmeet session
     * It implements attributes that a <source /> tag is expected to have.
     */
    public class Source
    {
        protected String type;
        protected String ssrc;
        protected String direction;
        
        public Source(String type,String ssrc,String direction)
        {
            this.type = type;
            this.ssrc = ssrc;
            this.direction = direction;
        }

        public String getType()
        {
            return type;
        }
        public String getSsrc()
        {
            return ssrc;
        }
        public String getDirection()
        {
            return direction;
        }
    }
}
