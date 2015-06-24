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

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smackx.muc.*;

/**
 * 
 * @author Thomas Kuntz
 *
 * An implement of <tt>PacketListener</tt> that should listen to the message
 * of a MUC to display them in the terminal, and send automatic replies.
 *
 */
public class MyPacketListener implements PacketListener 
{
    /**
     * muc is used to send a message when this client receive a Message (not from itself)
     */
    protected MultiUserChat muc;
    /**
     * myID is this client ID of a "from" attribute of a Message
     */
    protected String myID;

    /**
     * Instantiate a new <tt>MyPacketListener</tt>
     * @param muc the <tt>MultiUserChat</tt> used to send a reply to a <tt>Message<tt>
     * @param id the id of the user this <tt>PacketListener</tt> listen for.
     */
    public MyPacketListener(MultiUserChat muc,String id) 
    {
        this.muc = muc;
        this.myID = id;
    }

    public void processPacket(Packet packet) 
    {
        /*
         * When a packet is received, it checks if this packet is a Message.
         * If it is, it print its body and sender.
         * If the Message received isn't from this client,
         * this client replies with "C'est pas faux.".
         */
        
        if(packet instanceof Message) 
        {
            Message msg = (Message)packet;
            System.out.println(msg.getFrom().split("/")[1] + " : " + msg.getBody());

            if(packet.getFrom().equals(myID) !=  true)
            {           
                try 
                {
                    muc.sendMessage("C'est pas faux.");
                }
                catch (XMPPException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
