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
