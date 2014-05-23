package org.jitsi.hammer;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smackx.muc.*;


public class MyPacketListener implements PacketListener {
	/*
	 * muc is used to send a message when this client receive a Message (not from itself)
	 */
	protected MultiUserChat muc;
	/*
	 * myID is this client ID of a "from" attribute of a Message
	 */
	protected String myID;

	public MyPacketListener(MultiUserChat muc,String id)
	{
		this.muc = muc;
		this.myID = id;
	}

	/*
	 * When a packet is received, it check is this packet is a Message.
	 * If it is, it print its body and sender.
	 * If the Message received isn't from this client, this client responds with "C'est pas faux.".
	 */
	public void processPacket(Packet packet)
	{
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
				catch (XMPPException e) {}
			}
		}
	}
}
