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

/**
 * @author Thomas Kuntz
 * 
 * SsrcPacketExtension is used by SsrcProvider.
 * This class is used to store attributes of XML sub-documents
 * starting with "ssrc" tag.
 */
public class SsrcPacketExtension implements ExtensionElement
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
