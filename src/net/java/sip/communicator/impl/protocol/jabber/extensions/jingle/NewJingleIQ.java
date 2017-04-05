package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.extension.NewContentPacketExtension;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.IQ;

import javax.swing.text.AbstractDocument;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bbaldino on 4/3/17.
 */
public class NewJingleIQ extends IQ
{
    /**
     * The name space that jingle belongs to.
     */
    public static final String NAMESPACE = "urn:xmpp:jingle:1";

    /**
     * The name of the element that contains the jingle data.
     */
    public static final String ELEMENT_NAME = "jingle";

    /**
     * The name of the argument that contains the jingle action value.
     */
    public static final String ACTION_ATTR_NAME = "action";

    /**
     * The name of the argument that contains the "initiator" jid.
     */
    public static final String INITIATOR_ATTR_NAME = "initiator";

    /**
     * The name of the argument that contains the "responder" jid.
     */
    public static final String RESPONDER_ATTR_NAME = "responder";

    /**
     * The name of the argument that contains the session id.
     */
    public static final String SID_ATTR_NAME = "sid";

    private JingleAction action;
    private String initiator;
    private String responder;
    private String sid;
    /**
     * The <tt>reason</tt> extension in a <tt>jingle</tt> IQ providers machine
     * and possibly human-readable information about the reason for the action.
     */
    private ReasonPacketExtension reason;

    /**
     * The list of "content" elements included in this IQ.
     */
    private final List<NewContentPacketExtension> contentList
            = new ArrayList<>();

    public NewJingleIQ()
    {
        super(ELEMENT_NAME, NAMESPACE);
    }

    @Override
    public IQ.IQChildElementXmlStringBuilder getIQChildElementBuilder(IQ.IQChildElementXmlStringBuilder xml) {
        xml.attribute(ACTION_ATTR_NAME, getAction());
        if (initiator != null)
        {
            xml.attribute(INITIATOR_ATTR_NAME, getInitiator());
        }

        if (responder != null)
        {
            xml.attribute(RESPONDER_ATTR_NAME, getResponder());
        }

        xml.attribute(SID_ATTR_NAME, getSID());

        xml.rightAngleBracket();
        for (NewContentPacketExtension cpe : contentList)
        {
            xml.element(cpe);
        }
        if (reason != null)
        {
            xml.element(reason);
        }

        return xml;
    }

    public void setAction(JingleAction action) {
        this.action = action;
    }

    public JingleAction getAction() {
        return this.action;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getInitiator() {
        return this.initiator;
    }

    public void setSID(String sid) {
        this.sid = sid;
    }

    public String getSID() {
        return this.sid;
    }

    public void setResponder(String responder) {
        this.responder = responder;
    }

    public String getResponder() {
        return this.responder;
    }

    /**
     * Specifies this IQ's <tt>reason</tt> extension. The <tt>reason</tt>
     * extension in a <tt>jingle</tt> IQ provides machine and possibly human
     * -readable information about the reason for the action.
     *
     * @param reason this IQ's <tt>reason</tt> extension.
     */
    public void setReason(ReasonPacketExtension reason)
    {
        this.reason = reason;
    }

    /**
     * Returns this IQ's <tt>reason</tt> extension. The <tt>reason</tt>
     * extension in a <tt>jingle</tt> IQ provides machine and possibly human
     * -readable information about the reason for the action.
     *
     * @return this IQ's <tt>reason</tt> extension.
     */
    public ReasonPacketExtension getReason()
    {
        return reason;
    }

    /**
     * Adds <tt>contentPacket</tt> to this IQ's content list.
     *
     * @param contentPacket the content packet extension we'd like to add to
     * this element's content list.
     */
    public void addContent(NewContentPacketExtension contentPacket)
    {
        synchronized(contentList)
        {
            this.contentList.add(contentPacket);
        }
    }

    public List<NewContentPacketExtension> getContentList() {
        synchronized(contentList)
        {
            return new ArrayList<NewContentPacketExtension>(contentList);
        }
    }
}
