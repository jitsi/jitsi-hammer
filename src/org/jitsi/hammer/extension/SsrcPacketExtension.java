/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer.extension;

import org.jivesoftware.smack.packet.*;

/**
 * @author Thomas Kuntz
 * 
 * SsrcPacketExtension is used by SsrcProvider.
 * This class is used to store attributes of XML sub-documents
 * starting with "ssrc" tag.
 */
public class SsrcPacketExtension implements PacketExtension
{
    /**
     * The namespace of this <tt>SsrcPacketExtension</tt>
     */
    private String namespace = SsrcProvider.NAMESPACE;
    
    /**
     * The cname of this <tt>SsrcPacketExtension</tt>
     */
    private String cname;
    
    /**
     * The msid of this <tt>SsrcPacketExtension</tt>
     */
    private String msid;
    
    /**
     * The mslabel of this <tt>SsrcPacketExtension</tt>
     */
    private String mslabel;
    
    /**
     * The label of this <tt>SsrcPacketExtension</tt>
     */
    private String label;
    
    /**
     * The ssrc of this <tt>SsrcPacketExtension</tt>
     */
    private String ssrc;

    
    
    /**
     * Get the element name of the <tt>SsrcPacketExtension</tt>.
     * @return the element name of the <tt>SsrcPacketExtension</tt>.
     */
    public String getElementName()
    {
        return SsrcProvider.ELEMENT_NAME;
    }
    
    /**
     * Get the namespace of the <tt>SsrcPacketExtension</tt>.
     * @return the namespace of the <tt>SsrcPacketExtension</tt>.
     */
    public String getNamespace()
    {
        return namespace;
    }
    
    /**
     * Get the cname of the <tt>SsrcPacketExtension</tt>.
     * @return the cname of the <tt>SsrcPacketExtension</tt>.
     */
    public String getCname()
    {
        return cname;
    }
    
    /**
     * Get the msid of the <tt>SsrcPacketExtension</tt>.
     * @return the msid of the <tt>SsrcPacketExtension</tt>.
     */
    public String getMsid()
    {
        return msid;
    }
    
    /**
     * Get the mslabel of the <tt>SsrcPacketExtension</tt>.
     * @return the mslabel of the <tt>SsrcPacketExtension</tt>.
     */
    public String getMslabel()
    {
        return mslabel;
    }
    
    /**
     * Get the label of the <tt>SsrcPacketExtension</tt>.
     * @return the label of the <tt>SsrcPacketExtension</tt>.
     */
    public String getLabel()
    {
        return label;
    }
    
    /**
     * Get the ssrc of the <tt>SsrcPacketExtension</tt>.
     * @return the ssrc of the <tt>SsrcPacketExtension</tt>.
     */
    public String getSsrc()
    {
        return ssrc;
    }
    
    
    
    /**
     * Set the namespace of the <tt>SsrcPacketExtension</tt>.
     * @param ns the namespace to be set of the <tt>SsrcPacketExtension</tt>.
     */
    public void setNamespace(String ns)
    {
        namespace = ns;
    }

    /**
     * Set the cname of the <tt>SsrcPacketExtension</tt>.
     * @param cname the cname of the <tt>SsrcPacketExtension</tt>.
     */
    public void setCname(String cname)
    {
        this.cname = cname;
    }
    
    /**
     * Set the msid of the <tt>SsrcPacketExtension</tt>.
     * @param msid the msid of the <tt>SsrcPacketExtension</tt>.
     */
    public void setMsid(String msid)
    {
        this.msid = msid;
    }
    
    /**
     * Set the mslabel of the <tt>SsrcPacketExtension</tt>.
     * @param mslabel the mslabel of the <tt>SsrcPacketExtension</tt>.
     */
    public void setMslabel(String mslabel)
    {
        this.mslabel = mslabel;
    }
    
    /**
     * Set the label of the <tt>SsrcPacketExtension</tt>.
     * @param label the label of the <tt>SsrcPacketExtension</tt>.
     */
    public void setLabel(String label)
    {
        this.label = label;
    }
    
    /**
     * Set the ssrc of the <tt>SsrcPacketExtension</tt>.
     * @param ssrc the ssrc of the <tt>SsrcPacketExtension</tt>.
     */
    public void setSsrc(String ssrc)
    {
        this.ssrc = ssrc;
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
        bldr.append("<"+ SsrcProvider.ELEMENT_NAME +" xmlns=\""+getNamespace()+"\"");
        bldr.append(" cname=\""+ this.cname +"\"");
        bldr.append(" msid=\""+ this.msid +"\"");
        bldr.append(" mslabel=\""+ this.mslabel +"\"");
        bldr.append(" label=\""+ this.label +"\"");
        bldr.append(" ssrc=\""+ this.ssrc +"\"");
        bldr.append("/>");
        return bldr.toString();
    }
}
