package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import org.jivesoftware.smack.packet.Element;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.NamedElement;
import org.jivesoftware.smack.util.XmlStringBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An <tt>ExtensionElement</tt> implementation which provides common functionality <tt>ExtensionElement</tt>s
 *
 * @author Brian baldino
 */
public abstract class NewAbstractExtensionElement
    implements ExtensionElement
{
    /**
     * The name space of this packet extension. Should remain <tt>null</tt> if
     * there's no namespace associated with this element.
     */
    private final String elementName;

    /**
     * The name space of this packet extension. Should remain <tt>null</tt> if
     * there's no namespace associated with this element.
     */
    private String namespace;

    /**
     * NOTE(brian): temporarily(?) doing this for now to handle elements
     *  that dont actually have a unique namespace.  If this is 'true', then
     *  the namespace will not be serialized when returning XML.  This allows
     *  us to register a namespace-less element under multiple namespaces, but
     *  not have the namespace actually serialize when creating the xml.
     */
    public boolean namespaceInherited;

    /**
     * A map of all attributes that this extension is currently using.
     */
    protected final Map<String, Object> attributes
            = new LinkedHashMap<String, Object>();

    /**
     * A list of extensions registered with this element.
     */
    private final List<Element> childExtensions
            = new ArrayList<>();

    /**
     * The text content of this packet extension, if any.
     */
    private String textContent;

    protected NewAbstractExtensionElement(String elementName, String namespace)
    {
        this.namespace = namespace;
        this.elementName = elementName;
    }

    /**
     * Returns the name of the <tt>encryption</tt> element.
     *
     * @return the name of the <tt>encryption</tt> element.
     */
    public String getElementName()
    {
        return elementName;
    }

    /**
     * Returns the XML namespace for this element or <tt>null</tt> if the
     * element does not live in a namespace of its own.
     *
     * @return the XML namespace for this element or <tt>null</tt> if the
     * element does not live in a namespace of its own.
     */
    public String getNamespace()
    {
        return namespace;
    }

    /**
     * Set the XML namespace for this element.
     *
     * @param namespace the XML namespace for this element.
     */
    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    /**
     * Returns an XML representation of this extension.
     *
     * @return an XML representation of this extension.
     */
    public CharSequence toXML()
    {
        XmlStringBuilder xmlBuilder;
        if (namespaceInherited)
        {
            xmlBuilder = new XmlStringBuilder((NamedElement)this);
        }
        else
        {
            xmlBuilder = new XmlStringBuilder(this);

        }

        for (Map.Entry<String, Object> entry : attributes.entrySet())
        {
            xmlBuilder.attribute(entry.getKey(), entry.getValue().toString());
        }

        List<Element> childElements = new ArrayList<>(getChildExtensions());
        String text = getText();

        if (childElements.isEmpty() && text == null)
        {
            xmlBuilder.closeEmptyElement();
        }
        else
        {
            xmlBuilder.rightAngleBracket();

            // Child elements, if any
            for (Element e : childElements)
            {
                xmlBuilder.element(e);
            }

            // Text, if non-null
            xmlBuilder.optAppend(getText());

            xmlBuilder.closeElement(this);
        }

        return xmlBuilder.toString();
    }

    /**
     * Specifies the text content of this extension.
     *
     * @param text the text content of this extension.
     */
    public void setText(String text)
    {
        this.textContent = text;
    }

    /**
     * Returns the text content of this extension or <tt>null</tt> if no text
     * content has been specified so far.
     *
     * @return the text content of this extension or <tt>null</tt> if no text
     * content has been specified so far.
     */
    public String getText()
    {
        return textContent;
    }

    /**
     * Adds the specified <tt>childExtension</tt> to the list of extensions
     * registered with this packet.
     * <p/>
     * Overriding extensions may need to override this method if they would like
     * to have anything more elaborate than just a list of extensions (e.g.
     * casting separate instances to more specific.
     *
     * @param childExtension the extension we'd like to add here.
     */
    public void addChildExtension(Element childExtension)
    {
        childExtensions.add(childExtension);
    }

    /**
     * Returns all sub-elements for this <tt>NewAbstractExtensionElement</tt> or
     * <tt>null</tt> if there aren't any.
     * <p>
     * Overriding extensions may need to override this method if they would like
     * to have anything more elaborate than just a list of extensions.
     *
     * @return the {@link List} of elements that this packet extension contains.
     */
    public List<Element> getChildExtensions()
    {
        return childExtensions;
    }

    /**
     * Returns this packet's direct child extensions that match the
     * specified <tt>type</tt>.
     *
     * @param <T> the specific <tt>PacketExtension</tt> type of child extensions
     * to be returned
     *
     * @param type the <tt>Class</tt> of the extension we are looking for.
     *
     * @return a (possibly empty) list containing all of this packet's direct
     * child extensions that match the specified <tt>type</tt>
     */
    public <T extends NewAbstractExtensionElement> List<T> getChildExtensionsOfType(
            Class<T> type)
    {
        synchronized (childExtensions)
        {
            return getChildExtensions()
                    .stream()
                    .filter(element -> type.isInstance(element))
                    .map(element -> (T) element)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Returns this packet's first direct child extension that matches the
     * specified <tt>type</tt>.
     *
     * @param <T> the specific type of <tt>PacketExtension</tt> to be returned
     *
     * @param type the <tt>Class</tt> of the extension we are looking for.
     *
     * @return this packet's first direct child extension that matches specified
     * <tt>type</tt> or <tt>null</tt> if no such child extension was found.
     */
    public <T extends NewAbstractExtensionElement> T getFirstChildOfType(Class<T> type)
    {
        synchronized (childExtensions)
        {
            return getChildExtensions()
                    .stream()
                    .filter(element -> type.isInstance(element))
                    .map(element -> (T) element)
                    .findFirst()
                    .get();
        }
    }

    /**
     * Sets the value of the attribute named <tt>name</tt> to <tt>value</tt>.
     *
     * @param name the name of the attribute that we are setting.
     * @param value an {@link Object} whose <tt>toString()</tt> method returns
     * the XML value of the attribute we are setting or <tt>null</tt> if we'd
     * like to remove the attribute with the specified <tt>name</tt>.
     */
    public void setAttribute(String name, Object value)
    {
        synchronized (attributes)
        {
            if (value != null)
            {
                this.attributes.put(name, value);
            }
            else
            {
                this.attributes.remove(name);
            }
        }
    }

    /**
     * Returns the attribute with the specified <tt>name</tt> from the list of
     * attributes registered with this packet extension.
     *
     * @param attribute the name of the attribute that we'd like to retrieve.
     *
     * @return the value of the specified <tt>attribute</tt> or <tt>null</tt>
     * if no such attribute is currently registered with this extension.
     */
    public Object getAttribute(String attribute)
    {
        synchronized(attributes)
        {
            return attributes.get(attribute);
        }
    }

    /**
     * Tries to parse the value of the specified <tt>attribute</tt> as an
     * <tt>URI</tt> and returns it.
     *
     * @param attribute the name of the attribute that we'd like to retrieve.
     *
     * @return the <tt>URI</tt> value of the specified <tt>attribute</tt> or
     * <tt>null</tt> if no such attribute is currently registered with this
     * extension.
     * @throws IllegalArgumentException if <tt>attribute</tt> is not a valid {@link
     * URI}
     */
    public URI getAttributeAsURI(String attribute)
            throws IllegalArgumentException
    {
        synchronized(attributes)
        {
            String attributeVal = getAttributeAsString(attribute);

            if (attributeVal == null)
                return null;

            try
            {
                URI uri = new URI(attributeVal);

                return uri;
            }
            catch (URISyntaxException e)
            {
                throw new IllegalArgumentException(e);
            }
        }
    }

    /**
     * Returns the <tt>int</tt> value of the attribute with the specified
     * <tt>name</tt>.
     *
     * @param attribute the name of the attribute that we'd like to retrieve.
     *
     * @return the <tt>int</tt> value of the specified <tt>attribute</tt> or
     * <tt>-1</tt> if no such attribute is currently registered with this
     * extension.
     */
    public int getAttributeAsInt(String attribute)
    {
        return getAttributeAsInt(attribute, -1);
    }

    /**
     * Returns the <tt>int</tt> value of the attribute with the specified
     * <tt>name</tt>.
     *
     * @param attribute the name of the attribute that we'd like to retrieve
     * @param defaultValue the <tt>int</tt> to be returned as the value of the
     * specified attribute if no such attribute is currently registered with
     * this extension
     * @return the <tt>int</tt> value of the specified <tt>attribute</tt> or
     * <tt>defaultValue</tt> if no such attribute is currently registered with
     * this extension
     */
    public int getAttributeAsInt(String attribute, int defaultValue)
    {
        synchronized(attributes)
        {
            String value = getAttributeAsString(attribute);

            return (value == null) ? defaultValue : Integer.parseInt(value);
        }
    }

    /**
     * Returns the string value of the attribute with the specified
     * <tt>name</tt>.
     *
     * @param attribute the name of the attribute that we'd like to retrieve.
     *
     * @return the String value of the specified <tt>attribute</tt> or
     * <tt>null</tt> if no such attribute is currently registered with this
     * extension.
     */
    public String getAttributeAsString(String attribute)
    {
        synchronized(attributes)
        {
            Object attributeVal = attributes.get(attribute);

            return attributeVal == null ? null : attributeVal.toString();
        }
    }

    /**
     * Gets the names of the attributes which currently have associated values
     * in this extension.
     *
     * @return the names of the attributes which currently have associated
     * values in this extension
     */
    public List<String> getAttributeNames()
    {
        synchronized (attributes)
        {
            return new ArrayList<String>(attributes.keySet());
        }
    }

    /**
     * Removes the attribute with the specified <tt>name</tt> from the list of
     * attributes registered with this packet extension.
     *
     * @param name the name of the attribute that we are removing.
     */
    public void removeAttribute(String name)
    {
        synchronized(attributes)
        {
            attributes.remove(name);
        }
    }

    /**
     * Clones the attributes, namespace and text of a specific
     * <tt>NewAbstractExtensionElement</tt> into a new
     * <tt>NewAbstractExtensionElement</tt> instance of the same run-time type.
     *
     * @param src the <tt>NewAbstractExtensionElement</tt> to be cloned
     * @return a new <tt>NewAbstractExtensionElement</tt> instance of the run-time
     * type of the specified <tt>src</tt> which has the same attributes,
     * namespace and text
     * @throws Exception if an error occurs during the cloning of the specified
     * <tt>src</tt>
     */
    @SuppressWarnings("unchecked")
    public static <T extends NewAbstractExtensionElement> T clone(T src)
    {
        T dst = null;
        try
        {
            dst = (T) src.getClass().newInstance();
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }

        // attributes
        for (String name : src.getAttributeNames())
        {
            dst.setAttribute(name, src.getAttribute(name));
        }
        // namespace
        dst.setNamespace(src.getNamespace());
        // text
        dst.setText(src.getText());

        return dst;
    }
}
