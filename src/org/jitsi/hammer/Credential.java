/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer;

/**
 * @author Thomas Kuntz
 *
 * A class that describe a jabber user credential (username and password).
 *
 */
public class Credential
{
    /**
     * The username composing the Credentials.
     */
    private String username;

    /**
     * The password composing the Credentials.
     */
    private String password;

    /**
     * Empty constructor : does nothing.
     */
    public Credential() {}

    /**
     * Create a <tt>Credential</tt> with already set username and password.
     * @param username the username to be set.
     * @param password the password to be set.
     */
    public Credential(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    /**
     * Set the username of this <tt>Credential</tt> object.
     * @param username the username that will be set.
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * Set the password of this <tt>Credential</tt> object.
     * @param username the password that will be set.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Get the username of this <tt>Credential</tt> object.
     * @return the username of this <tt>Credential</tt> object.
     */
    public String getUsername()
    {
        return this.username;
    }

    /**
     * Get the password of this <tt>Credential</tt> object.
     * @return the password of this <tt>Credential</tt> object.
     */
    public String getPassword()
    {
        return this.password;
    }

    @Override
    public String toString()
    {
        return "("+username+" , "+password+")";
    }
}
