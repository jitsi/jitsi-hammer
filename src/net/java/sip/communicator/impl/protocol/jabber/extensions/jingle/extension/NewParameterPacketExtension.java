package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.extension;

/**
 * Represents the <tt>parameter</tt> elements described in XEP-0167.
 *
 * @author Emil Ivov
 */

//NOTE(brian): since this doesn't use a namespace(?) it would technically be more accurate for this to derive
// from NamedElement (and not ExtensionElement), but will see if that ends up being a big deal
public class NewParameterPacketExtension
    extends NewAbstractExtensionElement
{
    /**
     * The name of the "parameter" element.
     */
    public static final String ELEMENT_NAME = "parameter";

    /**
     * The namespace of the "parameter" element.
     */
    public static final String NAMESPACE = "urn:xmpp:jingle:apps:rtp:1";

    /**
     * The name of the <tt>name</tt> parameter in the <tt>parameter</tt>
     * element.
     */
    public static final String NAME_ATTR_NAME = "name";

    /**
     * The name of the <tt>value</tt> parameter in the <tt>parameter</tt>
     * element.
     */
    public static final String VALUE_ATTR_NAME = "value";

    public NewParameterPacketExtension()
    {
        this(null, null);
    }

    public NewParameterPacketExtension(String name, String value)
    {
        super(ELEMENT_NAME, NAMESPACE);
        setName(name);
        setValue(value);
    }

    @Override
    public String getElementName()
    {
        return NewParameterPacketExtension.ELEMENT_NAME;
    }

    /**
     * Sets the name of the format parameter we are representing here.
     *
     * @param name the name of the format parameter we are representing here.
     */
    public void setName(String name)
    {
        super.setAttribute(NAME_ATTR_NAME, name);
    }

    /**
     * Returns the name of the format parameter we are representing here.
     *
     * @return the name of the format parameter we are representing here.
     */
    public String getName()
    {
        return super.getAttributeAsString(NAME_ATTR_NAME);
    }

    /**
     * Sets that value of the format parameter we are representing here.
     *
     * @param value the value of the format parameter we are representing here.
     */
    public void setValue(String value)
    {
        super.setAttribute(VALUE_ATTR_NAME, value);
    }

    /**
     * Returns the value of the format parameter we are representing here.
     *
     * @return the value of the format parameter we are representing here.
     */
    public String getValue()
    {
        return super.getAttributeAsString(VALUE_ATTR_NAME);
    }


}
