package net.java.sip.communicator.impl.protocol.jabber.extensions;

/**
 * An <tt>AbstractPacketExtension</tt> inheritor that represents 
 * a <tt>property</tt> element of the conference
 *
 * @author Maksym Kulish
 */
public class ConferencePropertyPacketExtension extends AbstractPacketExtension {

    /**
     * The name of the "parameter" element.
     */
    public static final String ELEMENT_NAME = "parameter";
    
    /**
     * The name of the attribute that contains the property name
     */
    private static final String NAME_ATTR_NAME = "name";

    /**
     * The name of the attribute which holds the property value
     */
    private static final String VALUE_ATTR_NAME = "value";
    

    /**
     * The default constructor which does not set the element values
     */
    public ConferencePropertyPacketExtension() {
        super(null, ELEMENT_NAME);
    }
    
    /**
     * Construct this <tt>ConferencePropertyPacketExtension</tt>
     * using given property name and value
     * 
     * @param propertyName the property name
     * @param propertyValue the property value
     */
    public ConferencePropertyPacketExtension(
            String propertyName, String propertyValue) {
        super(null, ELEMENT_NAME);
        setPropertyName(propertyName);
        setPropertyValue(propertyValue);
    }

    /**
     * Get the property name associated with this 
     * <tt><ConferencePropertyPacketExtension/tt>
     *     
     * @return property name
     */
    public String getPropertyName() {
        return super.getAttributeAsString(NAME_ATTR_NAME);
    }

    /**
     * Set the property name associated with this 
     * <tt><ConferencePropertyPacketExtension/tt>
     *
     * @param propertyName the property name
     */
    public void setPropertyName(String propertyName) {
        super.setAttribute(NAME_ATTR_NAME, propertyName);
    }

    /**
     * Get the property value associated with this 
     * <tt><ConferencePropertyPacketExtension/tt>
     *
     * @return property value
     */
    public String getPropertyValue() {
        return super.getAttributeAsString(VALUE_ATTR_NAME);
    }

    /**
     * Set the property value associated with this 
     * <tt><ConferencePropertyPacketExtension/tt>
     *
     * @param propertyValue the property name
     */
    public void setPropertyValue(String propertyValue) {
        super.setAttribute(VALUE_ATTR_NAME, propertyValue);
    }
    
}
