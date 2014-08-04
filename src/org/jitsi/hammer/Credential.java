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
    private String username;
    private String password;

    public Credential() {}

    public Credential(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getUsername()
    {
        return this.username;
    }

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
