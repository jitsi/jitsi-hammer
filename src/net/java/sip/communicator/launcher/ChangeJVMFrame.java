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
package net.java.sip.communicator.launcher;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * The <tt>ChangeJVMFrame</tt> will ask the user to install the newest java
 * version if she's using an old and incompatible one.
 *
 * @author Yana Stamcheva
 */
public class ChangeJVMFrame
    extends JFrame
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 0L;

    /**
     * The MacOSX operating system.
     */
    public static final String MAC_OSX = "MacOSX";

    /**
     * The Windows operating system.
     */
    public static final String WINDOWS = "Windows";

    /**
     * The Linux operating system.
     */
    public static final String LINUX = "Linux";

    private JTextArea textArea = new JTextArea();

    private JEditorPane javaLinkPane = new JEditorPane();

    private String text = "Sorry. Your Java version is too old. The minimum"
        + " Java version required is 1.5. Please folow the link below to install"
        + " the newest version for your environment.";

    private String macLink
        = "<a href=\"http://www.apple.com/downloads/macosx/apple/application_updates/" +
          "javaformacosx104release9.html\">Download Java 1.5 for MacOSX</a>";

    private String defaultLink
        = "<a href=\"https://cds.sun.com/is-bin/INTERSHOP.enfinity/" +
        "WFS/CDS-CDS_Developer-Site/en_US/-/USD/ViewProductDetail-Start?" +
        "ProductRef=jre-6u18-oth-JPR@CDS-CDS_Developer\">Download Java 1.6</a>";

    private JPanel mainPanel = new JPanel(new BorderLayout());

    /**
     * Initializes new frame that will ask the user to install the newest java
     * version if she's using an old and incompatible one.
     *
     * @param osName OS name
     */
    public ChangeJVMFrame(String osName)
    {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setTitle("SIP Communicator requirements");

        this.mainPanel.setPreferredSize(
            new Dimension(450, 150));

        this.mainPanel.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10));

        this.textArea.setOpaque(false);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        this.textArea.setText(text);
        this.textArea.setEditable(false);

        this.javaLinkPane.setOpaque(false);
        this.javaLinkPane.setContentType("text/html");
        this.javaLinkPane.setEditable(false);

        this.javaLinkPane.setText(osName.equals(MAC_OSX) ? macLink : defaultLink);

        this.javaLinkPane.addHyperlinkListener(new HyperlinkListener()
        {
            public void hyperlinkUpdate(HyperlinkEvent e)
            {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                    new BrowserLauncher().openURL(e.getDescription());
            }
        });

        this.mainPanel.add(textArea, BorderLayout.NORTH);
        this.mainPanel.add(javaLinkPane, BorderLayout.CENTER);

        this.getContentPane().add(mainPanel);

        this.pack();
    }
}
